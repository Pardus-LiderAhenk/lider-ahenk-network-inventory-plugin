package tr.org.liderahenk.network.inventory.dto;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Data transfer object class for hosts which we tried to distribute the
 * specified file.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 * @see tr.org.liderahenk.network.inventory.entities.FileDistResultHost
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileDistResultHostDto implements Serializable {

	private static final long serialVersionUID = 2154833650187473041L;

	private String ip;

	private boolean success;

	private String errorMessage;

	public FileDistResultHostDto() {
		super();
	}

	public FileDistResultHostDto(String ip, boolean success, String errorMessage) {
		super();
		this.ip = ip;
		this.success = success;
		this.errorMessage = errorMessage;
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

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
