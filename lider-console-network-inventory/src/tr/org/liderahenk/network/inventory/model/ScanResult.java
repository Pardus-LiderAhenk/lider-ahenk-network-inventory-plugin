package tr.org.liderahenk.network.inventory.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Model class to correspond to data transfer object class that comes from server
 * side for network scan results.
 * 
 * @author <a href="mailto:caner.feyzullahoglu@agem.com.tr">Caner
 *         Feyzullahoglu</a>
 *
 */

public class ScanResult implements Serializable {

	private static final long serialVersionUID = 6257218673285744658L;

	private String ipRange;
	
	private String portRange;

	private String timingTemplate;

	private String ports;

	private String sudoUsername;

	private String sudoPassword;

	private Date scanDate;

	private List<ScanResultHost> hosts;

	public ScanResult() {
		super();
	}

	public ScanResult(String ipRange, String portRange, String timingTemplate, String ports, String sudoUsername, String sudoPassword,
			Date scanDate, List<ScanResultHost> hosts) {
		super();
		this.ipRange = ipRange;
		this.timingTemplate = timingTemplate;
		this.ports = ports;
		this.sudoUsername = sudoUsername;
		this.sudoPassword = sudoPassword;
		this.scanDate = scanDate;
		this.hosts = hosts;
		this.portRange = portRange;
	}

	public String getIpRange() {
		return ipRange;
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

	public Date getScanDate() {
		return scanDate;
	}

	public void setScanDate(Date scanDate) {
		this.scanDate = scanDate;
	}

	public List<ScanResultHost> getHosts() {
		return hosts;
	}

	public void setHosts(List<ScanResultHost> hosts) {
		this.hosts = hosts;
	}

	public String getPortRange() {
		return portRange;
	}

	public void setPortRange(String portRange) {
		this.portRange = portRange;
	}

}
