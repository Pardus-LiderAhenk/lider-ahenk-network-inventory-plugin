package tr.org.liderahenk.network.inventory.model;

import java.util.List;

import tr.org.liderahenk.network.inventory.constants.AccessMethod;
import tr.org.liderahenk.network.inventory.constants.InstallMethod;

/**
 * Contains configuration variables used throughout the whole setup process.
 *
 */
public class AhenkSetupConfig {

	/**
	 * IP list that Ahenk will be installed
	 */
	private List<String> ipList;

	/**
	 * AhenkSetupLocationPage variables
	 */

	private boolean installAhenkLocally;
	private boolean performNetworkScanning;
	private boolean installOnGivenIps;

	/**
	 * AhenkSetupNetworkScanPage variables
	 */
	private boolean showSystemInfo;
	private String username;
	private String password;

	/**
	 * AhenkSetupConnectionMethodPage variables (Cm: Connection Method)
	 */
	private AccessMethod ahenkAccessMethod;
	private String usernameCm;
	private String passwordCm;
	private String privateKeyAbsPath;
	private String passphrase;

	/**
	 * AhenkSetupInstallationMethodPage variables
	 */
	private InstallMethod ahenkInstallMethod;
	private String debFileAbsPath;

	/**
	 * AhenkSetupDistributionMethodPage variables
	 */
	private boolean useScp;
	private boolean useTorrent;

	/**
	 * This variable is used to enable/disable finish button 
	 */
	private boolean isInstallationFinished = false;
	
	/**
	 * Port for SSH connection.
	 */
	private Integer port;
	
	// ------ Getter Setter ----- //
	public boolean isInstallAhenkLocally() {
		return installAhenkLocally;
	}

	public void setInstallAhenkLocally(boolean installAhenkLocally) {
		this.installAhenkLocally = installAhenkLocally;
	}

	public boolean isPerformNetworkScanning() {
		return performNetworkScanning;
	}

	public void setPerformNetworkScanning(boolean performNetworkScanning) {
		this.performNetworkScanning = performNetworkScanning;
	}

	public boolean isInstallOnGivenIps() {
		return installOnGivenIps;
	}

	public void setInstallOnGivenIps(boolean installOnGivenIps) {
		this.installOnGivenIps = installOnGivenIps;
	}

	public boolean isShowSystemInfo() {
		return showSystemInfo;
	}

	public void setShowSystemInfo(boolean showSystemInfo) {
		this.showSystemInfo = showSystemInfo;
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

	public String getUsernameCm() {
		return usernameCm;
	}

	public void setUsernameCm(String usernameCm) {
		this.usernameCm = usernameCm;
	}

	public String getPasswordCm() {
		return passwordCm;
	}

	public void setPasswordCm(String passwordCm) {
		this.passwordCm = passwordCm;
	}

	public String getPrivateKeyAbsPath() {
		return privateKeyAbsPath;
	}

	public void setPrivateKeyAbsPath(String privateKeyAbsPath) {
		this.privateKeyAbsPath = privateKeyAbsPath;
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

	public String getDebFileAbsPath() {
		return debFileAbsPath;
	}

	public void setDebFileAbsPath(String debFileAbsPath) {
		this.debFileAbsPath = debFileAbsPath;
	}

	public boolean isUseScp() {
		return useScp;
	}

	public void setUseScp(boolean useScp) {
		this.useScp = useScp;
	}

	public boolean isUseTorrent() {
		return useTorrent;
	}

	public void setUseTorrent(boolean useTorrent) {
		this.useTorrent = useTorrent;
	}

	public AccessMethod getAhenkAccessMethod() {
		return ahenkAccessMethod;
	}

	public void setAhenkAccessMethod(AccessMethod ahenkAccessMethod) {
		this.ahenkAccessMethod = ahenkAccessMethod;
	}

	public InstallMethod getAhenkInstallMethod() {
		return ahenkInstallMethod;
	}

	public void setAhenkInstallMethod(InstallMethod ahenkInstallMethod) {
		this.ahenkInstallMethod = ahenkInstallMethod;
	}

	public boolean isInstallationFinished() {
		return isInstallationFinished;
	}

	public void setInstallationFinished(boolean isInstallationFinished) {
		this.isInstallationFinished = isInstallationFinished;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

}
