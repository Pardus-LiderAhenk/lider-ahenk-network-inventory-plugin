package tr.org.liderahenk.network.inventory.handlers;

import org.apache.directory.studio.ldapbrowser.core.model.IBookmark;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.impl.SearchResult;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import tr.org.liderahenk.network.inventory.editorinputs.NetworkInventoryEditorInput;
import tr.org.liderahenk.network.inventory.editors.NetworkInventoryEditor;
import tr.org.liderahenk.network.inventory.i18n.Messages;

/**
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class NetworkInventoryHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		String dn = null;

		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IWorkbenchPage page = window.getActivePage();
		ISelection selection = page.getSelection();
		if (selection == null) {
			// Under certain circumstances, selection may be null (This might
			// be an eclipse bug?) In that case, this line below can also
			// provide the selection.
			selection = HandlerUtil.getCurrentSelection(event);
		}

		if (selection instanceof IStructuredSelection) {

			IStructuredSelection sselection = (IStructuredSelection) selection;
			Object selectedElement = sselection.getFirstElement();

			if (selectedElement != null) {

				if (selectedElement instanceof SearchResult) {
					dn = ((SearchResult) selectedElement).getDn().toString();
				} else if (selectedElement instanceof IBookmark) {
					dn = ((IBookmark) selectedElement).getDn().toString();
				} else if (selectedElement instanceof javax.naming.directory.SearchResult) {
					dn = ((javax.naming.directory.SearchResult) selectedElement).getName();
				} else if (selectedElement instanceof IEntry) {
					dn = ((IEntry) selectedElement).getDn().toString();
				} else if (selectedElement instanceof ISearch) {
					dn = ((ISearch) selectedElement).getName();
				}

			}

		}

		try {
			page.openEditor(new NetworkInventoryEditorInput(Messages.getString("NETWORK_INVENTORY"), dn),
					NetworkInventoryEditor.ID);
		} catch (PartInitException e) {
			e.printStackTrace();
		}

		return null;
	}

}