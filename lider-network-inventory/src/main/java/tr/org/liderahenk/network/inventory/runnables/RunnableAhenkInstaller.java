package tr.org.liderahenk.network.inventory.runnables;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.network.inventory.contants.Constants.InstallMethod;
import tr.org.liderahenk.network.inventory.contants.Constants.PackageInstaller;
import tr.org.liderahenk.network.inventory.entities.AhenkSetupParameters;
import tr.org.liderahenk.network.inventory.entities.AhenkSetupResultDetail;
import tr.org.liderahenk.network.inventory.exception.CommandExecutionException;
import tr.org.liderahenk.network.inventory.exception.SSHConnectionException;
import tr.org.liderahenk.network.inventory.utils.setup.SetupUtils;

public class RunnableAhenkInstaller implements Runnable {

	private Logger logger = LoggerFactory.getLogger(RunnableAhenkInstaller.class);

	private AhenkSetupParameters setupParams;

	private String ip;

	private String username;

	private String password;

	private Integer port;

	private String privateKey;

	private String passphrase;

	private InstallMethod installMethod;
	
	private String downloadUrl;
	
	private final static String MAKE_DIR_UNDER_TMP = "mkdir /tmp/{0}"; 

	public RunnableAhenkInstaller(String ip, String username, String password, Integer port, String privateKey,
			String passphrase, InstallMethod installMethod, String downloadUrl, AhenkSetupParameters setupParams) {
		super();
		this.ip = ip;
		this.username = username;
		this.password = password;
		this.port = port;
		this.privateKey = privateKey;
		this.passphrase = passphrase;
		this.installMethod = installMethod;
		this.downloadUrl = downloadUrl;
		this.setupParams = setupParams;
	}

	@Override
	public void run() {
		logger.info("Runnable started.");
		try {
			logger.info("Checking SSH authentication to: " + ip);

			// Check authorization before starting installation
			final boolean canConnect = SetupUtils.canConnectViaSsh(ip, username, password, port, privateKey,
					passphrase);
			logger.info("canConnect = " + (canConnect == true ? "true" : "false"));

			// If we can connect to machine install Ahenk
			if (canConnect) {
				logger.info("Authentication successfull for: " + ip);

				// Check installation method
				if (installMethod == InstallMethod.APT_GET) {
					logger.info("Installing package by APT-GET to: " + ip);

					// TODO gedit değiştirilecek
					SetupUtils.installPackage(ip, username, password, port, privateKey, passphrase, "gedit", null);

				} else if (installMethod == InstallMethod.WGET) {
					
					// In case of folder name clash use current time as postfix
					Date date = new Date();
					SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy-HH:mm:ss");
					String timestamp = dateFormat.format(date);
					
					logger.info("Creating directory under /tmp");
					SetupUtils.executeCommand(ip, username, password, port, privateKey, passphrase, MAKE_DIR_UNDER_TMP.replace("{0}", "ahenkTmpDir" + timestamp));
					
					logger.info("Downloading file from URL: " + downloadUrl);
					SetupUtils.downloadPackage(ip, username, password, port, privateKey, passphrase, "ahenkTmpDir" + timestamp, "ahenk.deb", downloadUrl);
					
					logger.info("Installing downloaded package to: " + ip);
					SetupUtils.installDownloadedPackage(ip, username, password, port, privateKey, passphrase, "ahenkTmpDir" + timestamp, "ahenk.deb", PackageInstaller.DPKG);

				} else {
					logAndAddDetailEntity("Installation method is not set or not selected. Installation cancelled.",
							"ERROR");
				}
				logAndAddDetailEntity("Successfully installed to: " + ip, "INFO");
			} else {
				logAndAddDetailEntity("Could not connect to: " + ip + " passing over to another IP.", "ERROR");
			}

		} catch (SSHConnectionException e) {
			logAndAddDetailEntity("Error occured installing Ahenk on IP: " + ip + " Error message: " + e.getMessage(),
					"ERROR");
			e.printStackTrace();
		} catch (CommandExecutionException e) {
			logAndAddDetailEntity("Error occured installing Ahenk on IP: " + ip + " Error message: " + e.getMessage(),
					"ERROR");
			e.printStackTrace();
		}
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
