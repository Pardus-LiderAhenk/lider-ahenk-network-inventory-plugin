package tr.org.liderahenk.network.inventory.editors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import tr.org.liderahenk.liderconsole.core.rest.Priority;
import tr.org.liderahenk.liderconsole.core.rest.RestClient;
import tr.org.liderahenk.liderconsole.core.rest.RestRequest;
import tr.org.liderahenk.liderconsole.core.rest.RestResponse;
import tr.org.liderahenk.liderconsole.core.ui.GenericEditorInput;
import tr.org.liderahenk.network.inventory.i18n.Messages;
import tr.org.liderahenk.network.inventory.wizard.AhenkSetupWizard;

public class NetworkInventoryEditor extends EditorPart {

	public static final String ID = "tr.org.liderahenk.network.inventory.editors.NetworkInventoryEditor";

	private String userName;
	private String entryDn;

	private Button btnScan;
	private Button btnAhenkInstall;
	private Button btnFileUpload;
	private Button btnShareFile;

	private Text txtIpRange;
	private Label lblAhenkInstall;
	private TableViewer tblInventory;

	private List<String> selectedIpList;

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		final GenericEditorInput ginput = (GenericEditorInput) input;
		setEntryDn(ginput.getId());
		setUserName(ginput.getUserId());
		setPartName(input.getName());
	}

	@Override
	public void createPartControl(Composite parent) {

		Composite cmpMain = new Composite(parent, SWT.NONE);
		cmpMain.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		cmpMain.setLayout(new GridLayout(1, false));

		Composite cmpAction = new Composite(cmpMain, SWT.NONE);
		cmpAction.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		cmpAction.setLayout(new GridLayout(3, false));

		createScanArea(cmpAction);
		createAhenkInstallArea(cmpAction);
		createFileShareArea(cmpAction);

		createTableArea(cmpMain);

	}

	private void createFileShareArea(Composite composite) {

		final Composite cmpAhenkInstall = new Composite(composite, SWT.BORDER);
		cmpAhenkInstall.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		cmpAhenkInstall.setLayout(new GridLayout(3, false));

		final Text txtFilePath = new Text(cmpAhenkInstall, SWT.RIGHT | SWT.SINGLE | SWT.FILL | SWT.BORDER);

		btnFileUpload = new Button(cmpAhenkInstall, SWT.NONE);
		btnFileUpload.setImage(
				new Image(cmpAhenkInstall.getDisplay(), this.getClass().getResourceAsStream("/icons/sample.gif")));
		btnFileUpload.setText(Messages.getString("UPLOAD_FILE"));
		btnFileUpload.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				FileDialog dialog = new FileDialog(cmpAhenkInstall.getShell(), SWT.OPEN);
				// dialog.setFilterExtensions(new String [] {"*.<file_type>"});
				dialog.setFilterPath("/home/volkan/");
				txtFilePath.setText(dialog.open());
				// String result = dialog.open();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		btnShareFile = new Button(cmpAhenkInstall, SWT.NONE);
		btnShareFile.setImage(
				new Image(cmpAhenkInstall.getDisplay(), this.getClass().getResourceAsStream("/icons/sample.gif")));
		btnShareFile.setText(Messages.getString("SHARE_FILE"));
		btnShareFile.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

	}

	private void createAhenkInstallArea(Composite composite) {

		Composite cmpAhenkInstall = new Composite(composite, SWT.BORDER);
		cmpAhenkInstall.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		cmpAhenkInstall.setLayout(new GridLayout(2, false));

		lblAhenkInstall = new Label(cmpAhenkInstall, PROP_TITLE);
		lblAhenkInstall.setText(Messages.getString("FOR_AHENK_INSTALLATION"));

		btnAhenkInstall = new Button(cmpAhenkInstall, SWT.NONE);
		btnAhenkInstall.setImage(
				new Image(cmpAhenkInstall.getDisplay(), this.getClass().getResourceAsStream("/icons/sample.gif")));
		btnAhenkInstall.setText(Messages.getString("INSTALL_AHENK"));
		btnAhenkInstall.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				setSelectedIps();

				AhenkSetupWizard wizard = new AhenkSetupWizard(selectedIpList);

				WizardDialog wd = new WizardDialog(Display.getCurrent().getActiveShell(), wizard);
				wd.setMinimumPageSize(new Point(800, 600));
				wd.setPageSize(new Point(800, 600));
				wd.open();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		btnAhenkInstall.setEnabled(false);
	}

	private void setSelectedIps() {

		TableItem[] items = tblInventory.getTable().getItems();
		
		List<String> tmpList = new ArrayList<String>();
		
		for (TableItem item : items) {
			
			if (item.getChecked()) {
				tmpList.add(item.getText(0));
			}
		}
		
		selectedIpList = tmpList;
	}

	private void createScanArea(Composite composite) {

		Composite cmpScan = new Composite(composite, SWT.BORDER);
		cmpScan.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		cmpScan.setLayout(new GridLayout(3, false));

		Label lblIpRange = new Label(cmpScan, SWT.NONE);
		lblIpRange.setText("IP Aralığı");

		txtIpRange = new Text(cmpScan, SWT.RIGHT | SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		txtIpRange.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		txtIpRange.setMessage(Messages.getString("EX_IP"));

		btnScan = new Button(cmpScan, SWT.NONE);
		btnScan.setImage(new Image(cmpScan.getDisplay(), this.getClass().getResourceAsStream("/icons/sample.gif")));
		btnScan.setText(Messages.getString("START_SCAN"));
		btnScan.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Create request instance
				RestRequest request = new RestRequest();
				request.setPluginName("network-inventory");
				request.setPluginVersion("1.0.0-SNAPSHOT");
				request.setCommandId("SCANNETWORK");
				request.setPriority(Priority.NORMAL);

				// Populate request parameters
				Map<String, Object> parameterMap = new HashMap<String, Object>();
				parameterMap.put("ipRange", txtIpRange.getText());
				parameterMap.put("timingTemplate", "3");
				request.setParameterMap(parameterMap);

				// Post request
				RestResponse response = RestClient.getInstance().post(request);
				Map<String, Object> resultMap = response.getResultMap();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

	}

	private void createTableArea(final Composite composite) {

		tblInventory = new TableViewer(composite,
				SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER | SWT.CHECK);

		// createTableColumns();

		final Table table = tblInventory.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.getVerticalBar().setEnabled(true);
		table.getVerticalBar().setVisible(true);

		tblInventory.setContentProvider(new ArrayContentProvider());
		tblInventory.setInput(createFakeIpToTable(table));

		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		tblInventory.getControl().setLayoutData(gridData);

		// Listen checkbox selections of IP table and enable/disable install
		// Ahenk button according to these selections
		tblInventory.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (!event.getSelection().isEmpty()) {
					updateInstallButtonStatus(tblInventory, btnAhenkInstall);
				}
			}
		});

	}

	// TODO fake data, will be removed.
	private List<String> createFakeIpToTable(Table table) {

		List<String> ipList = new ArrayList<String>();

		for (int i = 0; i < 10; i++) {
			ipList.add("192.168.56." + (i + 1));
		}

		return ipList;
	}

	/**
	 * Enables/Disables install Ahenk button according to IP selections.
	 * 
	 * @param tblVwr
	 * @param btn
	 */
	private void updateInstallButtonStatus(TableViewer tblVwr, Button btn) {

		// At least one IP should be selected
		boolean ipSelected = false;

		TableItem[] items = tblVwr.getTable().getItems();

		for (int i = 0; i < items.length; i++) {
			if (items[i].getChecked()) {
				ipSelected = true;
				// If one of the IP's is selected, that's enough
				// do not iterate over all items
				i = items.length;
			}
		}

		btn.setEnabled(ipSelected);
	}

	@Override
	public void setFocus() {

	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getEntryDn() {
		return entryDn;
	}

	public void setEntryDn(String entryDn) {
		this.entryDn = entryDn;
	}

}
