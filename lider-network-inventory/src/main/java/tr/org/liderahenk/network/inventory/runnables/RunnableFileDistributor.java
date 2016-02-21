package tr.org.liderahenk.network.inventory.runnables;

import java.io.File;
import java.util.List;

import tr.org.liderahenk.network.inventory.exception.CommandExecutionException;
import tr.org.liderahenk.network.inventory.exception.SSHConnectionException;
import tr.org.liderahenk.network.inventory.utils.setup.SetupUtils;

public class RunnableFileDistributor implements Runnable {

	private List<String> ipList;
	private String username;
	private String password;
	private Integer port;
	private String privateKey;
	private File fileToTransfer;
	private String destDirectory;

	// TODO pass also logService here:
	public RunnableFileDistributor(List<String> ipList, String username, String password, Integer port,
			String privateKey, File fileToTransfer, String destDirectory) {
		this.ipList = ipList;
		this.username = username;
		this.password = password;
		this.port = port;
		this.privateKey = privateKey;
		this.fileToTransfer = fileToTransfer;
		this.destDirectory = destDirectory;
	}

	@Override
	public void run() {
		for (String ip : ipList) {
			// TODO log if action is succesfull or not...
			try {
				SetupUtils.copyFile(ip, username, password, port, privateKey, fileToTransfer, destDirectory);
			} catch (SSHConnectionException e) {
				e.printStackTrace();
			} catch (CommandExecutionException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public String toString() {
		return "RunnableFileDistributor [ipList=" + ipList + ", username=" + username + ", password=" + password
				+ ", port=" + port + ", privateKey=" + privateKey + ", fileToTransfer=" + fileToTransfer
				+ ", destDirectory=" + destDirectory + "]";
	}

}
