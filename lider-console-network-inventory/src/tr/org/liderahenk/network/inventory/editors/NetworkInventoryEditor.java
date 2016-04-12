package tr.org.liderahenk.network.inventory.editors;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.rest.requests.TaskRequest;
import tr.org.liderahenk.liderconsole.core.rest.responses.RestResponse;
import tr.org.liderahenk.liderconsole.core.rest.utils.TaskUtils;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;
import tr.org.liderahenk.network.inventory.constants.AccessMethod;
import tr.org.liderahenk.network.inventory.constants.InstallMethod;
import tr.org.liderahenk.network.inventory.dialogs.AhenkSetupResultDialog;
import tr.org.liderahenk.network.inventory.dialogs.FileShareDialog;
import tr.org.liderahenk.network.inventory.dialogs.FileShareResultDialog;
import tr.org.liderahenk.network.inventory.i18n.Messages;
import tr.org.liderahenk.network.inventory.model.AhenkSetupConfig;
import tr.org.liderahenk.network.inventory.model.AhenkSetupResult;
import tr.org.liderahenk.network.inventory.model.FileDistResult;
import tr.org.liderahenk.network.inventory.model.ScanResult;
import tr.org.liderahenk.network.inventory.model.ScanResultHost;
import tr.org.liderahenk.network.inventory.wizard.AhenkSetupWizard;

/**
 * An editor that sends some network related commands such as network scan,
 * Ahenk installation and file sharing.
 * 
 * @author <a href="mailto:caner.feyzullahoglu@agem.com.tr">Caner
 *         Feyzullahoğlu</a>
 */
public class NetworkInventoryEditor extends EditorPart {

	public static final String ID = "tr.org.liderahenk.network.inventory.editors.NetworkInventoryEditor";

	private String userName;
	private String entryDn;

	private Button btnScan;
	private Button btnAhenkInstall;
	private Button btnFileUpload;
	private Button btnShareFile;

	private Text txtIpRange;
	private Text txtFilePath;
	private Label lblAhenkInstall;
	private TableViewer tblInventory;

	private List<String> selectedIpList;

	// Host colours
	Color HOST_UP_COLOR = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN);
	Color HOST_DOWN_COLOR = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED);

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
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

		final Composite cmpFileShare = new Composite(composite, SWT.BORDER);
		cmpFileShare.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		cmpFileShare.setLayout(new GridLayout(3, false));

		txtFilePath = new Text(cmpFileShare, SWT.RIGHT | SWT.SINGLE | SWT.FILL | SWT.BORDER);
		txtFilePath.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				btnShareFile.setEnabled(checkIpSelection(tblInventory) && !txtFilePath.getText().isEmpty());
			}
		});

		btnFileUpload = new Button(cmpFileShare, SWT.NONE);
		btnFileUpload.setImage(
				SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/folder-add.png"));
		btnFileUpload.setText(Messages.getString("UPLOAD_FILE"));
		btnFileUpload.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				FileDialog dialog = new FileDialog(cmpFileShare.getShell(), SWT.OPEN);
				dialog.setFilterPath("/home/volkan/");
				txtFilePath.setText(dialog.open());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		btnShareFile = new Button(cmpFileShare, SWT.NONE);
		btnShareFile.setImage(
				SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/share.png"));
		btnShareFile.setText(Messages.getString("SHARE_FILE"));
		btnShareFile.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				// Read file
				byte[] fileArray = readFileAsByteArray(txtFilePath.getText());
				String encodedFile = DatatypeConverter.printBase64Binary(fileArray);

				// Find file name
				int lastSeparatorIndex = txtFilePath.getText().lastIndexOf(FileSystems.getDefault().getSeparator());
				String filename = txtFilePath.getText(lastSeparatorIndex + 1, txtFilePath.getText().length());

				setSelectedIps();

				FileShareDialog dialog = new FileShareDialog(Display.getCurrent().getActiveShell(), selectedIpList,
						encodedFile, filename);

				dialog.open();

				Map<String, Object> resultMap = dialog.getResultMap();

				ObjectMapper mapper = new ObjectMapper();

				try {
					FileDistResult distResult = mapper.readValue(resultMap.get("result").toString(),
							FileDistResult.class);

					FileShareResultDialog resultDialog = new FileShareResultDialog(
							Display.getCurrent().getActiveShell(), distResult.getHosts());

					resultDialog.open();

				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		btnShareFile.setEnabled(false);

	}

	private void createAhenkInstallArea(final Composite composite) {

		Composite cmpAhenkInstall = new Composite(composite, SWT.BORDER);
		cmpAhenkInstall.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		cmpAhenkInstall.setLayout(new GridLayout(2, false));

		lblAhenkInstall = new Label(cmpAhenkInstall, PROP_TITLE);
		lblAhenkInstall.setText(Messages.getString("FOR_AHENK_INSTALLATION"));

		btnAhenkInstall = new Button(cmpAhenkInstall, SWT.NONE);
		btnAhenkInstall.setImage(SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE,
				"icons/16/package-download-install.png"));
		btnAhenkInstall.setText(Messages.getString("INSTALL_AHENK"));
		btnAhenkInstall.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				setSelectedIps();

				AhenkSetupWizard wizard = new AhenkSetupWizard(selectedIpList);

				WizardDialog wizardDialog = new WizardDialog(Display.getCurrent().getActiveShell(), wizard);
				wizardDialog.setMinimumPageSize(new Point(800, 600));
				wizardDialog.setPageSize(new Point(800, 600));
				wizardDialog.open();

				AhenkSetupConfig config = wizard.getConfig();

				// Create request object
				TaskRequest task = new TaskRequest();
				task.setPluginName("network-inventory");
				task.setPluginVersion("1.0.0");
				task.setCommandId("INSTALLAHENK");

				// Add config object as parameter. It has all information that
				// Lider needs to know.
				Map<String, Object> parameterMap = new HashMap<String, Object>();

				// Put parameters to map
				parameterMap.put("ipList", config.getIpList());
				parameterMap.put("accessMethod", config.getAccessMethod());
				parameterMap.put("installMethod", config.getInstallMethod());
				parameterMap.put("username", config.getUsername());
				parameterMap.put("port", config.getPort());

				if (config.getAccessMethod() == AccessMethod.USERNAME_PASSWORD) {
					parameterMap.put("password", config.getPassword());
				} else {
					parameterMap.put("passphrase", config.getPassphrase());
				}

				if (config.getInstallMethod() == InstallMethod.WGET) {
					parameterMap.put("downloadUrl", config.getDownloadUrl());
				}

				task.setParameterMap(parameterMap);

				Map<String, Object> resultMap = new HashMap<String, Object>();

				// Send command
				RestResponse response;
				try {
					response = (RestResponse) TaskUtils.execute(task);

					resultMap = response.getResultMap();

				} catch (Exception e3) {
					e3.printStackTrace();
				}

				ObjectMapper mapper = new ObjectMapper();

				try {
					AhenkSetupResult setupResult = mapper.readValue(resultMap.get("result").toString(),
							AhenkSetupResult.class);

					AhenkSetupResultDialog resultDialog = new AhenkSetupResultDialog(composite.getShell(),
							setupResult.getSetupDetailList());

					resultDialog.open();

				} catch (Exception e2) {
					e2.printStackTrace();
				}
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
		btnScan.setImage(
				SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/search.png"));
		btnScan.setText(Messages.getString("START_SCAN"));
		btnScan.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!txtIpRange.getText().isEmpty()) {
					// Create request instance
					TaskRequest task = new TaskRequest();
					task.setPluginName("network-inventory");
					task.setPluginVersion("1.0.0");
					task.setCommandId("SCANNETWORK");

					// Populate request parameters
					Map<String, Object> parameterMap = new HashMap<String, Object>();
					parameterMap.put("ipRange", txtIpRange.getText());
					parameterMap.put("timingTemplate", "3");
					task.setParameterMap(parameterMap);

					RestResponse response;
					try {
						// Post request
						response = (RestResponse) TaskUtils.execute(task);

						Map<String, Object> resultMap = response.getResultMap();

						ObjectMapper mapper = new ObjectMapper();

						ScanResult scanResult = mapper.readValue(resultMap.get("result").toString(), ScanResult.class);

						tblInventory.setInput(scanResult.getHosts());

					} catch (Exception e1) {
						e1.printStackTrace();
					}
				} else {
					Notifier.warning(Messages.getString("NETWORK_SCAN"), Messages.getString("PLEASE_ENTER_IP_RANGE"));
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

	}

	private void createTableArea(final Composite composite) {

		tblInventory = new TableViewer(composite,
				SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER | SWT.CHECK);

		createTableColumns();

		final Table table = tblInventory.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.getVerticalBar().setEnabled(true);
		table.getVerticalBar().setVisible(true);

		tblInventory.setContentProvider(new ArrayContentProvider());

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
					btnAhenkInstall.setEnabled(checkIpSelection(tblInventory));
					btnShareFile.setEnabled(checkIpSelection(tblInventory) && !txtFilePath.getText().isEmpty());
				}
			}
		});

	}

	private void createTableColumns() {

		TableViewerColumn ipCol = createTableViewerColumn(tblInventory, Messages.getString("IP_ADDRESS"), 100);
		ipCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String ip = ((ScanResultHost) element).getIp();
				return ip != null ? ip : Messages.getString("UNTITLED");
			}

			@Override
			public Color getForeground(Object element) {
				if (((ScanResultHost) element).isHostUp()) {
					return HOST_UP_COLOR;
				} else {
					return HOST_DOWN_COLOR;
				}
			}
		});

		TableViewerColumn hostnameCol = createTableViewerColumn(tblInventory, Messages.getString("HOST_NAME"), 50);
		hostnameCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String hostname = ((ScanResultHost) element).getHostname();
				return hostname != null ? hostname : Messages.getString("UNTITLED");
			}
		});

		TableViewerColumn portsCol = createTableViewerColumn(tblInventory, Messages.getString("PORTS"), 150);
		portsCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String openPorts = ((ScanResultHost) element).getOpenPorts();
				return openPorts != null ? openPorts : Messages.getString("UNTITLED");
			}
		});

		TableViewerColumn osCol = createTableViewerColumn(tblInventory, Messages.getString("OS_INFO"), 250);
		osCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String osGuess = ((ScanResultHost) element).getOsGuess();
				return osGuess != null ? osGuess : Messages.getString("UNTITLED");
			}
		});

		TableViewerColumn distanceCol = createTableViewerColumn(tblInventory, Messages.getString("DISTANCE"), 30);
		distanceCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String distance = ((ScanResultHost) element).getDistance();
				return distance != null ? distance : Messages.getString("UNTITLED");
			}
		});

		TableViewerColumn uptimeCol = createTableViewerColumn(tblInventory, Messages.getString("UPTIME"), 50);
		uptimeCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String uptime = ((ScanResultHost) element).getUptime();
				return uptime != null ? uptime : Messages.getString("UNTITLED");
			}
		});

		TableViewerColumn macAddressCol = createTableViewerColumn(tblInventory, Messages.getString("MAC_ADDRESS"), 100);
		macAddressCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String mac = ((ScanResultHost) element).getMac();
				return mac != null ? mac : Messages.getString("UNTITLED");
			}
		});

		TableViewerColumn macVendorCol = createTableViewerColumn(tblInventory, Messages.getString("MAC_VENDOR"), 100);
		macVendorCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String vendor = ((ScanResultHost) element).getVendor();
				return vendor != null ? vendor : Messages.getString("UNTITLED");
			}
		});

	}

	/**
	 * Checks if any table item is selected.
	 * 
	 * @param tblVwr
	 * @return true if at least one table item is selected, false otherwise.
	 */
	private boolean checkIpSelection(TableViewer tblVwr) {

		TableItem[] items = tblVwr.getTable().getItems();

		// At least one IP should be selected
		for (int i = 0; i < items.length; i++) {
			if (items[i].getChecked()) {
				// If one of the IP's is selected, that's enough
				// do not iterate over all items
				return true;
			}
		}

		return false;
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

	/**
	 * Reads the file from provided path and returns it as an array of bytes.
	 * (Best use in Java 7)
	 * 
	 * @author Caner Feyzullahoglu <caner.feyzullahoglu@agem.com.tr>
	 * 
	 * @param pathOfFile
	 *            Absolute path to file
	 * @return given file as byte[]
	 */
	private byte[] readFileAsByteArray(String pathOfFile) {

		Path path;

		byte[] fileArray;

		try {

			path = Paths.get(pathOfFile);

			fileArray = Files.readAllBytes(path);

			return fileArray;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new byte[0];
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
