package tr.org.liderahenk.network.inventory.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Entity class for scanned hosts.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 * @see tr.org.liderahenk.network.inventory.dto.ScanResultHostDto
 *
 */
@Entity
@Table(name = "P_NETWORK_SCAN_RESULT_HOST")
public class ScanResultHost implements Serializable {

	private static final long serialVersionUID = 8670893089813007443L;

	@Id
	@GeneratedValue
	@Column(name = "HOST_ID")
	private Integer id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "SCAN_RESULT_ID")
	private ScanResult scanResult;

	@Column(name = "HOST_NAME")
	private String hostname;

	@Column(name = "IP_ADDRESS")
	private String ip;

	@Column(name = "IS_HOST_UP")
	private boolean hostUp;

	@Lob
	@Column(name = "OPEN_PORTS")
	private String openPorts;

	@Column(name = "OS_GUESS")
	private String osGuess;

	@Column(name = "DISTANCE")
	private String distance;

	@Column(name = "UPTIME")
	private String uptime;

	@Column(name = "MAC_ADDRESS")
	private String mac;

	@Column(name = "MAC_VENDOR")
	private String vendor;

	// TODO additional info about ahenk-installed machines

	public ScanResultHost() {
		super();
	}

	public ScanResultHost(Integer id, ScanResult scanResult, String hostname, String ip, boolean hostUp,
			String openPorts, String osGuess, String distance, String uptime, String mac, String vendor) {
		super();
		this.id = id;
		this.scanResult = scanResult;
		this.hostname = hostname;
		this.ip = ip;
		this.hostUp = hostUp;
		this.openPorts = openPorts;
		this.osGuess = osGuess;
		this.distance = distance;
		this.uptime = uptime;
		this.mac = mac;
		this.vendor = vendor;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
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

	public boolean isHostUp() {
		return hostUp;
	}

	public void setHostUp(boolean hostUp) {
		this.hostUp = hostUp;
	}

}
