package tr.org.liderahenk.network.inventory.wizard.pages;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.network.inventory.constants.AccessMethod;
import tr.org.liderahenk.network.inventory.i18n.Messages;
import tr.org.liderahenk.network.inventory.model.AhenkSetupConfig;

/**
 * @author Caner Feyzullahoğlu <caner.feyzullahoglu@agem.com.tr>
 */
public class AhenkConnectionMethodPage extends WizardPage {

	private AhenkSetupConfig config = null;

	// Widgets
	private Composite mainContainer = null;
	private Composite usernameContainer = null;
	private Composite passphraseContainer = null;

	private Button userPassBtn = null;

	private Label userName = null;
	private Text userNameTxt = null;

	private Label password = null;
	private Text passwordTxt = null;

	private Button usePrivateKey = null;

	private Label passphrase = null;
	private Text passphraseTxt = null;
	
	private Label usernameForKeyOption = null;
	private Text usernameTxtForKeyOption = null;
	
	private Label privateKey = null;
	private Text privateKeyTxt = null;
	
	private Button selectPrivateKeyBtn = null;

	private Text portTxt;

	// Status variable for the possible errors on this page
	IStatus ipStatus;

	public AhenkConnectionMethodPage(AhenkSetupConfig config) {
		super(AhenkConnectionMethodPage.class.getName(), Messages.getString("INSTALLATION_OF_AHENK"), null);

		setDescription(Messages.getString("HOW_TO_ACCESS_TO_SELECTED_COMPUTERS"));

		this.config = config;

		ipStatus = new Status(IStatus.OK, "not_used", 0, "", null);
	}

	@Override
	public void createControl(Composite parent) {

		// create main container
		mainContainer = new Composite(parent, SWT.NONE);
		mainContainer.setLayout(new GridLayout(1, false));
		setControl(mainContainer);

		// Access with username and password
		userPassBtn = new Button(mainContainer, SWT.RADIO);

		userPassBtn.setText(Messages.getString("USE_USERNAME_AND_PASSWORD"));
		userPassBtn.setSelection(true);

		userPassBtn.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (userPassBtn.getSelection()) {
					updatePageCompleteStatus();
					organizeFields();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// Group for username and password texts.
		usernameContainer = new Composite(mainContainer, SWT.NONE);
		GridLayout glUsername = new GridLayout(2, false);
		glUsername.marginLeft = 15;
		usernameContainer.setLayout(glUsername);

		userName = new Label(usernameContainer, SWT.SINGLE);
		userName.setText(Messages.getString("USERNAME"));

		// Username text
		userNameTxt = new Text(usernameContainer, SWT.BORDER);
		userNameTxt.setText("root");

		GridData gdUserTxt = new GridData();
		gdUserTxt.widthHint = 170;
		userNameTxt.setLayoutData(gdUserTxt);

		userNameTxt.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				updatePageCompleteStatus();
			}
		});

		// Password text
		password = new Label(usernameContainer, SWT.SINGLE);
		password.setText(Messages.getString("PASSWORD"));

		passwordTxt = new Text(usernameContainer, SWT.BORDER | SWT.PASSWORD);

		GridData gdPasswordTxt = new GridData();
		gdPasswordTxt.widthHint = 170;
		passwordTxt.setLayoutData(gdPasswordTxt);

		passwordTxt.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				updatePageCompleteStatus();
			}
		});

		usePrivateKey = new Button(mainContainer, SWT.RADIO);

		usePrivateKey.setText(Messages.getString("USE_PRIVATE_KEY"));

		usePrivateKey.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updatePageCompleteStatus();
				organizeFields();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// Container for passphrase section
		passphraseContainer = new Composite(mainContainer, SWT.NONE);
		GridLayout glPrivateKey = new GridLayout(3, false);
		glPrivateKey.marginLeft = -6;
		passphraseContainer.setLayout(glPrivateKey);
		
		// PathToPrivateKey label
		privateKey = new Label(passphraseContainer, SWT.SINGLE);
		privateKey.setText(Messages.getString("PRIVATE_KEY"));

		// PathToPrivateKey text field
		privateKeyTxt = new Text(passphraseContainer, SWT.BORDER);
		GridData gdPrivateKey = new GridData();
		gdPrivateKey.widthHint = 97;
		privateKeyTxt.setLayoutData(gdPrivateKey);
		privateKeyTxt.setEnabled(false);
		
		// Select private key
		selectPrivateKeyBtn = new Button(passphraseContainer, SWT.PUSH);
		selectPrivateKeyBtn.setText(Messages.getString("SELECT_KEY"));
		selectPrivateKeyBtn.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(passphraseContainer.getShell(), SWT.OPEN);
				String fn = dialog.open();
				if (fn != null) {
					String filterPath = dialog.getFilterPath();
					String fileName = dialog.getFileName();
					privateKeyTxt.setText(filterPath + "/" + fileName);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		selectPrivateKeyBtn.setEnabled(false);
		
		// Passphrase label
		passphrase = new Label(passphraseContainer, SWT.SINGLE);
		passphrase.setText(Messages.getString("PASSPHRASE(OPTIONAL)"));

		// Passphrase text field
		passphraseTxt = new Text(passphraseContainer, SWT.BORDER | SWT.PASSWORD);
		GridData gdPassphrase = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, true, 2, 1);
		gdPassphrase.widthHint = 97;
		passphraseTxt.setLayoutData(gdPassphrase);
		passphraseTxt.setEnabled(false);
		
		// Username label
		usernameForKeyOption = new Label(passphraseContainer, SWT.SINGLE);
		usernameForKeyOption.setText(Messages.getString("USERNAME"));

		// Username text field
		usernameTxtForKeyOption = new Text(passphraseContainer, SWT.BORDER);
		GridData gdUsernameForKeyOption = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, true, 2, 1);
		gdUsernameForKeyOption.widthHint = 97;
		usernameTxtForKeyOption.setLayoutData(gdUsernameForKeyOption);
		usernameTxtForKeyOption.setEnabled(false);

		// Port section
		Composite portComp = new Composite(mainContainer, SWT.NONE);

		GridLayout glPort = new GridLayout(2, false);
		portComp.setLayout(glPort);

		Label port = new Label(portComp, SWT.SINGLE);
		port.setText(Messages.getString("PLEASE_ENTER_PORT"));

		portTxt = SWTResourceManager.createText(portComp);
		portTxt.setText("22");
		portTxt.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				updatePageCompleteStatus();
			}
		});

		setPageComplete(false);
	}

	private boolean updatePageCompleteStatus() {

		boolean userInfoEntered;
		boolean portEntered;

		// If "Use username and password" is selected, username and password
		// fields must be entered.
		if (userPassBtn.getSelection()) {
			if (!"".equals(userNameTxt.getText()) && userNameTxt.getText() != null && !"".equals(passwordTxt.getText())
					&& passwordTxt.getText() != null) {
				userInfoEntered = true;
			} else {
				userInfoEntered = false;
			}
		} else {
			userInfoEntered = true;
		}

		if (!"".equals(portTxt.getText()) && portTxt.getText() != null) {
			portEntered = true;
		} else {
			portEntered = false;
		}

		setPageComplete(userInfoEntered && portEntered);

		return userInfoEntered && portEntered;
	}

	private void organizeFields() {

		if (userPassBtn.getSelection()) {
			userNameTxt.setEnabled(true);
			passwordTxt.setEnabled(true);

			passphraseTxt.setEnabled(false);
			usernameTxtForKeyOption.setEnabled(false);
			privateKeyTxt.setEnabled(false);
			selectPrivateKeyBtn.setEnabled(false);
		}

		if (usePrivateKey.getSelection()) {
			userNameTxt.setEnabled(false);
			passwordTxt.setEnabled(false);

			passphraseTxt.setEnabled(true);
			usernameTxtForKeyOption.setEnabled(true);
			privateKeyTxt.setEnabled(true);
			selectPrivateKeyBtn.setEnabled(true);
		}
	}

	@Override
	public IWizardPage getNextPage() {

		if (userPassBtn.getSelection()) {
			config.setAccessMethod(AccessMethod.USERNAME_PASSWORD);
			config.setUsername(userNameTxt.getText());
			config.setPassword(passwordTxt.getText());
		} else {
			config.setAccessMethod(AccessMethod.PRIVATE_KEY);
			if (!"".equals(privateKeyTxt.getText()) && privateKeyTxt.getText() != null) {
				config.setPrivateKeyPath(privateKeyTxt.getText());
			}
			if (!"".equals(usernameTxtForKeyOption.getText()) && usernameTxtForKeyOption.getText() != null) {
				config.setUsername(usernameTxtForKeyOption.getText());
			}
			else {
				config.setUsername("root");
			}
			if (!"".equals(passphraseTxt.getText()) && passphraseTxt.getText() != null) {
				config.setPassphrase(passphraseTxt.getText());
			}
		}

		config.setPort(
				portTxt.getText() != null && !portTxt.getText().isEmpty() ? new Integer(portTxt.getText()) : null);

		return super.getNextPage();
	}
}