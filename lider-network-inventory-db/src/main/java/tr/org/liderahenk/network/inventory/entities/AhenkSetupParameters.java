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
 * Entity class for Ahenk installation parameters. It has a detail entity class
 * for installation results (AhenkSetupDetailResult).
 * 
 * @author Caner Feyzullahoğlu <caner.feyzullahoglu@agem.com.tr>

 * @see tr.org.liderahenk.network.inventory.entities.AhenkSetupResultDetail
 *
 */
@Entity
@Table(name = "P_AHENK_SETUP_RESULT")
public class AhenkSetupParameters implements Serializable {
	
	private static final long serialVersionUID = -4130227601711334080L;

	@Id
	@GeneratedValue
	@Column(name = "AHENK_SETUP_RESULT_ID", unique = true, nullable = false)
	private Long id;

	@Column(name = "INSTALL_METHOD", nullable = false)
	private String installMethod;

	@Column(name = "ACCESS_METHOD", nullable = false)
	private String accessMethod;

	@Column(name = "USERNAME", nullable = false)
	private String username;

	@Column(name = "PASSWORD")
	private String password;

	@Column(name = "PORT", nullable = false)
	private Integer port;

	@Column(name = "PRIVATE_KEY")
	private String privateKey;

	@Column(name = "PASSPHRASE")
	private String passphrase;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "SETUP_DATE", nullable = false)
	private Date setupDate;

	@Column(name = "DOWNLOAD_URL")
	private String downloadUrl;

	@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	private List<AhenkSetupResultDetail> details = new ArrayList<AhenkSetupResultDetail>();

	public AhenkSetupParameters() {
	}

	public AhenkSetupParameters(Long id, String installMethod, String accessMethod, String username, String password,
			Integer port, String privateKey, String passphrase, Date setupDate, String downloadUrl, List<AhenkSetupResultDetail> details) {
		super();
		this.id = id;
		this.installMethod = installMethod;
		this.accessMethod = accessMethod;
		this.username = username;
		this.password = password;
		this.port = port;
		this.privateKey = privateKey;
		this.passphrase = passphrase;
		this.setupDate = setupDate;
		this.downloadUrl = downloadUrl;
		this.details = details;
	}
	
	public void addResultDetail(AhenkSetupResultDetail detail) {
		if (details == null) {
			details = new ArrayList<AhenkSetupResultDetail>();
		}
		details.add(detail);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getInstallMethod() {
		return installMethod;
	}

	public void setInstallMethod(String installMethod) {
		this.installMethod = installMethod;
	}

	public String getAccessMethod() {
		return accessMethod;
	}

	public void setAccessMethod(String accessMethod) {
		this.accessMethod = accessMethod;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public String getPassphrase() {
		return passphrase;
	}

	public void setPassphrase(String passphrase) {
		this.passphrase = passphrase;
	}

	public Date getSetupDate() {
		return setupDate;
	}

	public void setSetupDate(Date setupDate) {
		this.setupDate = setupDate;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public List<AhenkSetupResultDetail> getDetails() {
		return details;
	}

	public void setDetails(List<AhenkSetupResultDetail> details) {
		this.details = details;
	}
	
	
}
