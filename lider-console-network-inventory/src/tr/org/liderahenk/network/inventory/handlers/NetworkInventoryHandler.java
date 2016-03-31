package tr.org.liderahenk.network.inventory.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import tr.org.liderahenk.liderconsole.core.editorinput.DefaultEditorInput;
import tr.org.liderahenk.network.inventory.editors.NetworkInventoryEditor;
import tr.org.liderahenk.network.inventory.i18n.Messages;

public class NetworkInventoryHandler extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IWorkbenchPage page = window.getActivePage();
		
       	try {
       		page.openEditor(new DefaultEditorInput(Messages.getString("NETWORKINVENTORY")), NetworkInventoryEditor.ID);
		} catch (PartInitException e) {
			e.printStackTrace();
		}       	
       	return null;
	}
}
