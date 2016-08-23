package tr.org.liderahenk.network.inventory.utils.setup;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.network.inventory.contants.Constants.PackageInstaller;
import tr.org.liderahenk.network.inventory.exception.CommandExecutionException;
import tr.org.liderahenk.network.inventory.exception.SSHConnectionException;
import tr.org.liderahenk.network.inventory.utils.StringUtils;
import tr.org.liderahenk.network.inventory.utils.network.NetworkUtils;

/**
 * Utility class which provides common command execution methods (such as
 * installing/un-installing a package, checking version of a package etc.)
 * locally or remotely
 *
 * @author Emre Akkaya <emre.akkaya@agem.com.tr>
 *
 */
public class SetupUtils {

	private static final Logger logger = LoggerFactory.getLogger(SetupUtils.class);

	/**
	 * Install package via apt-get
	 */
	private static final String INSTALL_PACKAGE_FROM_REPO_CMD = "apt-get install -y --force-yes {0}={1}";

	/**
	 * Install package via apt-get (without version)
	 */
	private static final String INSTALL_PACKAGE_FROM_REPO_CMD_WITHOUT_VERSION = "apt-get install -y --force-yes {0}";

	/**
	 * Install given package via dpkg
	 */
	private static final String INSTALL_PACKAGE = "dpkg -i {0}";

	/**
	 * Download file with its default file name on the server from provided URL.
	 * Downloaded file will be in /tmp/{0} folder.
	 */
	private static final String DOWNLOAD_PACKAGE = "wget ‐‐directory-prefix=/tmp/{0}/ {1}";

	/**
	 * Download file with provided file name from provided URL. Downloaded file
	 * will be in /tmp/{0} folder.
	 */
	private static final String DOWNLOAD_PACKAGE_WITH_FILENAME = "wget --output-document=/tmp/{0}/{1} {2}";

	private static final String INSTALL_PACKAGE_GDEBI = "gdebi -n {0}";
	
	private static final String INSTALL_PACKAGE_GDEBI_WITH_OPTS = "gdebi -n -o {0} {1}";
	
	private static final String INSTALL_GDEBI = "apt-get install -y gdebi";
	
	/**
	 * Tries to connect via SSH. If password parameter is null, then it tries to
	 * connect via SSH key
	 * 
	 * @param ip
	 * @param username
	 * @param password
	 * @return true if an SSH connection can be established successfully, false
	 *         otherwise
	 */
	public static boolean canConnectViaSsh(final String ip, final String username, final String password,
			final Integer port, final String privateKey, final String passphrase) {
		logger.info("Started executing canConnectViaSsh");

		SSHManager manager = null;

		boolean connected = true;
		try {
			manager = new SSHManager(ip, username == null ? "root" : username, password, port, privateKey, passphrase);
			manager.connect();
			logger.info("Connection established to: {} with username: {}", new Object[] { ip, username });
		} catch (SSHConnectionException e) {
			logger.error(e.getMessage(), e);
			connected = false;
		} finally {
			try {
				if (manager != null) {
					manager.disconnect();
				}
			} catch (Exception e2) {
				logger.warn("Unimportant exception while manager class disconnects (it does not affect process).");
				logger.warn("Unimportant Exception Message: " + e2.getMessage());

				return connected;
			}
		}

		return connected;
	}

	/**
	 * Installs a package which specified by package name and version. Before
	 * calling this method, package existence should be ensured by calling
	 * packageExists() method.
	 * 
	 * @param ip
	 * @param username
	 * @param password
	 * @param port
	 * @param privateKey
	 * @param packageName
	 * @param version
	 * @throws SSHConnectionException
	 * @throws CommandExecutionException
	 */
	public static void installPackage(final String ip, final String username, final String password, final Integer port,
			final String privateKey, final String passphrase, final String packageName, final String version)
					throws SSHConnectionException, CommandExecutionException {
		if (NetworkUtils.isLocal(ip)) {

			logger.debug("Installing package locally.");

			try {

				String command;
				String logMessage;

				// If version is not given
				if (version == null || version.isEmpty()) {
					command = INSTALL_PACKAGE_FROM_REPO_CMD_WITHOUT_VERSION.replace("{0}", packageName);
					logMessage = "Package {0} installed successfully";
				} else {
					command = INSTALL_PACKAGE_FROM_REPO_CMD.replace("{0}", packageName).replace("{1}", version);
					logMessage = "Package {0}:{1} installed successfully";
				}

				Process process = Runtime.getRuntime().exec(command);

				int exitValue = process.waitFor();
				if (exitValue != 0) {
					logger.error("Process ends with exit value: {} - err: {}",
							new Object[] { process.exitValue(), StringUtils.convertStream(process.getErrorStream()) });
					throw new CommandExecutionException("Failed to execute command: " + command);
				}
				if (version == null || "".equals(version)) {
					logger.info(logMessage, new Object[] { packageName, version });
				} else {
					logger.info(logMessage, new Object[] { packageName });
				}

			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		} else {
			logger.info("Installing package remotely on: {} with username: {}", new Object[] { ip, username });

			SSHManager manager = new SSHManager(ip, username == null ? "root" : username, password, port, privateKey,
					passphrase);
			manager.connect();

			// If version is not given
			if (version == null || "".equals(version)) {
				manager.execCommand(INSTALL_PACKAGE_FROM_REPO_CMD_WITHOUT_VERSION, new Object[] { packageName });
				logger.info("Package {} installed successfully", new Object[] { packageName });
			} else {
				manager.execCommand(INSTALL_PACKAGE_FROM_REPO_CMD, new Object[] { packageName, version });
				logger.info("Package {}:{} installed successfully", new Object[] { packageName, version });
			}
			manager.disconnect();
		}

	}

	/**
	 * Installs a deb package file. This can be used when a specified deb
	 * package is already provided
	 * 
	 * @param ip
	 * @param username
	 * @param password
	 * @param port
	 * @param privateKey
	 * @param debPackage
	 * @param packageInstaller
	 * @throws SSHConnectionException
	 * @throws CommandExecutionException
	 */
	public static void installPackage(final String ip, final String username, final String password, final Integer port,
			final String privateKey, final String passphrase, final File debPackage, final PackageInstaller packageInstaller)
					throws SSHConnectionException, CommandExecutionException {
		
		String command;
		
		if (packageInstaller == PackageInstaller.DPKG) {
			command = INSTALL_PACKAGE.replace("{0}", "/tmp/" + debPackage.getName());
		} else {
			command = INSTALL_PACKAGE_GDEBI.replace("{0}", "/tmp/" + debPackage.getName());
		}
		
		if (NetworkUtils.isLocal(ip)) {

			logger.debug("Installing package locally.");

			try {

				copyFile(ip, username, password, port, privateKey, passphrase, debPackage, "/tmp/");

				Process process = Runtime.getRuntime().exec(command);

				int exitValue = process.waitFor();
				if (exitValue != 0) {
					logger.error("Process ends with exit value: {} - err: {}",
							new Object[] { process.exitValue(), StringUtils.convertStream(process.getErrorStream()) });
					throw new CommandExecutionException("Failed to execute command: " + command);
				}

				logger.info("Package {} installed successfully", debPackage.getName());

			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		} else {

			logger.debug("Installing package remotely on: {} with username: {}", new Object[] { ip, username });

			copyFile(ip, username, password, port, privateKey, passphrase, debPackage, "/tmp/");

			SSHManager manager = new SSHManager(ip, username == null ? "root" : username, password, port, privateKey,
					passphrase);
			manager.connect();
			manager.execCommand(command, new Object[] {});
			manager.disconnect();

			logger.info("Package {} installed successfully", debPackage.getName());
		}
	}

	/**
	 * Installs a deb package which has been downloaded before by
	 * downloadPackage method. It searches the file in /tmp/{tmpDir} folder.
	 * 
	 * @param ip
	 * @param username
	 * @param password
	 * @param port
	 * @param privateKey
	 * @param passphrase
	 * @param tmpDir
	 * @param filename
	 * @param packageInstaller
	 * @throws SSHConnectionException
	 * @throws CommandExecutionException
	 */
	public static void installDownloadedPackage(final String ip, final String username, final String password,
			final Integer port, final String privateKey, final String passphrase, final String tmpDir, final String filename, final PackageInstaller packageInstaller)
					throws SSHConnectionException, CommandExecutionException {
		
		String command;

		if (packageInstaller == PackageInstaller.DPKG) {
			// Prepare command
			if (!"".equals(filename)) {
				command = INSTALL_PACKAGE.replace("{0}", "/tmp/" + tmpDir + "/" + filename);
			} else {
				command = INSTALL_PACKAGE.replace("{0}", "/tmp/" + tmpDir + "/*.deb");
			}
		} else {
			if (!"".equals(filename)) {
				command = INSTALL_PACKAGE_GDEBI.replace("{0}", "/tmp/" + tmpDir + "/" + filename);
			} else {
				command = INSTALL_PACKAGE_GDEBI.replace("{0}", "/tmp/" + tmpDir + "/*.deb");
			}
		}

		if (NetworkUtils.isLocal(ip)) {

			logger.debug("Installing package locally.");

			try {


				Process process = Runtime.getRuntime().exec(command);

				int exitValue = process.waitFor();
				if (exitValue != 0) {
					logger.error("Process ends with exit value: {} - err: {}",
							new Object[] { process.exitValue(), StringUtils.convertStream(process.getErrorStream()) });
					throw new CommandExecutionException("Failed to execute command: " + command);
				}

				logger.info("Package {} installed successfully", filename);

			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		} else {

			logger.debug("Installing package remotely on: {} with username: {}", new Object[] { ip, username });

			SSHManager manager = new SSHManager(ip, username == null ? "root" : username, password, port, privateKey,
					passphrase);
			manager.connect();
			manager.execCommand(command, new Object[] {});
			manager.disconnect();

			logger.info("Package {} installed successfully", filename);
		}
	}

	public static String replace(Map<String, String> map, String text) {
		for (Entry<String, String> entry : map.entrySet()) {
			text = text.replaceAll(entry.getKey().replaceAll("#", "\\#"), entry.getValue());
		}
		return text;
	}
	
	/**
	 * 
	 * @param ip
	 * @param username
	 * @param password
	 * @param port
	 * @param privateKey
	 * @param passphrase
	 * @param fileToTranster
	 * @param destDirectory
	 * @throws SSHConnectionException
	 * @throws CommandExecutionException
	 */
	public static void copyFile(final String ip, final String username, final String password, final Integer port,
			final String privateKey, final String passphrase, final File fileToTranster, final String destDirectory)
					throws SSHConnectionException, CommandExecutionException {
		String destinationDir = destDirectory;
		if (!destinationDir.endsWith("/")) {
			destinationDir += "/";
		}

		logger.info("Copying file to: {0} with username: {1}", new Object[] { ip, username });

		SSHManager manager = new SSHManager(ip, username == null ? "root" : username, password, port, privateKey,
				passphrase);
		manager.connect();
		manager.copyFileToRemote(fileToTranster, destinationDir, false);
		manager.disconnect();

		logger.info("File {0} copied successfully", fileToTranster.getName());
	}

	/**
	 * Downloads a file from given URL to given machine. It creates another
	 * folder with provided name under /tmp to prevent duplication of files.
	 * (e.g.: If tmpDir parameter is given as "ahenkTmpDir" then downloaded file
	 * will be under /tmp/ahenkTmpDir/ folder.)
	 * 
	 * @param ip
	 * @param username
	 * @param password
	 * @param port
	 * @param privateKey
	 * @param passphrase
	 * @param filename
	 * @param downloadUrl
	 * @throws SSHConnectionException
	 * @throws CommandExecutionException
	 */
	public static void downloadPackage(final String ip, final String username, final String password,
			final Integer port, final String privateKey, final String passphrase, final String tmpDir, final String filename,
			final String downloadUrl) throws SSHConnectionException, CommandExecutionException {
		
		String command;
		
		if (filename == null || "".equals(filename)) {
			command = DOWNLOAD_PACKAGE.replace("{0}", tmpDir).replace("{1}", downloadUrl);
		} else {
			command = DOWNLOAD_PACKAGE_WITH_FILENAME.replace("{0}", tmpDir).replace("{1}", filename).replace("{2}", downloadUrl);
		}

		if (NetworkUtils.isLocal(ip)) {

			logger.info("Executing command locally.");

			try {

				Process process = Runtime.getRuntime().exec(command);

				int exitValue = process.waitFor();
				if (exitValue != 0) {
					logger.info("Process ends with exit value: {0} - err: {1}",
							new Object[] { process.exitValue(), StringUtils.convertStream(process.getErrorStream()) });
					throw new CommandExecutionException("Failed to execute command: " + command);
				}

				logger.info("Command: '{0}' executed successfully.", new Object[] { command });

			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		} else {
			logger.info("Executing command remotely on: {0} with username: {1}", new Object[] { ip, username });

			SSHManager manager = new SSHManager(ip, username == null ? "root" : username, password, port, privateKey,
					passphrase);
			manager.connect();

			manager.execCommand(command, new Object[] {});
			logger.info("Command: '{0}' executed successfully.",
					new Object[] { DOWNLOAD_PACKAGE.replace("{0}", filename).replace("{1}", downloadUrl) });

			manager.disconnect();
		}

	}
	
	/**
	 * 
	 * Executes a command on the given machine.
	 * 
	 * @param ip
	 * @param username
	 * @param password
	 * @param port
	 * @param privateKey
	 * @param passphrase
	 * @param command
	 * @throws SSHConnectionException
	 * @throws CommandExecutionException
	 */
	public static void executeCommand(final String ip, final String username, final String password, final Integer port,
			final String privateKey, final String passphrase, final String command)
					throws SSHConnectionException, CommandExecutionException {
		if (NetworkUtils.isLocal(ip)) {

			logger.info("Executing command locally.");

			try {

				Process process = Runtime.getRuntime().exec(command);

				int exitValue = process.waitFor();
				if (exitValue != 0) {
					logger.error("Process ends with exit value: {0} - err: {1}",
							new Object[] { process.exitValue(), StringUtils.convertStream(process.getErrorStream()) });
					throw new CommandExecutionException("Failed to execute command: " + command);
				}
				
				logger.info("Command: '{0}' executed successfully.", new Object[] { command });

			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		} else {
			logger.info("Executing command remotely on: {0} with username: {1}",
					new Object[] { ip, username });

			SSHManager manager = new SSHManager(ip, username == null ? "root" : username, password, port, privateKey, passphrase);
			manager.connect();

			manager.execCommand(command, new Object[] {});
			logger.info("Command: '{0}' executed successfully.", new Object[] { command });

			manager.disconnect();
		}

	}
	
	/**
	 * Installs a deb package file via Gdebi non-interactively by using given
	 * DPKG or APT options. This can be used when a specified deb package is
	 * already provided
	 * 
	 * @param ip
	 * @param username
	 * @param password
	 * @param port
	 * @param privateKey
	 * @param passphrase
	 * @param debPackagePath
	 * @param dpkgOpts
	 * @throws SSHConnectionException
	 * @throws CommandExecutionException
	 */
	public static void installPackageGdebiWithOpts(final String ip, final String username, final String password,
			final Integer port, final String privateKey, final String passphrase, final String debPackagePath,
			final String dpkgOpts) throws SSHConnectionException, CommandExecutionException {

		logger.info("Installing package remotely on: {0} with username: {1}", new Object[] { ip, username });

		String command;

		SSHManager manager = new SSHManager(ip, username == null ? "root" : username, password, port, privateKey,
				passphrase);

		manager.connect();
		
		// Add given options and deb package.
		command = INSTALL_PACKAGE_GDEBI_WITH_OPTS.replace("{0}", dpkgOpts).replace("{1}", debPackagePath);
		manager.execCommand(INSTALL_GDEBI, new Object[] {});

		manager.execCommand(command, new Object[] {});
		manager.disconnect();

		logger.info("Package {0} installed successfully", debPackagePath);
	}

}