package tr.org.liderahenk.network.inventory.model;

import java.io.Serializable;

/**
 * Model class for scanned hosts.
 * 
 * @author <a href="mailto:caner.feyzullahoglu@agem.com.tr">Caner
 *         Feyzullahoglu</a>
 */
public class ScanResultHost implements Serializable {

	private static final long serialVersionUID = -6526266401464167129L;

	private String hostname;

	private String ip;

	private boolean isHostUp;

	private String openPorts;

	private String osGuess;

	private String distance;

	private String uptime;

	private String mac;

	private String vendor;

	// TODO additional info about ahenk-installed machines

	public ScanResultHost() {
		super();
	}

	public ScanResultHost(String hostname, String ip, boolean isHostUp, String openPorts, String osGuess,
			String distance, String uptime, String mac, String vendor) {
		super();
		this.hostname = hostname;
		this.ip = ip;
		this.isHostUp = isHostUp;
		this.openPorts = openPorts;
		this.osGuess = osGuess;
		this.distance = distance;
		this.uptime = uptime;
		this.mac = mac;
		this.vendor = vendor;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getOpenPorts() {
		return openPorts;
	}

	public void setOpenPorts(String openPorts) {
		this.openPorts = openPorts;
	}

	public String getOsGuess() {
		return osGuess;
	}

	public void setOsGuess(String osGuess) {
		this.osGuess = osGuess;
	}

	public String getDistance() {
		return distance;
	}

	public void setDistance(String distance) {
		this.distance = distance;
	}

	public String getUptime() {
		return uptime;
	}

	public void setUptime(String uptime) {
		this.uptime = uptime;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public boolean isHostUp() {
		return isHostUp;
	}

	public void setHostUp(boolean isHostUp) {
		this.isHostUp = isHostUp;
	}

}
