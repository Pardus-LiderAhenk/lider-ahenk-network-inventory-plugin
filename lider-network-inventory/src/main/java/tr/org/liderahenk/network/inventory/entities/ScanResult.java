package tr.org.liderahenk.network.inventory.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Entity class for network parameters.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 * @see tr.org.liderahenk.network.inventory.dto.NmapParametersDto
 *
 */
@Entity
@Table(name = "PLGN_NETWORK_INVENTORY_SCAN_RESULT")
public class ScanResult {

	@Id
	@GeneratedValue
	@Column(name = "SCAN_RESULT_ID")
	private Integer id;

	private String ipRange;

	private List<String> ipList;

	private String timingTemplate;

	private String ports;

	private String sudoUsername;

	private String sudoPassword;

	private Date scanDate;

	@OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	private List<Host> hosts = new ArrayList<Host>(0);

	public ScanResult() {
		super();
	}

	public ScanResult(Integer id, String ipRange, List<String> ipList, String timingTemplate, String ports,
			String sudoUsername, String sudoPassword, Date scanDate, List<Host> hosts) {
		super();
		this.id = id;
		this.ipRange = ipRange;
		this.ipList = ipList;
		this.timingTemplate = timingTemplate;
		this.ports = ports;
		this.sudoUsername = sudoUsername;
		this.sudoPassword = sudoPassword;
		this.scanDate = scanDate;
		this.hosts = hosts;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getIpRange() {
		return ipRange;
	}

	public void setIpRange(String ipRange) {
		this.ipRange = ipRange;
	}

	public List<String> getIpList() {
		return ipList;
	}

	public void setIpList(List<String> ipList) {
		this.ipList = ipList;
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

	public String getTimingTemplate() {
		return timingTemplate;
	}

	public void setTimingTemplate(String timingTemplate) {
		this.timingTemplate = timingTemplate;
	}

	public Date getScanDate() {
		return scanDate;
	}

	public void setScanDate(Date scanDate) {
		this.scanDate = scanDate;
	}

	public List<Host> getHosts() {
		return hosts;
	}

	public void setHosts(List<Host> hosts) {
		this.hosts = hosts;
	}

}
