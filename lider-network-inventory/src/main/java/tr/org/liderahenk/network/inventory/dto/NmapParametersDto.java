package tr.org.liderahenk.network.inventory.dto;

import java.io.Serializable;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import tr.org.liderahenk.network.inventory.utils.network.NetworkUtils;

/**
 * Data transfer object for network mapper parameters.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 * @see tr.org.liderahenk.network.inventory.entities.ScanResult
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NmapParametersDto implements Serializable {

	private static final long serialVersionUID = 355170254886079322L;

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
		return str.append(" IP Range: ")
				.append(getIpRange())
				.append(" IP List: ")
				.append(getIpList())
				.append(" Timing Template: ")
				.append(timingTemplate)
				.append(" Ports: ")
				.append(ports)
				.append(" Sudo: ")
				.append(sudoUsername)
				.toString();
	}

}
