package tr.org.liderahenk.network.inventory.editorinputs;

import tr.org.liderahenk.liderconsole.core.editorinput.DefaultEditorInput;

/**
 * 
 * @author <a href="mailto:mine.dogan@agem.com.tr">Mine Dogan</a>
 *
 */
public class NetworkInventoryEditorInput extends DefaultEditorInput {
	
	private String label;
	private String dn;

	public NetworkInventoryEditorInput(String label, String dn) {
		super(label);
		this.label = label;
		this.dn = dn;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDn() {
		return dn;
	}

	public void setDn(String dn) {
		this.dn = dn;
	}
	
}
