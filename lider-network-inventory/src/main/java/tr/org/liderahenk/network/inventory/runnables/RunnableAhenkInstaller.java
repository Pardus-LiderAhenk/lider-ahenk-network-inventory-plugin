package tr.org.liderahenk.network.inventory.runnables;

import java.io.File;
import java.io.FileOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.lider.core.api.plugin.IPluginDbService;
import tr.org.liderahenk.network.inventory.contants.Constants.InstallMethod;
import tr.org.liderahenk.network.inventory.entities.AhenkSetupParameters;
import tr.org.liderahenk.network.inventory.entities.AhenkSetupResultDetail;
import tr.org.liderahenk.network.inventory.exception.CommandExecutionException;
import tr.org.liderahenk.network.inventory.exception.SSHConnectionException;
import tr.org.liderahenk.network.inventory.utils.setup.SetupUtils;

public class RunnableAhenkInstaller implements Runnable {

	private Logger logger = LoggerFactory.getLogger(RunnableAhenkInstaller.class);

	private IPluginDbService pluginDbService;

	private AhenkSetupParameters setupResult;

	private AhenkSetupResultDetail setupDetailResult;

	private String ip;

	private String username;

	private String password;

	private Integer port;

	private byte[] privateKey;

	private String passphrase;

	private byte[] debFileArray;

	private InstallMethod installMethod;

	public RunnableAhenkInstaller(String ip, String username, String password, Integer port, byte[] privateKey,
			String passphrase, byte[] debFileArray, InstallMethod installMethod) {
		super();
		this.ip = ip;
		this.username = username;
		this.password = password;
		this.port = port;
		this.privateKey = privateKey;
		this.passphrase = passphrase;
		this.debFileArray = debFileArray;
		this.installMethod = installMethod;
	}

	@Override
	public void run() {
		try {
			logger.debug("Checking SSH authentication to: " + ip);
			
			// Check authorization before starting installation
			final boolean canConnect = SetupUtils.canConnectViaSsh(ip, username, password, port, privateKey,
					passphrase);

			// If we can connect to machine install Ahenk
			if (canConnect) {
				logger.debug("Authentication successfull for: " + ip);

				// Check installation method
				if (installMethod == InstallMethod.APT_GET) {
					logger.debug("Installing package by APT-GET to: " + ip);
					
					// TODO gedit değiştirilecek
					SetupUtils.installPackage(ip, username, password, port, privateKey, "gedit", null);
					
				} else if (installMethod == InstallMethod.PROVIDED_DEB) {
					logger.debug("Converting byte array to deb file.");

					File debPackage = byteArrayToFile(debFileArray, "ahenk.deb");

					logger.debug("Installing package from DEB package to: " + ip);

					SetupUtils.installPackage(ip, username, password, port, privateKey, passphrase, debPackage);

				} else {
					logAndSaveDetailEntity("Installation method is not set or not selected. Installation cancelled.",
							"ERROR");
				}
				logAndSaveDetailEntity("Successfully installed to: " + ip, "INFO");
			} else {
				logAndSaveDetailEntity("Could not connect to: " + ip + " passing over to another IP.", "ERROR");
			}

		} catch (SSHConnectionException e) {
			logAndSaveDetailEntity("Error occured installing Ahenk on IP: " + ip + " Error message: " + e.getMessage(),
					"ERROR");
			e.printStackTrace();
		} catch (CommandExecutionException e) {
			logAndSaveDetailEntity("Error occured installing Ahenk on IP: " + ip + " Error message: " + e.getMessage(),
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
	private void logAndSaveDetailEntity(String setupResult, String logType) {

		AhenkSetupResultDetail setupDetailResult = null;

		logger.debug("Preparing entity object.");
		
		// Prepare entity object
		setupDetailResult = new AhenkSetupResultDetail(null, ip, setupResult);

		logger.debug("Entity object created.");
		
		// Select log type
		if ("ERROR".equals(logType)) {
			logger.error(setupResult);
		} else {
			logger.info(setupResult);
		}

		logger.debug("Detail entity will be saved.");

		pluginDbService.save(setupDetailResult);
		
		logger.debug("Detail entity saved successfully.");
	}

	/**
	 * Creates a temporary file from an array of bytes.
	 * 
	 * @author Caner Feyzullahoğlu <caner.feyzullahoglu@agem.com.tr>
	 * 
	 * @param contents
	 * 
	 * @param filename
	 * 
	 * @return File
	 */
	private File byteArrayToFile(byte[] content, String filename) {

		FileOutputStream fileOutputStream = null;

		File temp = null;

		try {

			fileOutputStream = new FileOutputStream(temp);

			temp = File.createTempFile(filename, "");
			// Delete temp file when program exits.
			temp.deleteOnExit();

			// Write to temp file
			fileOutputStream.write(content);
			fileOutputStream.close();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return temp;
	}

}
