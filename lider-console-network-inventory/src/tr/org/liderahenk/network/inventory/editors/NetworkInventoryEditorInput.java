package tr.org.liderahenk.network.inventory.editors;

import tr.org.liderahenk.liderconsole.core.editorinput.DefaultEditorInput;

/**
 * 
 * @author <a href="mailto:mine.dogan@agem.com.tr">Mine Dogan</a>
 *
 */
public class NetworkInventoryEditorInput extends DefaultEditorInput {
	
	private String label;
	private String commandId;

	public NetworkInventoryEditorInput(String label, String commandId) {
		super(label);
		this.label = label;
		this.commandId = commandId;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getCommandId() {
		return commandId;
	}

	public void setCommandId(String commandId) {
		this.commandId = commandId;
	}

}
