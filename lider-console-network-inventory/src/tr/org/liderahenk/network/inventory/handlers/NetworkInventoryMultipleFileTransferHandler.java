package tr.org.liderahenk.network.inventory.handlers;

import java.util.Set;

import org.eclipse.swt.widgets.Display;

import tr.org.liderahenk.liderconsole.core.handlers.MultipleSelectionHandler;
import tr.org.liderahenk.network.inventory.dialogs.MultipleFileTransferTaskDialog;

/**
 * A task handler class for the command (NetworkInventoryMultipleFileTransfer) that
 * distributes the selected file to multiple clients.
 * 
 * @author <a href="mailto:caner.feyzullahoglu@agem.com.tr">Caner
 *         Feyzullahoglu</a>
 */
public class NetworkInventoryMultipleFileTransferHandler extends MultipleSelectionHandler {

	@Override
	public void executeWithDNSet(Set<String> dnSet) {
		
		MultipleFileTransferTaskDialog taskDialog = new MultipleFileTransferTaskDialog(Display.getDefault().getActiveShell(), dnSet);
		taskDialog.create();
		taskDialog.open();
	}

}
