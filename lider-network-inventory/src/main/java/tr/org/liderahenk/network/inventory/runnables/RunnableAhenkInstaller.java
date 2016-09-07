package tr.org.liderahenk.network.inventory.runnables;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.network.inventory.contants.Constants.InstallMethod;
import tr.org.liderahenk.network.inventory.dto.AhenkSetupDetailDto;
import tr.org.liderahenk.network.inventory.dto.AhenkSetupDto;
import tr.org.liderahenk.network.inventory.entities.AhenkSetupParameters;
import tr.org.liderahenk.network.inventory.entities.AhenkSetupResultDetail;
import tr.org.liderahenk.network.inventory.exception.CommandExecutionException;
import tr.org.liderahenk.network.inventory.exception.SSHConnectionException;
import tr.org.liderahenk.network.inventory.utils.setup.SetupUtils;

/**
 * A runnable that is responsible of installing Ahenk to a machine.
 * 
 * @author <a href="mailto:caner.feyzullahoglu@agem.com.tr">Caner
 *         Feyzullahoğlu</a>
 */
public class RunnableAhenkInstaller implements Runnable {

	private Logger logger = LoggerFactory.getLogger(RunnableAhenkInstaller.class);

	private AhenkSetupParameters setupParams;

	private AhenkSetupDto setupDto;

	private String ip;

	private String username;

	private String password;

	private Integer port;

	private String privateKey;

	private String passphrase;

	private String downloadUrl;

	private final static String MAKE_DIR_UNDER_TMP = "mkdir /tmp/{0}";

	private String xmppHost;
	private String xmppUsername;
	private String xmppServiceName;
	private String receiveFile;
	private String useTls;
	
	public RunnableAhenkInstaller(AhenkSetupDto setupDto, String ip, String username, String password, Integer port,
			String privateKey, String passphrase, InstallMethod installMethod, String downloadUrl,
			AhenkSetupParameters setupParams, String xmppHost, String xmppUsername, String xmppServiceName, String receiveFile, String useTls) {
		super();
		this.setupDto = setupDto;
		this.ip = ip;
		this.username = username;
		this.password = password;
		this.port = port;
		this.privateKey = privateKey;
		this.passphrase = passphrase;
		this.downloadUrl = downloadUrl;
		this.setupParams = setupParams;
		this.xmppHost = xmppHost;
		this.xmppUsername = xmppUsername;
		this.xmppServiceName = xmppServiceName;
		this.receiveFile = receiveFile;
		this.useTls = useTls;
	}

	@Override
	public void run() {
		logger.info("Runnable started.");

		try {
			logger.info("Checking SSH authentication to: " + ip);

			// Check authorization before starting installation
			final boolean canConnect = SetupUtils.canConnectViaSsh(ip, username, password, port, privateKey,
					passphrase);

			// If we can connect to machine install Ahenk
			if (canConnect) {
				logger.info("Authentication successfull for: " + ip);

				// Check installation method

				// In case of folder name clash use current time as postfix
				Date date = new Date();
				SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy-HH:mm:ss");
				String timestamp = dateFormat.format(date);

				logger.info("Creating directory under /tmp");
				SetupUtils.executeCommand(ip, username, password, port, privateKey, passphrase,
						MAKE_DIR_UNDER_TMP.replace("{0}", "ahenkTmpDir" + timestamp));

				logger.info("Downloading file from URL: " + downloadUrl);
				SetupUtils.downloadPackage(ip, username, password, port, privateKey, passphrase,
						"ahenkTmpDir" + timestamp, "ahenk.deb", downloadUrl);

				logger.info("Creating repository file");
				SetupUtils.executeCommand(ip, username, password, port, privateKey, passphrase,
						"touch /etc/apt/sources.list.d/liderahenk.list");

				logger.info("Writing to repository file");
				SetupUtils.executeCommand(ip, username, password, port, privateKey, passphrase,
						"echo 'deb http://ftp.pardus.org.tr/lider-ahenk/la-stable yenikusak main' > /etc/apt/sources.list.d/liderahenk.list");

				logger.info("Adding key");
				SetupUtils.executeCommand(ip, username, password, port, privateKey, passphrase,
						"wget -qO - http://ftp.pardus.org.tr/Release.pub | apt-key add -");

				logger.info("Updating package list");
				SetupUtils.executeCommand(ip, username, password, port, privateKey, passphrase, "apt-get update");

				logger.info("Clearing old Ahenk files");
				SetupUtils.executeCommand(ip, username, password, port, privateKey, passphrase,
						"rm -rf /etc/ahenk/ahenk.db");
				SetupUtils.executeCommand(ip, username, password, port, privateKey, passphrase, "rm -rf /opt/ahenk");

				logger.info("Installing Ahenk");
				SetupUtils.installPackageGdebiWithOpts(ip, username, password, port, privateKey, passphrase,
						"/tmp/ahenkTmpDir" + timestamp + "/ahenk.deb", "Dpkg::Options::='--force-overwrite'");

				logger.info("Preparing ahenk.conf file");
				File ahenkConfFile = prepareConfFile();
				
				logger.info("Copying ahenk.conf file");
				SetupUtils.copyFile(ip, username, password, port, privateKey, passphrase, ahenkConfFile, "/etc/ahenk/");
				
				logger.info("Starting Ahenk service");
				SetupUtils.executeCommand(ip, username, password, port, privateKey, passphrase,
						"service ahenk start");

				logger.info("Ahenk installation successfully completed.");

				logAndAddDetailEntity("Successfully installed to: " + ip, "INFO");
				setupDto.getSetupDetailList().add(new AhenkSetupDetailDto(ip, true, null));

			} else {
				logAndAddDetailEntity("Could not connect to: " + ip + " passing over to another IP.", "ERROR");
				setupDto.getSetupDetailList().add(new AhenkSetupDetailDto(ip, false, "Could not connect to: " + ip));
			}

		} catch (SSHConnectionException e) {
			logAndAddDetailEntity("Error occured installing Ahenk on IP: " + ip + " Error message: " + e.getMessage(),
					"ERROR");
			setupDto.getSetupDetailList().add(new AhenkSetupDetailDto(ip, false, e.getMessage()));
			e.printStackTrace();
		} catch (CommandExecutionException e) {
			logAndAddDetailEntity("Error occured installing Ahenk on IP: " + ip + " Error message: " + e.getMessage(),
					"ERROR");
			setupDto.getSetupDetailList().add(new AhenkSetupDetailDto(ip, false, e.getMessage()));
			e.printStackTrace();
		}
	}

	private File prepareConfFile() {
		String confText = readFile("/ahenk.conf");
		
		Map<String, String> map = new HashMap<String, String>();
		map.put("#HOST", xmppHost);
		map.put("#LIDERJID", xmppUsername);
		map.put("#SERVICENAME", xmppServiceName);
		map.put("#RECEIVER_RESOURCE", "");
		map.put("#RECEIVE_FILE", receiveFile);
		map.put("#USE_TLS", useTls);
		
		confText = SetupUtils.replace(map, confText);
		
		return writeToFile(confText, "ahenk.conf");
	}
	
	/**
	 * Creates file under temporary file directory and writes configuration to
	 * it. Returns the file itself.
	 * 
	 * @param content
	 * @param namePrefix
	 * @param nameSuffix
	 * @return returns the file itself
	 */
	private File writeToFile(String content, String fileName) {

		File temp = null;
		
		try {
			temp = new File(System.getProperty("java.io.tmpdir") + "/" + fileName);

			FileWriter fileWriter = new FileWriter(temp.getAbsoluteFile());

			BufferedWriter buffWriter = new BufferedWriter(fileWriter);

			buffWriter.write(content);
			buffWriter.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return temp;
	}

	/**
	 * Reads file from classpath location of current project
	 * 
	 * @param fileName
	 */
	private String readFile(String fileName) {

		BufferedReader br = null;
		InputStream inputStream = null;

		String readingText = "";

		try {
			String currentLine;

			inputStream = this.getClass().getClassLoader().getResourceAsStream(fileName);

			br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

			while ((currentLine = br.readLine()) != null) {
				// Platform independent line separator.
				readingText += currentLine + System.getProperty("line.separator");
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return readingText;
	}
	
	/**
	 * Creates a log and saves a setup detail entity to database with same
	 * content.
	 * 
	 * @author Caner Feyzullahoğlu <caner.feyzullahoglu@agem.com.tr>
	 * 
	 * @param setupResult
	 *            info about result of installation
	 * @param logType
	 *            enter "ERROR" for error type of log.
	 */
	private void logAndAddDetailEntity(String setupResult, String logType) {

		AhenkSetupResultDetail setupDetailResult = null;

		logger.info("Preparing entity object.");

		// Prepare entity object
		setupDetailResult = new AhenkSetupResultDetail(null, setupParams, ip, setupResult);

		logger.info("Entity object created.");

		// Select log type
		if ("ERROR".equals(logType)) {
			logger.error(setupResult);
		} else {
			logger.info(setupResult);
		}

		logger.info("Detail entity will be added to parent entity.");

		setupParams.addResultDetail(setupDetailResult);

		logger.info("Detail entity added successfully.");
	}

}
