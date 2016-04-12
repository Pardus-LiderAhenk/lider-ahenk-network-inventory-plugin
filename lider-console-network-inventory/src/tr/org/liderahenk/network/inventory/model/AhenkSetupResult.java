package tr.org.liderahenk.network.inventory.model;

import java.io.Serializable;
import java.util.List;

import tr.org.liderahenk.network.inventory.constants.AccessMethod;
import tr.org.liderahenk.network.inventory.constants.InstallMethod;

/**
 * A model class that keeps the results and parameters of an Ahenk installation
 * command.
 * 
 * @author <a href="mailto:caner.feyzullahoglu@agem.com.tr">Caner
 *         Feyzullahoğlu</a>
 */
public class AhenkSetupResult implements Serializable {

	private static final long serialVersionUID = 2643258088727846728L;

	private List<String> ipList;

	private AccessMethod accessMethod;

	private String username;

	private String password;

	private String privateKey;

	private String passphrase;

	private InstallMethod installMethod;

	private Integer port;

	private List<AhenkSetupResultDetail> setupDetailList;

	public AhenkSetupResult() {
		super();
	}

	public AhenkSetupResult(List<String> ipList, AccessMethod accessMethod, String username, String password,
			String privateKey, String passphrase, InstallMethod installMethod, Integer port,
			List<AhenkSetupResultDetail> setupDetailList) {
		super();
		this.ipList = ipList;
		this.accessMethod = accessMethod;
		this.username = username;
		this.password = password;
		this.privateKey = privateKey;
		this.passphrase = passphrase;
		this.installMethod = installMethod;
		this.port = port;
		this.setupDetailList = setupDetailList;
	}

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

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public List<AhenkSetupResultDetail> getSetupDetailList() {
		return setupDetailList;
	}

	public void setSetupDetailList(List<AhenkSetupResultDetail> setupDetailList) {
		this.setupDetailList = setupDetailList;
	}

}
