package tr.org.liderahenk.network.inventory.entities;

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
public class AhenkSetupParameters {

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
	private byte[] privateKey;

	@Column(name = "PASSPHRASE")
	private String passphrase;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "SETUP_DATE")
	private Date setupDate;

	@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	private List<AhenkSetupResultDetail> details = new ArrayList<AhenkSetupResultDetail>();

	public AhenkSetupParameters() {
		super();
	}

	public AhenkSetupParameters(Long id, String installMethod, String accessMethod, String username, String password,
			Integer port, byte[] privateKey, String passphrase, Date setupDate, List<AhenkSetupResultDetail> details) {
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
		this.details = details;
	}
}
