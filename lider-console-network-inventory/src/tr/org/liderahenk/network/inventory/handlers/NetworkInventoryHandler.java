package tr.org.liderahenk.network.inventory.handlers;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import tr.org.liderahenk.liderconsole.core.handlers.SingleSelectionHandler;
import tr.org.liderahenk.network.inventory.editors.NetworkInventoryEditor;
import tr.org.liderahenk.network.inventory.editors.NetworkInventoryEditorInput;
import tr.org.liderahenk.network.inventory.i18n.Messages;

public class NetworkInventoryHandler extends SingleSelectionHandler {

	@Override
	public void executeWithDn(String dn) {
		
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		IWorkbenchPage page = win.getActivePage();
		
		try {
			page.closeEditor(page.getActiveEditor(), true);
			page.openEditor(new NetworkInventoryEditorInput(Messages.getString("NETWORK_INVENTORY"), dn), NetworkInventoryEditor.ID);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}
}