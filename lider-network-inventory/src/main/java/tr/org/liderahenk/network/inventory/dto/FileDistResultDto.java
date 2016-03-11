package tr.org.liderahenk.network.inventory.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Data transfer object class for file distribution results.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 * @see tr.org.liderahenk.network.inventory.entities.FileDistResult
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileDistResultDto implements Serializable {

	private static final long serialVersionUID = 7527517152725287797L;

	private ArrayList<String> ipAddresses;

	private String fileName;

	private String username;

	private String password;

	private Integer port;

	private String privateKey;

	private String destDirectory;

	private Date fileDistDate;

	private List<FileDistResultHostDto> hosts;

	public FileDistResultDto() {
		super();
	}

	public FileDistResultDto(ArrayList<String> ipAddresses, String fileName, String username, String password,
			Integer port, String privateKey, String destDirectory, Date fileDistDate,
			List<FileDistResultHostDto> hosts) {
		super();
		this.ipAddresses = ipAddresses;
		this.fileName = fileName;
		this.username = username;
		this.password = password;
		this.port = port;
		this.privateKey = privateKey;
		this.destDirectory = destDirectory;
		this.fileDistDate = fileDistDate;
		this.hosts = hosts;
	}

	public ArrayList<String> getIpAddresses() {
		return ipAddresses;
	}

	public void setIpAddresses(ArrayList<String> ipAddresses) {
		this.ipAddresses = ipAddresses;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getDestDirectory() {
		return destDirectory;
	}

	public void setDestDirectory(String destDirectory) {
		this.destDirectory = destDirectory;
	}

	public Date getFileDistDate() {
		return fileDistDate;
	}

	public void setFileDistDate(Date fileDistDate) {
		this.fileDistDate = fileDistDate;
	}

	public List<FileDistResultHostDto> getHosts() {
		return hosts;
	}

	public void setHosts(List<FileDistResultHostDto> hosts) {
		this.hosts = hosts;
	}

}
