package tr.org.liderahenk.network.inventory.model;

import java.io.Serializable;
import java.util.List;

import tr.org.liderahenk.network.inventory.constants.AccessMethod;
import tr.org.liderahenk.network.inventory.constants.InstallMethod;

/**
 * Contains configuration variables used throughout the whole setup process.
 *
 * @author <a href="mailto:caner.feyzullahoglu@agem.com.tr">Caner
 *         Feyzullahoğlu</a>
 */
public class AhenkSetupConfig implements Serializable{

	private static final long serialVersionUID = 5285933884424740906L;

	/**
	 * IP list that Ahenk will be installed
	 */
	private List<String> ipList;

	/**
	 * AhenkSetupConnectionMethodPage variables (Cm: Connection Method)
	 */
	private AccessMethod accessMethod;
	private String username;
	private String password;
	private String privateKeyPath;
	private String passphrase;

	/**
	 * AhenkSetupInstallationMethodPage variables
	 */
	private InstallMethod installMethod;
	
	private byte[] debFile;

	private String downloadUrl;
	
	/**
	 * Port for SSH connection.
	 */
	private Integer port;
	
	// ------ Getter Setter ----- //

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

	public String getPassphrase() {
		return passphrase;
	}

	public void setPassphrase(String passphrase) {
		this.passphrase = passphrase;
	}

	public List<String> getIpList() {
		return ipList;
	}

	public void setIpList(List<String> ipList) {
		this.ipList = ipList;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public AccessMethod getAccessMethod() {
		return accessMethod;
	}

	public void setAccessMethod(AccessMethod accessMethod) {
		this.accessMethod = accessMethod;
	}

	public InstallMethod getInstallMethod() {
		return installMethod;
	}

	public void setInstallMethod(InstallMethod installMethod) {
		this.installMethod = installMethod;
	}

	public byte[] getDebFile() {
		return debFile;
	}

	public void setDebFile(byte[] debFile) {
		this.debFile = debFile;
	}

	public String getPrivateKeyPath() {
		return privateKeyPath;
	}

	public void setPrivateKeyPath(String privateKeyPath) {
		this.privateKeyPath = privateKeyPath;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

}
