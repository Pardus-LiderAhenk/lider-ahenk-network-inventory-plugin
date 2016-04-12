package tr.org.liderahenk.network.inventory.model;

import java.io.Serializable;

/**
 * A model class that keeps the detailed results an Ahenk installation
 * command for each host.
 * 
 * @author <a href="mailto:caner.feyzullahoglu@agem.com.tr">Caner
 *         Feyzullahoğlu</a>
 */
public class AhenkSetupResultDetail implements Serializable {

	private static final long serialVersionUID = -6160067284883201047L;

	private String ip;
	
	private boolean success;

	private String message;
	
	public AhenkSetupResultDetail() {
		super();
	}

	public AhenkSetupResultDetail(String ip, boolean success, String message) {
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
	
	public void setSuccess(boolean success) {
		this.success = success;
	}

	public void setSetupResult(boolean setupResult) {
		this.success = setupResult;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
