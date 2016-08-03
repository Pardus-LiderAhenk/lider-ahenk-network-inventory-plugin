package tr.org.liderahenk.network.inventory.dialogs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import tr.org.liderahenk.liderconsole.core.rest.requests.TaskRequest;
import tr.org.liderahenk.liderconsole.core.rest.responses.RestResponse;
import tr.org.liderahenk.liderconsole.core.rest.utils.TaskRestUtils;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.network.inventory.constants.AccessMethod;
import tr.org.liderahenk.network.inventory.i18n.Messages;

/**
 * A dialog to be shown before starting a file sharing command to take required
 * information from user.
 * 
 * @author <a href="mailto:caner.feyzullahoglu@agem.com.tr">Caner
 *         Feyzullahoğlu</a>
 */
public class FileShareDialog extends Dialog {

	private Text txtDestDirectory;
	private Text txtPort;

	private Button btnUsernamePass;
	private Text txtUsername;
	private Text txtPassword;

	private Button btnPrivateKey;
	private Text txtPassphrase;

	private List<String> selectedIpList;

	private String encodedFile;

	private String filename;

	private Map<String, Object> resultMap;
	
	private Text txtKeyUsername;
	private Text txtKeyPath;
	
	public FileShareDialog(Shell parentShell, List<String> selectedIpList, String encodedFile, String filename) {
		super(parentShell);
		createButtonBar(parentShell);
		this.selectedIpList = selectedIpList;
		this.encodedFile = encodedFile;
		this.filename = filename;

	}

	@Override
	protected Control createDialogArea(Composite parent) {

		Composite mainComposite = SWTResourceManager.createComposite(parent, 1);

//		GridData gdNoGrapHorizontal = new GridData(SWT.LEFT, SWT.CENTER, false, true);

		SWTResourceManager.createLabel(mainComposite, Messages.getString("PLEASE_CHOOSE_ACCESS_METHOD"));

		// --- Destination directory and port --- //
		Composite compInfo = SWTResourceManager.createComposite(mainComposite, 2);
//		compInfo.setLayoutData(gdNoGrapHorizontal);

		GridData gdDestAndPort = new GridData();
		gdDestAndPort.widthHint = 250;

		SWTResourceManager.createLabel(compInfo, Messages.getString("DESTINATION_DIRECTORY"));
		txtDestDirectory = SWTResourceManager.createText(compInfo);
		txtDestDirectory.setLayoutData(gdDestAndPort);
		txtDestDirectory.setMessage(Messages.getString("EG_DEST_DIR"));
		txtDestDirectory.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				updateOkButtonStatus();
			}
		});

		SWTResourceManager.createLabel(compInfo, Messages.getString("PORT"));
		txtPort = SWTResourceManager.createText(compInfo);
		txtPort.setLayoutData(gdDestAndPort);
		txtPort.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				updateOkButtonStatus();
			}
		});
		txtPort.setText("22");
		// -------------------------------------- //

		// --- Username/Pass radio button area --- //
		Composite compRadioBtns = SWTResourceManager.createComposite(mainComposite, 1);

		btnUsernamePass = SWTResourceManager.createButton(compRadioBtns, SWT.RADIO,
				Messages.getString("USE_USERNAME_AND_PASSWORD_PAIR"));
		btnUsernamePass.setSelection(true);
		btnUsernamePass.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				organizeFields();
				updateOkButtonStatus();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		GridData gdUserAndPass = new GridData();
		gdUserAndPass.widthHint = 150;

		Composite compUserPass = SWTResourceManager.createComposite(compRadioBtns, 2);
//		compUserPass.setLayoutData(gdNoGrapHorizontal);

		SWTResourceManager.createLabel(compUserPass, Messages.getString("USERNAME"));
		txtUsername = SWTResourceManager.createText(compUserPass);
		txtUsername.setLayoutData(gdUserAndPass);
		txtUsername.setText("root");
		txtUsername.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				updateOkButtonStatus();
			}
		});

		SWTResourceManager.createLabel(compUserPass, Messages.getString("PASSWORD"));
		txtPassword = SWTResourceManager.createPasswordText(compUserPass);
		txtPassword.setLayoutData(gdUserAndPass);
		txtPassword.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				updateOkButtonStatus();
			}
		});
		// ------------------------------- //

		// --- Private key radio button area --- //
		btnPrivateKey = SWTResourceManager.createButton(compRadioBtns, SWT.RADIO,
				Messages.getString("USE_PRIVATE_KEY_TO_ACCESS"));
		btnPrivateKey.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				organizeFields();
				updateOkButtonStatus();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		Composite compPrivateKey = SWTResourceManager.createComposite(compRadioBtns, 2);
//		compPrivateKey.setLayoutData(gdNoGrapHorizontal);
		
		SWTResourceManager.createLabel(compPrivateKey, Messages.getString("USERNAME"));
		txtKeyUsername = SWTResourceManager.createText(compPrivateKey);
		txtKeyUsername.setEnabled(false);
		
		SWTResourceManager.createLabel(compPrivateKey, Messages.getString("PRIVATE_KEY_PATH"));
		txtKeyPath = SWTResourceManager.createText(compPrivateKey);
		txtKeyPath.setEnabled(false);
		txtKeyPath.setText("~/.ssh/id_rsa");
		txtKeyPath.setMessage("ENTER_LOCATION_OF_PRIVATE_KEY");
		
		SWTResourceManager.createLabel(compPrivateKey, Messages.getString("PASSPHRASE"));
		txtPassphrase = SWTResourceManager.createPasswordText(compPrivateKey);
		txtPassphrase.setEnabled(false);
		// ------------------------------------- //

		return mainComposite;
	}

	private void updateOkButtonStatus() {
		// First check destination directory and port
		if (!txtDestDirectory.getText().isEmpty() && !txtPort.getText().isEmpty()) {
			if (btnUsernamePass.getSelection()) {
				getButton(IDialogConstants.OK_ID)
						.setEnabled(!txtUsername.getText().isEmpty() && !txtPassword.getText().isEmpty());
			} else {
				getButton(IDialogConstants.OK_ID).setEnabled(true);
			}
		} else {
			getButton(IDialogConstants.OK_ID).setEnabled(false);
		}
	}

	private void organizeFields() {
		if (btnUsernamePass.getSelection()) {
			txtUsername.setEnabled(true);
			txtPassword.setEnabled(true);
			txtPassphrase.setEnabled(false);
			txtKeyUsername.setEnabled(false);
			txtKeyPath.setEnabled(false);
		} else {
			txtUsername.setEnabled(false);
			txtPassword.setEnabled(false);
			txtPassphrase.setEnabled(true);
			txtKeyUsername.setEnabled(true);
			txtKeyPath.setEnabled(true);
		}
	}

	@Override
	protected Point getInitialSize() {
		return new Point(500, 450);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("FILE_SHARING_ACCESS_INFO"));
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, true);
		getButton(IDialogConstants.CANCEL_ID).setEnabled(true);

		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		getButton(IDialogConstants.OK_ID).setEnabled(false);
	}

	@Override
	protected void okPressed() {

		// Create request instance
		TaskRequest task = new TaskRequest();
		task.setPluginName("network-inventory");
		task.setPluginVersion("1.0.0");
		task.setCommandId("DISTRIBUTEFILE");

		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put("ipAddresses", selectedIpList);
		parameterMap.put("file", encodedFile);
		parameterMap.put("filename", filename);
		parameterMap.put("destDirectory", txtDestDirectory.getText());
		parameterMap.put("username",btnUsernamePass.getSelection() ? txtUsername.getText() : txtKeyUsername.getText());
		parameterMap.put("password", txtPassword.getText());
		parameterMap.put("passphrase", txtPassphrase.getText());
		parameterMap.put("accessMethod", btnUsernamePass.getSelection() ? AccessMethod.USERNAME_PASSWORD : AccessMethod.PRIVATE_KEY);

		if (!btnUsernamePass.getSelection()) {
			parameterMap.put("privateKeyPath", txtKeyPath.getText());
		}
		
		Integer port = new Integer(txtPort.getText());

		parameterMap.put("port", port);

		task.setParameterMap(parameterMap);

		RestResponse response;
		// Post request
		try {
			response = (RestResponse) TaskRestUtils.execute(task);

			resultMap = response.getResultMap();

		} catch (Exception e1) {
			e1.printStackTrace();
		}

		super.okPressed();
	}

	public Map<String, Object> getResultMap() {
		return resultMap;
	}
}
