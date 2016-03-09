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

	private AhenkSetupParameters setupParams;

	private String ip;

	private String username;

	private String password;

	private Integer port;

	private byte[] privateKey;

	private String passphrase;

	private byte[] debFileArray;
	
	private InstallMethod installMethod;

	public RunnableAhenkInstaller(String ip, String username, String password, Integer port, byte[] privateKey,
			String passphrase, byte[] debFileArray, InstallMethod installMethod, AhenkSetupParameters setupParams) {
		super();
		this.ip = ip;
		this.username = username;
		this.password = password;
		this.port = port;
		this.privateKey = privateKey;
		this.passphrase = passphrase;
		this.debFileArray = debFileArray;
		this.installMethod = installMethod;
		this.setupParams = setupParams;
	}

	@Override
	public void run() {
		logger.warn("Runnable started.");
		try {
			logger.warn("Checking SSH authentication to: " + ip);
			
			// Check authorization before starting installation
			final boolean canConnect = SetupUtils.canConnectViaSsh(ip, username, password, port, privateKey,
					passphrase);
			logger.warn("canConnect = " + (canConnect == true ? "true" : "false"));

			// If we can connect to machine install Ahenk
			if (canConnect) {
				logger.warn("Authentication successfull for: " + ip);

				// Check installation method
				if (installMethod == InstallMethod.APT_GET) {
					logger.warn("Installing package by APT-GET to: " + ip);

					// TODO gedit değiştirilecek
					SetupUtils.installPackage(ip, username, password, port, privateKey, passphrase, "gedit", null);
					
				} else if (installMethod == InstallMethod.PROVIDED_DEB) {
					logger.warn("Converting byte array to deb file.");

					File debPackage = byteArrayToFile(debFileArray, "ahenk.deb");

					logger.warn("Installing package from DEB package to: " + ip);

					SetupUtils.installPackage(ip, username, password, port, privateKey, passphrase, debPackage);

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

		logger.warn("Preparing entity object.");
		
		// Prepare entity object
		setupDetailResult = new AhenkSetupResultDetail(null, setupParams, ip, setupResult);

		logger.warn("Entity object created.");
		
		// Select log type
		if ("ERROR".equals(logType)) {
			logger.error(setupResult);
		} else {
			logger.info(setupResult);
		}

		logger.warn("Detail entity will be added to parent entity.");

		setupParams.addResultDetail(setupDetailResult);
		
		logger.warn("Detail entity added successfully.");
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
