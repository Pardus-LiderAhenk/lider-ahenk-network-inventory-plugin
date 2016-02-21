package tr.org.liderahenk.network.inventory.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Entity class for scanned hosts.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 * @see tr.org.liderahenk.network.inventory.dto.HostDto
 *
 */
@Entity
@Table(name = "PLGN_NETWORK_INVENTORY_HOSTS")
public class Host {
	
	@Id
	@GeneratedValue
	@Column(name = "HOST_ID")
	private Integer id;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "SCAN_RESULT_ID")
	private ScanResult scanResult;
	
	private String hostname;
	
	private String openPorts;
	
	private String osGuess;
	
	private String distance;
	
	private String uptime;
	
	private String mac;
	
	private String vendor;
	
	// TODO additional info about ahenk-installed machines

	public Host() {
		super();
	}
	
	public Host(Integer id, String hostname, String openPorts, String osGuess, String distance, String uptime,
			String mac, String vendor) {
		super();
		this.id = id;
		this.hostname = hostname;
		this.openPorts = openPorts;
		this.osGuess = osGuess;
		this.distance = distance;
		this.uptime = uptime;
		this.mac = mac;
		this.vendor = vendor;
	}

	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public ScanResult getScanResult() {
		return scanResult;
	}

	public void setScanResult(ScanResult scanResult) {
		this.scanResult = scanResult;
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
	
}
