package tr.org.liderahenk.network.inventory.model;

import java.io.Serializable;

/**
 * Model class for results of distributing the specified file to hosts.
 * 
 * @author <a href="mailto:caner.feyzullahoglu@agem.com.tr">Caner
 *         Feyzullahoğlu</a>
 *
 */
public class FileDistResultHost implements Serializable {

	private static final long serialVersionUID = -4363580359189557690L;

	private String ip;

	private boolean success;

	private String errorMessage;

	public FileDistResultHost() {
		super();
	}

	public FileDistResultHost(String ip, boolean success, String errorMessage) {
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
