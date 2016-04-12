package tr.org.liderahenk.network.inventory.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.network.inventory.i18n.Messages;
import tr.org.liderahenk.network.inventory.model.FileDistResultHost;

/**
 * A dialog that shows the result of a file sharing command.
 * 
 * @author <a href="mailto:caner.feyzullahoglu@agem.com.tr">Caner
 *         Feyzullahoğlu</a>
 */
public class FileShareResultDialog extends Dialog {

	private TableViewer tblViewer;
	
	private List<FileDistResultHost> resultList;
	
	public FileShareResultDialog(Shell parentShell, List<FileDistResultHost> resultList) {
		super(parentShell);
		this.resultList = resultList;
		
		createButtonBar(parentShell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		
		Composite mainComposite = SWTResourceManager.createComposite(parent, 1);
		
		createTableArea(mainComposite);
		
		tblViewer.setInput(resultList);
		
		return mainComposite;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(650, 450);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("FILE_SHARING_RESULTS"));
	}
	
	private void createTableArea(final Composite composite) {

		tblViewer = new TableViewer(composite,
				SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);

		createTableColumns();

		final Table table = tblViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.getVerticalBar().setEnabled(true);
		table.getVerticalBar().setVisible(true);

		tblViewer.setContentProvider(new ArrayContentProvider());

		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		tblViewer.getControl().setLayoutData(gridData);

	}
	
	private void createTableColumns() {
		TableViewerColumn ipCol = createTableViewerColumn(tblViewer, Messages.getString("IP_ADDRESS"), 175);
		ipCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String ip = ((FileDistResultHost) element).getIp();
				return ip != null ? ip : Messages.getString("UNTITLED");
			}
		});

		TableViewerColumn successCol = createTableViewerColumn(tblViewer, Messages.getString("RESULT"), 150);
		successCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String result;
				if (((FileDistResultHost) element).isSuccess()) {
					result = Messages.getString("SUCCESSFUL");
				} else {
					result = Messages.getString("ERROR_OCCURED");
				}
				return result;
			}
		});

		TableViewerColumn messageCol = createTableViewerColumn(tblViewer, Messages.getString("MESSAGE"), 150);
		messageCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String errorMessage = ((FileDistResultHost) element).getErrorMessage();
				return errorMessage != null ? errorMessage : Messages.getString("UNTITLED");
			}
		});
	}
	
	/**
	 * Helper method to create table columns
	 * 
	 * @param tblVwrSetup
	 * @param title
	 * @param bound
	 * @return
	 */
	private TableViewerColumn createTableViewerColumn(final TableViewer tblVwrSetup, String title, int bound) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(tblVwrSetup, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(false);
		column.setAlignment(SWT.LEFT);
		return viewerColumn;
	}
	
	@Override
	protected boolean isResizable() {
		return true;
	}
	
}
