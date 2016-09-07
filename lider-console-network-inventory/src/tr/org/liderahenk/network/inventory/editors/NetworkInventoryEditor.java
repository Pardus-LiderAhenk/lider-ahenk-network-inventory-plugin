package tr.org.liderahenk.network.inventory.editors;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.ldap.enums.DNType;
import tr.org.liderahenk.liderconsole.core.rest.enums.RestResponseStatus;
import tr.org.liderahenk.liderconsole.core.rest.requests.TaskRequest;
import tr.org.liderahenk.liderconsole.core.rest.responses.IResponse;
import tr.org.liderahenk.liderconsole.core.rest.responses.RestResponse;
import tr.org.liderahenk.liderconsole.core.rest.utils.TaskRestUtils;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;
import tr.org.liderahenk.liderconsole.core.xmpp.enums.ContentType;
import tr.org.liderahenk.liderconsole.core.xmpp.notifications.TaskStatusNotification;
import tr.org.liderahenk.network.inventory.constants.NetworkInventoryConstants;
import tr.org.liderahenk.network.inventory.dialogs.AhenkSetupDialog;
import tr.org.liderahenk.network.inventory.dialogs.FileShareDialog;
import tr.org.liderahenk.network.inventory.dialogs.FileShareResultDialog;
import tr.org.liderahenk.network.inventory.editorinputs.NetworkInventoryEditorInput;
import tr.org.liderahenk.network.inventory.i18n.Messages;
import tr.org.liderahenk.network.inventory.model.FileDistResult;
import tr.org.liderahenk.network.inventory.model.ScanResult;
import tr.org.liderahenk.network.inventory.model.ScanResultHost;

/**
 * An editor that sends some network related commands such as network scan,
 * Ahenk installation and file sharing.
 * 
 * @author <a href="mailto:caner.feyzullahoglu@agem.com.tr">Caner
 *         Feyzullahoğlu</a>
 * @author <a href="mailto:mine.dogan@agem.com.tr">Mine Dogan</a>
 */
public class NetworkInventoryEditor extends EditorPart {

	public static final String ID = "tr.org.liderahenk.network.inventory.editors.NetworkInventoryEditor";

	private Button[] btnScanOptions = new Button[2];
	private Button btnScan;
	private Button btnAhenkInstall;
	private Button btnFileUpload;
	private Button btnShareFile;

	private Text txtIpRange;
	private Text txtPortRange;
	private Text txtFilePath;
	private Label lblAhenkInstall;
	private TableViewer tblInventory;

	private Combo cmbTimingTemplate;
	// Combo values
	private final String[] templateArr = new String[] { "PARANOID", "SNEAKY", "POLITE", "NORMAL", "AGGRESSIVE",
			"INSANE" };
	private final String[] templateValueArr = new String[] { "0", "1", "2", "3", "4", "5" };

	private List<String> selectedIpList;

	private IEventBroker eventBroker = (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		eventBroker.subscribe(NetworkInventoryConstants.PLUGIN_NAME.toUpperCase(Locale.ENGLISH), eventHandler);
	}

	private EventHandler eventHandler = new EventHandler() {
		@Override
		public void handleEvent(final Event event) {
			Job job = new Job("TASK") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					monitor.beginTask("INVENTORY", 100);
					try {

						TaskStatusNotification taskStatus = (TaskStatusNotification) event
								.getProperty("org.eclipse.e4.data");

						// If result contains plain text
						if (ContentType.TEXT_PLAIN.equals(taskStatus.getResult().getContentType())) {

							// Put resultId to parameter map
							// and create new task to get result of the nmap
							// scan task from database
							Map<String, Object> parameterMap = new HashMap<String, Object>();
							Long resultId = taskStatus.getResult().getId();
							parameterMap.put("resultId", resultId);

							TaskRequest taskRequest = new TaskRequest(null, null, NetworkInventoryConstants.PLUGIN_NAME,
									NetworkInventoryConstants.PLUGIN_VERSION,
									NetworkInventoryConstants.GET_SCAN_RESULT_COMMAND, parameterMap, null, null,
									new Date());
							
							IResponse response = TaskRestUtils.execute(taskRequest);

							if (response.getStatus() != RestResponseStatus.OK) {
								List<String> messages = response.getMessages();
								Notifier.error(null, messages != null && !messages.isEmpty() ? messages.get(0)
										: Messages.getString("ERROR_OCCURED"));
							} else {
								Map<String, Object> resultMap = response.getResultMap();
								ObjectMapper mapper = new ObjectMapper();
								
								final ScanResult scanResult = mapper.readValue(resultMap.get("result").toString(), ScanResult.class);
								
								Display.getDefault().asyncExec(new Runnable() {
									@Override
									public void run() {
										tblInventory.setInput(scanResult.getHosts());
									}
								});
							}
						}

					} catch (Exception e) {
						e.printStackTrace();
						Notifier.error(null, Messages.getString("UNEXPECTED_ERROR"));
					}

					monitor.worked(100);
					monitor.done();

					return Status.OK_STATUS;
				}
			};

			job.setUser(true);
			job.schedule();
		}
	};

	@Override
	public void createPartControl(Composite parent) {

		Composite cmpMain = new Composite(parent, SWT.NONE);
		cmpMain.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		cmpMain.setLayout(new GridLayout(2, false));

		Composite cmpAction = new Composite(cmpMain, SWT.NONE);
		cmpAction.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		cmpAction.setLayout(new GridLayout(1, false));

		Composite cmpSide = new Composite(cmpMain, SWT.NONE);
		cmpSide.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		cmpSide.setLayout(new GridLayout(1, false));

		createScanArea(cmpAction);
		createAhenkInstallArea(cmpSide);
		createFileShareArea(cmpSide);

		createTableArea(cmpMain);
	}

	private void createFileShareArea(Composite composite) {

		final Composite cmpFileShare = new Composite(composite, SWT.BORDER);
		cmpFileShare.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		cmpFileShare.setLayout(new GridLayout(3, false));

		txtFilePath = new Text(cmpFileShare, SWT.RIGHT | SWT.SINGLE | SWT.FILL | SWT.BORDER);
		txtFilePath.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
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
				dialog.setFilterPath(System.getProperty("user.dir"));
				String open = dialog.open();
				if (open != null) {
					txtFilePath.setText(open);
				}
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

		final Composite cmpAhenkInstall = new Composite(composite, SWT.BORDER);
		cmpAhenkInstall.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		cmpAhenkInstall.setLayout(new GridLayout(2, true));

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

				ArrayList<String> dnList = null;
				String dn = ((NetworkInventoryEditorInput) getEditorInput()).getDn();
				if (dn != null) {
					dnList = new ArrayList<String>();
					dnList.add(dn);
				}

				AhenkSetupDialog dialog = new AhenkSetupDialog(cmpAhenkInstall.getShell(), null, selectedIpList,
						btnScanOptions[0].getSelection(), dnList);
				dialog.open();
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
		cmpScan.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		cmpScan.setLayout(new GridLayout(1, false));

		Composite cmpOptions = new Composite(cmpScan, SWT.NONE);
		cmpOptions.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		cmpOptions.setLayout(new GridLayout(6, false));

		btnScanOptions[0] = new Button(cmpOptions, SWT.RADIO);
		btnScanOptions[0].setText(Messages.getString("USE_AHENK"));

		btnScanOptions[1] = new Button(cmpOptions, SWT.RADIO);
		btnScanOptions[1].setText(Messages.getString("USE_LIDER"));

		if (((NetworkInventoryEditorInput) getEditorInput()).getDn() == null) {
			btnScanOptions[0].setEnabled(false);
			btnScanOptions[1].setEnabled(false);
			btnScanOptions[1].setSelection(true);
		} else {
			btnScanOptions[1].setSelection(true);
		}
		Label bos = new Label(cmpOptions, SWT.FILL);
		bos.setLayoutData(new GridData(15, 30));

		Label lblTimingTemp = new Label(cmpOptions, SWT.NONE);
		lblTimingTemp.setText(Messages.getString("TIMING_TEMPLATE"));

		cmbTimingTemplate = new Combo(cmpOptions, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
		cmbTimingTemplate.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		cmbTimingTemplate.add("");
		cmbTimingTemplate.setData("");
		for (int i = 1; i <= templateArr.length; i++) {
			String label = Messages.getString(templateArr[i - 1]);
			if (label != null && !label.isEmpty()) {
				cmbTimingTemplate.add(label);
				cmbTimingTemplate.setData(i + "", templateValueArr[i - 1]);
			}
		}
		// Select 'normal' by default
		cmbTimingTemplate.select(4);

		Composite cmpIp = new Composite(cmpScan, SWT.NONE);
		cmpIp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		cmpIp.setLayout(new GridLayout(5, false));

		Label lblIpRange = new Label(cmpIp, SWT.NONE);
		lblIpRange.setText(Messages.getString("IP_RANGE"));

		txtIpRange = new Text(cmpIp, SWT.RIGHT | SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		txtIpRange.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		txtIpRange.setMessage(Messages.getString("EX_IP"));

		Label lblPortRange = new Label(cmpIp, SWT.NONE);
		// lblPortRange.setLayoutData(new GridData(93, 20));
		lblPortRange.setText(Messages.getString("PORT_RANGE"));

		txtPortRange = new Text(cmpIp, SWT.RIGHT | SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		txtPortRange.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		txtPortRange.setMessage(Messages.getString("EX_PORT"));
		txtPortRange.setText("21-25");

		btnScan = new Button(cmpIp, SWT.NONE);
		btnScan.setImage(
				SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/search.png"));
		btnScan.setText(Messages.getString("START_SCAN"));
		btnScan.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!txtIpRange.getText().isEmpty()) {

					// Clear table items belong to previous response result
					if (tblInventory.getTable().getItems().length > 0) {
						tblInventory.getTable().removeAll();
						tblInventory.getTable().redraw();
					}

					// Populate request parameters
					Map<String, Object> parameterMap = new HashMap<String, Object>();
					parameterMap.put("ipRange", txtIpRange.getText());
					parameterMap.put("ports", txtPortRange.getText());
					parameterMap.put("timingTemplate", getSelectedValue(cmbTimingTemplate));
					parameterMap.put("executeOnAgent", btnScanOptions[0].getSelection());

					ArrayList<String> dnList = null;
					String dn = ((NetworkInventoryEditorInput) getEditorInput()).getDn();
					if (dn != null) {
						dnList = new ArrayList<String>();
						dnList.add(dn);
					}

					final TaskRequest task = new TaskRequest(dnList, DNType.AHENK,
							NetworkInventoryConstants.PLUGIN_NAME, NetworkInventoryConstants.PLUGIN_VERSION,
							NetworkInventoryConstants.SCAN_COMMAND, parameterMap, null, null, new Date());

					try {
						final RestResponse response = (RestResponse) TaskRestUtils.execute(task);
						if (response.getStatus() != RestResponseStatus.OK) {
							List<String> messages = response.getMessages();
							Notifier.error(null, messages != null && !messages.isEmpty() ? messages.get(0)
									: Messages.getString("ERROR_OCCURED"));
							return;
						}
						if (!(btnScanOptions[0].getSelection())) {
							Map<String, Object> resultMap = response.getResultMap();
							ObjectMapper mapper = new ObjectMapper();
							ScanResult scanResult;
							scanResult = mapper.readValue(resultMap.get("result").toString(), ScanResult.class);
							tblInventory.setInput(scanResult.getHosts());
						}

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

	/**
	 * 
	 * @param combo
	 * @return selected value of the provided combo
	 */
	public static String getSelectedValue(Combo combo) {
		int selectionIndex = combo.getSelectionIndex();
		if (selectionIndex > -1 && combo.getItem(selectionIndex) != null
				&& combo.getData(selectionIndex + "") != null) {
			return combo.getData(selectionIndex + "").toString();
		}
		return "4";
	}

	private void createTableArea(final Composite composite) {

		tblInventory = SWTResourceManager.createCheckboxTableViewer(composite);
		createTableColumns();

		// Listen checkbox selections of IP table and enable/disable install
		// Ahenk button according to these selections
		tblInventory.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				btnAhenkInstall.setEnabled(checkIpSelection(tblInventory));
				btnShareFile.setEnabled(checkIpSelection(tblInventory) && !txtFilePath.getText().isEmpty());
			}
		});
	}

	private void createTableColumns() {

		TableViewerColumn ipCol = SWTResourceManager.createTableViewerColumn(tblInventory,
				Messages.getString("IP_ADDRESS"), 120);
		ipCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String ip = ((ScanResultHost) element).getIp();
				return ip != null ? ip : Messages.getString("UNTITLED");
			}

			@Override
			public Color getForeground(Object element) {
				if (((ScanResultHost) element).isHostUp()) {
					return SWTResourceManager.getSuccessColor();
				} else {
					return SWTResourceManager.getErrorColor();
				}
			}
		});

		TableViewerColumn hostnameCol = SWTResourceManager.createTableViewerColumn(tblInventory,
				Messages.getString("HOST_NAME"), 100);
		hostnameCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String hostname = ((ScanResultHost) element).getHostname();
				return hostname != null ? hostname : Messages.getString("UNTITLED");
			}
		});

		TableViewerColumn portsCol = SWTResourceManager.createTableViewerColumn(tblInventory,
				Messages.getString("PORTS"), 150);
		portsCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String openPorts = ((ScanResultHost) element).getOpenPorts();
				return openPorts != null ? openPorts : Messages.getString("UNTITLED");
			}
		});

		TableViewerColumn osCol = SWTResourceManager.createTableViewerColumn(tblInventory,
				Messages.getString("OS_INFO"), 200);
		osCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String osGuess = ((ScanResultHost) element).getOsGuess();
				return osGuess != null ? osGuess : Messages.getString("UNTITLED");
			}
		});

		TableViewerColumn distanceCol = SWTResourceManager.createTableViewerColumn(tblInventory,
				Messages.getString("DISTANCE"), 80);
		distanceCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String distance = ((ScanResultHost) element).getDistance();
				return distance != null ? distance : Messages.getString("UNTITLED");
			}
		});

		TableViewerColumn uptimeCol = SWTResourceManager.createTableViewerColumn(tblInventory,
				Messages.getString("UPTIME"), 100);
		uptimeCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String uptime = ((ScanResultHost) element).getUptime();
				return uptime != null ? uptime : Messages.getString("UNTITLED");
			}
		});

		TableViewerColumn macAddressCol = SWTResourceManager.createTableViewerColumn(tblInventory,
				Messages.getString("MAC_ADDRESS"), 150);
		macAddressCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String mac = ((ScanResultHost) element).getMac();
				return mac != null ? mac : Messages.getString("UNTITLED");
			}
		});

		TableViewerColumn macVendorCol = SWTResourceManager.createTableViewerColumn(tblInventory,
				Messages.getString("MAC_VENDOR"), 100);
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

	@Override
	public void dispose() {
		super.dispose();
		eventBroker.unsubscribe(eventHandler);
	}
}
