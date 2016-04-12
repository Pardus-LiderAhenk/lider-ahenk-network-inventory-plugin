package tr.org.liderahenk.network.inventory.runnables;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.network.inventory.dto.FileDistResultDto;
import tr.org.liderahenk.network.inventory.dto.FileDistResultHostDto;
import tr.org.liderahenk.network.inventory.utils.setup.SetupUtils;

/**
 * A runnable that is responsible of distributing provided file to given
 * machines.
 * 
 * @author <a href="mailto:caner.feyzullahoglu@agem.com.tr">Caner
 *         Feyzullahoğlu</a>
 */
public class RunnableFileDistributor implements Runnable {

	private Logger logger = LoggerFactory.getLogger(RunnableFileDistributor.class);

	private FileDistResultDto fileDistResultDto;
	private List<String> ipList;
	private String username;
	private String password;
	private Integer port;
	private String privateKey;
	private String passphrase;
	private File fileToTransfer;
	private String destDirectory;

	public RunnableFileDistributor(FileDistResultDto fileDistResultDto, List<String> ipList, String username,
			String password, Integer port, String privateKey, String passphrase, File fileToTransfer,
			String destDirectory) {
		this.fileDistResultDto = fileDistResultDto;
		this.ipList = ipList;
		this.username = username;
		this.password = password;
		this.port = port;
		this.privateKey = privateKey;
		this.passphrase = passphrase;
		this.fileToTransfer = fileToTransfer;
		this.destDirectory = destDirectory;
	}

	@Override
	public void run() {

		logger.debug("ipList.size: " + ipList.size());
		for (String ip : ipList) {
			FileDistResultHostDto hostDto = null;
			try {
				logger.debug("Copying file to: " + ip);
				SetupUtils.copyFile(ip, username, password, port, privateKey, passphrase, fileToTransfer,
						destDirectory);
				hostDto = new FileDistResultHostDto(ip, true, null);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				hostDto = new FileDistResultHostDto(ip, false, e.getMessage());
			}
			fileDistResultDto.getHosts().add(hostDto);
		}
	}

	@Override
	public String toString() {
		return "RunnableFileDistributor [ipList=" + ipList + ", username=" + username + ", password=" + password
				+ ", port=" + port + ", privateKey=" + privateKey + ", fileToTransfer=" + fileToTransfer
				+ ", destDirectory=" + destDirectory + "]";
	}

}
