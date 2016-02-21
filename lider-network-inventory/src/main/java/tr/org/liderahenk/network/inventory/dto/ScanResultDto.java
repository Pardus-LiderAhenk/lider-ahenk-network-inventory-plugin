package tr.org.liderahenk.network.inventory.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Data transfer object class for network scan results.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 * @see tr.org.liderahenk.network.inventory.entities.ScanResult
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScanResultDto implements Serializable {

	private static final long serialVersionUID = -6156923247068631737L;

	private String ipRange;

	private String timingTemplate;

	private String ports;

	private String sudoUsername;

	private String sudoPassword;

	private Date scanDate;

	private List<ScanResultHostDto> hosts;

	public ScanResultDto() {
		super();
	}

	public ScanResultDto(String ipRange, String timingTemplate, String ports, String sudoUsername, String sudoPassword,
			Date scanDate, List<ScanResultHostDto> hosts) {
		super();
		this.ipRange = ipRange;
		this.timingTemplate = timingTemplate;
		this.ports = ports;
		this.sudoUsername = sudoUsername;
		this.sudoPassword = sudoPassword;
		this.scanDate = scanDate;
		this.hosts = hosts;
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

	public List<ScanResultHostDto> getHosts() {
		return hosts;
	}

	public void setHosts(List<ScanResultHostDto> hosts) {
		this.hosts = hosts;
	}

}
