package tr.org.liderahenk.network.inventory.dto;

import java.io.Serializable;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import tr.org.liderahenk.network.inventory.contants.Constants.AccessMethod;
import tr.org.liderahenk.network.inventory.contants.Constants.InstallMethod;

/**
 * Data transfer object class for Ahenk installation results.
 * 
 * @author <a href="mailto:caner.feyzullahoglu@agem.com.tr">Caner
 *         Feyzullahoğlu</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AhenkSetupDto implements Serializable {

	private static final long serialVersionUID = -8492968679338346240L;

	private List<String> ipList;

	private AccessMethod accessMethod;

	private String username;

	private String password;

	private String privateKey;

	private String passphrase;

	private InstallMethod installMethod;

	private Integer port;

	private List<AhenkSetupDetailDto> setupDetailList;

	public AhenkSetupDto() {
		super();
	}

	public AhenkSetupDto(List<String> ipList, AccessMethod accessMethod, String username, String password,
			String privateKey, String passphrase, InstallMethod installMethod, Integer port,
			List<AhenkSetupDetailDto> setupDetailList) {
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

	public List<AhenkSetupDetailDto> getSetupDetailList() {
		return setupDetailList;
	}

	public void setSetupDetailList(List<AhenkSetupDetailDto> setupDetailList) {
		this.setupDetailList = setupDetailList;
	}
}
