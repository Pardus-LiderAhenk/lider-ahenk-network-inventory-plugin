package tr.org.liderahenk.network.inventory.dto;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Data transfer object class for hosts which we tried to install Ahenk.
 * 
 * @author <a href="mailto:caner.feyzullahoglu@agem.com.tr">Caner Feyzullahoğlu</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AhenkSetupDetailDto implements Serializable {

	private static final long serialVersionUID = -1379366202742260928L;

	private String ip;
	
	private boolean success;

	private String message;
	
	public AhenkSetupDetailDto() {
		super();
	}

	public AhenkSetupDetailDto(String ip, boolean success, String message) {
		super();
		this.ip = ip;
		this.success = success;
		this.message = message;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean setupResult) {
		this.success = setupResult;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
}
