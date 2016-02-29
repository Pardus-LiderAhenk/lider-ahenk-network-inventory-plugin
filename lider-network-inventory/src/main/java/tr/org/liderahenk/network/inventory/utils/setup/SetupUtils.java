package tr.org.liderahenk.network.inventory.utils.setup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
			final Integer port, final byte[] privateKey, final String passphrase) {
		SSHManager manager = null;
		try {
			manager = new SSHManager(ip, username == null ? "root" : username, password, port, privateKey, passphrase);
			manager.connect();
			logger.info("Connection established to: {} with username: {}", new Object[] { ip, username });
			return true;
		} catch (SSHConnectionException e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (manager != null) {
				manager.disconnect();
			}
		}
		return false;
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
			final byte[] privateKey, final String passphrase, final String packageName, final String version)
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
			try {
				logger.info("Installing package remotely on: {} with username: {}",
						new Object[] { ip, username });

				SSHManager manager = new SSHManager(ip, username == null ? "root" : username, password, port, privateKey, passphrase);
				manager.connect();

				// If version is not given
				if (version == null || "".equals(version)) {
					manager.execCommand(INSTALL_PACKAGE_FROM_REPO_CMD_WITHOUT_VERSION, new Object[] { packageName });
					logger.info("Package {} installed successfully", new Object[] { packageName });
				} else {
					manager.execCommand(INSTALL_PACKAGE_FROM_REPO_CMD, new Object[] { packageName, version });
					logger.info("Package {}:{} installed successfully",
							new Object[] { packageName, version });
				}
				manager.disconnect();

			} catch (SSHConnectionException e) {
				e.printStackTrace();
			}
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
	 * @throws SSHConnectionException
	 * @throws CommandExecutionException
	 */
	public static void installPackage(final String ip, final String username, final String password, final Integer port,
			final byte[] privateKey, final String passphrase, final File debPackage) throws SSHConnectionException, CommandExecutionException {
		if (NetworkUtils.isLocal(ip)) {

			logger.debug("Installing package locally.");

			try {

				copyFile(ip, username, password, port, privateKey, passphrase, debPackage, "/tmp/");

				String command = INSTALL_PACKAGE.replace("{0}", "/tmp/" + debPackage.getName());
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

			logger.debug("Installing package remotely on: {} with username: {}",
					new Object[] { ip, username });

			copyFile(ip, username, password, port, privateKey, passphrase, debPackage, "/tmp/");

			SSHManager manager = new SSHManager(ip, username == null ? "root" : username, password, port, privateKey, passphrase);
			manager.connect();
			manager.execCommand(INSTALL_PACKAGE, new Object[] { "/tmp/" + debPackage.getName() });
			manager.disconnect();

			logger.info("Package {} installed successfully", debPackage.getName());
		}
	}

	/**
	 * 
	 * @param ip
	 * @param username
	 * @param password
	 * @param port
	 * @param privateKey
	 * @param fileToTransfer
	 * @param destDirectory
	 * @throws SSHConnectionException
	 * @throws CommandExecutionException
	 */
	public static void copyFile(final String ip, final String username, final String password, final Integer port,
			final byte[] privateKey, final String passphrase, final File fileToTransfer, final String destDirectory)
					throws SSHConnectionException, CommandExecutionException {
		if (NetworkUtils.isLocal(ip)) {

			String destinationDir = destDirectory;
			if (!destinationDir.endsWith("/")) {
				destinationDir += "/";
			}
			destinationDir += fileToTransfer.getName();

			logger.debug("Copying file to: {}", destinationDir);

			InputStream in = null;
			OutputStream out = null;

			try {

				in = new FileInputStream(fileToTransfer);
				out = new FileOutputStream(destinationDir);

				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}

				logger.info("File {0} copied successfully", fileToTransfer.getName());

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (out != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		} else {

			logger.debug("Copying file to: {} with username: {}", new Object[] { ip, username });

			SSHManager manager = new SSHManager(ip, username == null ? "root" : username, password, port, privateKey, passphrase);
			manager.connect();
			manager.copyFileToRemote(fileToTransfer, destDirectory, false);
			manager.disconnect();

			logger.info("File {} copied successfully", fileToTransfer.getName());
		}
	}

}