package tr.org.liderahenk.network.inventory.utils.network;

import org.nmap4j.core.nmap.NMapProperties;

public class LiderNMapProperties extends NMapProperties {

	private String sudoUser;
	private String sudoUserPassword;
	
	public LiderNMapProperties(String path) {
		super(path);
	}

	@Override
	public String getFullyFormattedCommand() {
		String command = super.getFullyFormattedCommand();
		if (sudoUser != null && sudoUserPassword != null) {
			// Execute as specified sudo user and wait for password!
			command = "sudo -u " + sudoUser + " -S " + command;
		}
		return command;
	}

	public String getSudoUser() {
		return sudoUser;
	}

	public void setSudoUser(String sudoUser) {
		this.sudoUser = sudoUser;
	}

	public String getSudoUserPassword() {
		return sudoUserPassword;
	}

	public void setSudoUserPassword(String sudoUserPassword) {
		this.sudoUserPassword = sudoUserPassword;
	}

}
