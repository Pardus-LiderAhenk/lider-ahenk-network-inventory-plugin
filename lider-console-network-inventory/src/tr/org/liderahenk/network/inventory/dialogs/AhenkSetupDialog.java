package tr.org.liderahenk.network.inventory.dialogs;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.dialogs.DefaultTaskDialog;
import tr.org.liderahenk.liderconsole.core.exceptions.ValidationException;
import tr.org.liderahenk.liderconsole.core.ldap.enums.DNType;
import tr.org.liderahenk.liderconsole.core.rest.requests.TaskRequest;
import tr.org.liderahenk.liderconsole.core.rest.responses.RestResponse;
import tr.org.liderahenk.liderconsole.core.rest.utils.TaskRestUtils;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.liderconsole.core.widgets.LiderConfirmBox;
import tr.org.liderahenk.network.inventory.constants.AccessMethod;
import tr.org.liderahenk.network.inventory.constants.InstallMethod;
import tr.org.liderahenk.network.inventory.constants.NetworkInventoryConstants;
import tr.org.liderahenk.network.inventory.i18n.Messages;
import tr.org.liderahenk.network.inventory.model.AhenkSetupResult;

/**
 * A dialog for entering Ahenk installation parameters and starting the installation.
 * 
 * @author <a href="mailto:caner.feyzullahoglu@agem.com.tr">Caner
 *         Feyzullahoğlu</a>
 */
public class AhenkSetupDialog extends DefaultTaskDialog {

	private Button btnUsePrivateKey;
	private Button btnUseUsernamePwd;

	private Text txtUsername;
	private Text txtPwd;

	private Text txtKeyUsername;
	private Text txtKeyPath;
	private Text txtPassphrase;

	private Text txtDebFileUrl;
	private Text txtReceiveFile;

	private boolean executeOnAgent;
	private List<String> selectedIpList;
	private Button btnExecuteNow;
	private ArrayList<String> dnList;
	private Combo cmbUseSsl;
	
	public AhenkSetupDialog(Shell parentShell, Set<String> dnSet, List<String> selectedIpList, boolean executeOnAgent,
			ArrayList<String> dnList) {
		super(parentShell, dnSet, true);
		this.executeOnAgent = executeOnAgent;
		this.selectedIpList = selectedIpList;
		this.dnList = dnList;
	}

	@Override
	public String createTitle() {
		return Messages.getString("AHENK_INSTALLATION_PARAMETERS");
	}

	@Override
	public Control createTaskDialogArea(Composite parent) {

		Composite cmpMain = SWTResourceManager.createComposite(parent, 1);

		btnUseUsernamePwd = SWTResourceManager.createButton(cmpMain, SWT.RADIO,
				Messages.getString("USE_USERNAME_AND_PWD"));
		btnUseUsernamePwd.setSelection(true);
		btnUseUsernamePwd.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				organizeFields();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
			}
		});

		Composite cmpUsernamePwd = SWTResourceManager.createComposite(cmpMain, 2);

		SWTResourceManager.createLabel(cmpUsernamePwd, Messages.getString("USERNAME"));
		txtUsername = SWTResourceManager.createText(cmpUsernamePwd);
		txtUsername.setText("root");
		txtUsername.setMessage(Messages.getString("ENTER_USERNAME_FOR_SSH_CONNECTION"));

		SWTResourceManager.createLabel(cmpUsernamePwd, Messages.getString("PASSWORD"));
		txtPwd = SWTResourceManager.createText(cmpUsernamePwd);
		txtPwd.setMessage(Messages.getString("ENTER_PWD_OF_USERNAME"));

		btnUsePrivateKey = SWTResourceManager.createButton(cmpMain, SWT.RADIO, Messages.getString("USE_PRIVATE_KEY"));
		btnUsePrivateKey.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				organizeFields();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
			}
		});
		btnUsePrivateKey.setSelection(false);

		Composite cmpPrivateKey = SWTResourceManager.createComposite(cmpMain, 2);

		SWTResourceManager.createLabel(cmpPrivateKey, Messages.getString("USERNAME"));
		txtKeyUsername = SWTResourceManager.createText(cmpPrivateKey);
		txtKeyUsername.setEnabled(false);
		txtKeyUsername.setText("root");
		txtKeyUsername.setMessage(Messages.getString("ENTER_USERNAME_FOR_DEFINED_PRIVATE_KEY"));

		SWTResourceManager.createLabel(cmpPrivateKey, Messages.getString("PRIVATE_KEY_PATH"));
		txtKeyPath = SWTResourceManager.createText(cmpPrivateKey);
		txtKeyPath.setEnabled(false);
		txtKeyPath.setText("~/.ssh/id_rsa");
		txtKeyPath.setMessage("ENTER_LOCATION_OF_PRIVATE_KEY");

		SWTResourceManager.createLabel(cmpPrivateKey, Messages.getString("PASSPHRASE"));
		txtPassphrase = SWTResourceManager.createText(cmpPrivateKey);
		txtPassphrase.setEnabled(false);
		txtPassphrase.setMessage(Messages.getString("ENTER_PASSPHRASE_OF_PRIVATE_KEY"));

		SWTResourceManager.createLabel(cmpMain, Messages.getString("DEB_FILE_URL"));
		txtDebFileUrl = SWTResourceManager.createText(cmpMain);
		txtDebFileUrl.setText("www.agem.com.tr/ahenk/ahenk_1.0_amd64.deb");
		txtDebFileUrl.setMessage(Messages.getString("ENTER_URL_OF_AHENK_DEB"));

		SWTResourceManager.createLabel(cmpMain, Messages.getString("RECEIVE_FILE_PATH"));
		txtReceiveFile = SWTResourceManager.createText(cmpMain);
		txtReceiveFile.setText("/tmp/");
		txtReceiveFile.setMessage(Messages.getString("ENTER_RECEIVE_FILE_PATH"));

		SWTResourceManager.createLabel(cmpMain, Messages.getString("AHENK_USE_SSL"));
		cmbUseSsl = new Combo(cmpMain, SWT.DROP_DOWN | SWT.READ_ONLY);
		cmbUseSsl.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		cmbUseSsl.setItems(new String[] {"false", "true"});
		cmbUseSsl.select(0);
		
		return cmpMain;
	}

	private void organizeFields() {

		if (btnUseUsernamePwd.getSelection()) {
			txtUsername.setEnabled(true);
			txtPwd.setEnabled(true);
			txtKeyUsername.setEnabled(false);
			txtKeyPath.setEnabled(false);
			txtPassphrase.setEnabled(false);
		} else {
			txtUsername.setEnabled(false);
			txtPwd.setEnabled(false);
			txtKeyUsername.setEnabled(true);
			txtKeyPath.setEnabled(true);
			txtPassphrase.setEnabled(true);
		}

	}

	@Override
	public void validateBeforeExecution() throws ValidationException {
		if (btnUseUsernamePwd.getSelection() && (txtUsername.getText().isEmpty() || txtPwd.getText().isEmpty()
				|| txtDebFileUrl.getText().isEmpty() || txtReceiveFile.getText().isEmpty())) {
			throw new ValidationException(Messages.getString("PLEASE_FILL_REQUIRED_INFO"));
		} else if (btnUsePrivateKey.getSelection() && (txtKeyUsername.getText().isEmpty()
				|| txtKeyPath.getText().isEmpty() || txtDebFileUrl.getText().isEmpty() || txtReceiveFile.getText().isEmpty())) {
			throw new ValidationException(Messages.getString("PLEASE_FILL_REQUIRED_INFO"));
		}
	}

	@Override
	public Map<String, Object> getParameterMap() {
		return null;
	}

	@Override
	public String getCommandId() {
		return null;
	}

	@Override
	public String getPluginName() {
		return null;
	}

	@Override
	public String getPluginVersion() {
		return null;
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent) {
		btnExecuteNow = createButton(parent, 5000, Messages.getString("EXECUTE_NOW"), false);
		btnExecuteNow.setImage(
				SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/task-play.png"));
		btnExecuteNow.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Validation of task data
				if (getSelf().validateTaskData()) {
					if (LiderConfirmBox.open(Display.getDefault().getActiveShell(),
							Messages.getString("TASK_EXEC_TITLE"), Messages.getString("TASK_EXEC_MESSAGE"))) {

						Map<String, Object> parameterMap = new HashMap<String, Object>();

						// Put parameters to map
						parameterMap.put("ipList", selectedIpList);
						parameterMap.put("accessMethod", btnUseUsernamePwd.getSelection()
								? AccessMethod.USERNAME_PASSWORD : AccessMethod.PRIVATE_KEY);
						parameterMap.put("installMethod", InstallMethod.WGET);
						parameterMap.put("username",
								btnUseUsernamePwd.getSelection() ? txtUsername.getText() : txtKeyUsername.getText());
						parameterMap.put("port", new Integer(22));
						parameterMap.put("executeOnAgent", executeOnAgent);
						parameterMap.put("downloadUrl", txtDebFileUrl.getText());
						parameterMap.put("receiveFile", txtReceiveFile.getText());

						if (btnUseUsernamePwd.getSelection()) {
							parameterMap.put("password", txtPwd.getText());
						} else {
							parameterMap.put("privateKeyPath", txtKeyPath.getText());
							parameterMap.put("passphrase", txtPassphrase.getText());
						}
						
						parameterMap.put("useTls", cmbUseSsl.getText());

						TaskRequest task = new TaskRequest();
						task = new TaskRequest(dnList, DNType.AHENK, NetworkInventoryConstants.PLUGIN_NAME,
								NetworkInventoryConstants.PLUGIN_VERSION, NetworkInventoryConstants.INSTALL_COMMAND,
								parameterMap, null, null, new Date());

						Map<String, Object> resultMap = new HashMap<String, Object>();

						// Send command
						RestResponse response;
						try {
							getProgressBar().setVisible(true);
							response = (RestResponse) TaskRestUtils.execute(task);

							if (!executeOnAgent) {
								resultMap = response.getResultMap();
							}
							
							getProgressBar().setVisible(false);

						} catch (Exception e3) {
							e3.printStackTrace();
						}

						ObjectMapper mapper = new ObjectMapper();

						try {
							if (!executeOnAgent) {
								AhenkSetupResult setupResult = mapper.readValue(resultMap.get("result").toString(),
										AhenkSetupResult.class);
								AhenkSetupResultDialog resultDialog = new AhenkSetupResultDialog(parent.getShell(),
										setupResult.getSetupDetailList());
								resultDialog.open();
							}
						} catch (Exception e2) {
							e2.printStackTrace();
						}
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		// Close
		createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("CANCEL"), true);
	}

	protected AhenkSetupDialog getSelf() {
		return this;
	}

}
