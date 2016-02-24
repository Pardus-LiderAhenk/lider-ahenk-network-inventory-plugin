package tr.org.liderahenk.network.inventory.wizard.pages;

import tr.org.liderahenk.network.inventory.constants.NextPageEventType;

/**
 * If there is a need to control the complexity of setPageComplete 
 * method and next/previous button clicks this interface can be 
 * implemented. For further information, please look at comments of 
 * NextPageEventType enum. And as an example usage, please look at 
 * XmppAccessPage and LiderLocationOfComponentsPage classes.
 * 
 * 
 * @author Caner Feyzullahoğlu <caner.feyzullahoglu@agem.com.tr>
 *
 */
public interface ControlNextEvent {
	public NextPageEventType getNextPageEventType();

	public void setNextPageEventType(NextPageEventType nextPageEventType);
}
