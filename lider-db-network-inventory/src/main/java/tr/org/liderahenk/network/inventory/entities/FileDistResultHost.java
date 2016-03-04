package tr.org.liderahenk.network.inventory.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Entity class for scanned hosts.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 * @see tr.org.liderahenk.network.inventory.dto.FileDistResultHostDto
 *
 */
@Entity
@Table(name = "P_FILE_DIST_RESULT_HOST")
public class FileDistResultHost {

	@Id
	@GeneratedValue
	@Column(name = "HOST_ID")
	private Integer id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "FILE_DIST_RESULT_ID")
	private FileDistResult fileDistResult;

	@Column(name = "IP_ADDRESS")
	private String ip;

	@Column(name = "IS_SUCCESSFULL")
	private boolean success;

	@Column(name = "ERROR_MESSAGE")
	private String errorMessage;

	public FileDistResultHost() {
		super();
	}

	public FileDistResultHost(Integer id, FileDistResult fileDistResult, String ip, boolean success,
			String errorMessage) {
		super();
		this.id = id;
		this.fileDistResult = fileDistResult;
		this.ip = ip;
		this.success = success;
		this.errorMessage = errorMessage;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public FileDistResult getFileDistResult() {
		return fileDistResult;
	}

	public void setFileDistResult(FileDistResult fileDistResult) {
		this.fileDistResult = fileDistResult;
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
