package tr.org.liderahenk.network.inventory.model;

import java.util.List;

import tr.org.liderahenk.network.inventory.utils.network.NetworkUtils;

public class NmapParameters {

	private String ipRange;
	private List<String> ipList;
	private String timingTemplate;
	private String ports;
	private String sudoUsername;
	private String sudoPassword;
	
	public String getIpRange() {
		return ipRange == null ? NetworkUtils.convertToIpRange(ipList) : ipRange;
	}

	public void setIpRange(String ipRange) {
		this.ipRange = ipRange;
	}

	public String getTimingTemplate() {
		return timingTemplate;
	}

	public void setTimingTemplate(String timingTemplate) {
		this.timingTemplate = timingTemplate;
	}

	public String getPorts() {
		return ports;
	}

	public void setPorts(String ports) {
		this.ports = ports;
	}

	public String getSudoUsername() {
		return sudoUsername;
	}

	public void setSudoUsername(String sudoUsername) {
		this.sudoUsername = sudoUsername;
	}

	public String getSudoPassword() {
		return sudoPassword;
	}

	public void setSudoPassword(String sudoPassword) {
		this.sudoPassword = sudoPassword;
	}

	public List<String> getIpList() {
		return ipList == null ? NetworkUtils.convertToIpList(ipRange) : ipList;
	}

	public void setIpList(List<String> ipList) {
		this.ipList = ipList;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		return str.append(" IP Range: ").append(getIpRange())
				.append(" IP List: ").append(getIpList())
				.append(" Timing Template: ").append(timingTemplate)
				.append(" Ports: ").append(ports)
				.append(" Sudo: ").append(sudoUsername)
				.append(" Sudo Pwd: ").append(sudoPassword != null && !sudoPassword.isEmpty())
				.toString();
	}
	
}
