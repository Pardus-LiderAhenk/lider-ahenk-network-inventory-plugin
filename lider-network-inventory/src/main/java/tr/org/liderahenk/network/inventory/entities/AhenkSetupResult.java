package tr.org.liderahenk.network.inventory.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "AHENK_SETUP_RESULT")
public class AhenkSetupResult {

	@Id
	@GeneratedValue
	@Column(name = "AHENK_SETUP_RESULT_ID")
	private Long id;
	
	@Column(name = "INSTALL_METHOD")
	private String installMethod;
	
	@Column(name = "ACCESS_METHOD")
	private String accessMethod;
	
	@Column(name = "USERNAME")
	private String username;

	@Column(name = "PASSWORD")
	private String password;

	@Column(name = "PORT")
	private Integer port;

	@Column(name = "PRIVATE_KEY")
	private String privateKey;

	@Column(name = "PASSPHRASE")
	private String passphrase;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "SETUP_DATE")
	private Date setupDate;

	public AhenkSetupResult() {
		super();
	}

	public AhenkSetupResult(Long id, String installMethod, String accessMethod, String username, String password,
			Integer port, String privateKey, String passphrase, Date setupDate) {
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
	}
}
