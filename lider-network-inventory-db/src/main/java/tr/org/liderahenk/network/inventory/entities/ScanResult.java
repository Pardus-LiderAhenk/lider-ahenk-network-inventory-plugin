package tr.org.liderahenk.network.inventory.entities;

import java.io.Serializable;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Entity class for network scan results.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 * @see tr.org.liderahenk.network.inventory.dto.ScanResultDto
 *
 */
@Entity
@Table(name = "P_NETWORK_SCAN_RESULT")
public class ScanResult implements Serializable {

	private static final long serialVersionUID = -2849756506403423208L;

	@Id
	@GeneratedValue
	@Column(name = "SCAN_RESULT_ID")
	private Long id;

	@Column(name = "RESULT_ID")
	private Long resultId;
	
	@Column(name = "IP_RANGE")
	private String ipRange;

	@Column(name = "TIMING_TEMPLATE")
	private String timingTemplate;

	@Column(name = "PORTS")
	private String ports;

	@Column(name = "USERNAME")
	private String sudoUsername;

	@Column(name = "PASSWORD")
	private String sudoPassword;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "SCAN_DATE")
	private Date scanDate;

	@OneToMany(mappedBy = "scanResult", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	private List<ScanResultHost> hosts = new ArrayList<ScanResultHost>(0);

	public ScanResult() {
		super();
	}

	public ScanResult(Long id, Long resultId, String ipRange, String timingTemplate, String ports, String sudoUsername,
			String sudoPassword, Date scanDate, List<ScanResultHost> hosts) {
		super();
		this.id = id;
		this.resultId = resultId;
		this.ipRange = ipRange;
		this.timingTemplate = timingTemplate;
		this.ports = ports;
		this.sudoUsername = sudoUsername;
		this.sudoPassword = sudoPassword;
		this.scanDate = scanDate;
		this.hosts = hosts;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getIpRange() {
		return ipRange;
	}

	public void setIpRange(String ipRange) {
		this.ipRange = ipRange;
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

	public List<ScanResultHost> getHosts() {
		return hosts;
	}

	public void setHosts(List<ScanResultHost> hosts) {
		this.hosts = hosts;
	}

	public Long getResultId() {
		return resultId;
	}

	public void setResultId(Long resultId) {
		this.resultId = resultId;
	}

}
