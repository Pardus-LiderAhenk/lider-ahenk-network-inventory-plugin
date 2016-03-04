package tr.org.liderahenk.network.inventory.dto;

import java.io.Serializable;
import java.util.List;

import tr.org.liderahenk.network.inventory.contants.Constants.AccessMethod;
import tr.org.liderahenk.network.inventory.contants.Constants.InstallMethod;

public class AhenkSetupDto implements Serializable {

	private static final long serialVersionUID = -8492968679338346240L;

	private List<String> ipList;
	
	private AccessMethod accessMethod;
	
	private String username;
	
	private String password;
	
	private byte[] privateKeyFile;
	
	private String passphrase;
	
	private InstallMethod installMethod;
	
	private byte[] debFile;
	
	private Integer port;

	public List<String> getIpList() {
		return ipList;
	}

	public void setIpList(List<String> ipList) {
		this.ipList = ipList;
	}

	public AccessMethod getAccessMethod() {
		return accessMethod;
	}

	public void setAccessMethod(AccessMethod accessMethod) {
		this.accessMethod = accessMethod;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public byte[] getPrivateKeyFile() {
		return privateKeyFile;
	}

	public void setPrivateKeyFile(byte[] privateKeyFile) {
		this.privateKeyFile = privateKeyFile;
	}

	public String getPassphrase() {
		return passphrase;
	}

	public void setPassphrase(String passphrase) {
		this.passphrase = passphrase;
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

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
