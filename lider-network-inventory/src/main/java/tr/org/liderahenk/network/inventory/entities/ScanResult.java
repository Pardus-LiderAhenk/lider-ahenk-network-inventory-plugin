package tr.org.liderahenk.network.inventory.entities;

import java.util.List;

import javax.persistence.Entity;
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

	private String ipRange;

	private List<String> ipList;

	private String timingTemplate;

	private String ports;

	private String sudoUsername;

	private String sudoPassword;
	
	

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
	
}
