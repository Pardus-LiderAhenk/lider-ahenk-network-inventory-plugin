package tr.org.liderahenk.network.inventory.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "AHENK_SETUP_DETAIL_RESULT")
public class AhenkSetupDetailResult {

	@Id
	@GeneratedValue
	@Column(name = "AHENK_SETUP_DETAIL_RESULT_ID")
	private Long id;
	
	@Column(name = "IP")
	private Long ip;
	
	@Column(name = "SETUP_RESULT")
	private String setupResult;

	public AhenkSetupDetailResult() {
		super();
	}
	
	public AhenkSetupDetailResult(Long id, Long ip, String setupResult) {
		super();
		this.id = id;
		this.ip = ip;
		this.setupResult = setupResult;
	}
}
