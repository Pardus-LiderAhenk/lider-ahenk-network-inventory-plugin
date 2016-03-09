package tr.org.liderahenk.network.inventory.wizard.pages;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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

import tr.org.liderahenk.liderconsole.core.utils.GUIHelper;
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
	private Composite fileDialogContainer = null;
	private Composite usernameContainer = null;
	private Composite privateKeyContainer = null;
	private Composite passphraseContainer = null;

	private Button userPassBtn = null;

	private Label userName = null;
	private Text userNameTxt = null;

	private Label password = null;
	private Text passwordTxt = null;

	private Button usePrivateKey = null;

	private Text fileDialogText = null;
	private Button fileDialogBtn = null;

	private Label passphrase = null;
	private Text passphraseTxt = null;

	private FileDialog fileDialog = null;

	private String fileDialogResult = null;

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

		privateKeyContainer = new Composite(mainContainer, SWT.NONE);
		GridLayout glPrivateKey = new GridLayout(1, false);
		glPrivateKey.marginLeft = 15;
		privateKeyContainer.setLayout(glPrivateKey);

		fileDialogContainer = new Composite(privateKeyContainer, SWT.NONE);
		GridLayout glFileDialog = new GridLayout(2, false);
		glFileDialog.marginLeft = -6;
		// Adjust button near to text field
		glFileDialog.horizontalSpacing = -3;
		fileDialogContainer.setLayout(glFileDialog);

		// File dialog window
		fileDialog = new FileDialog(mainContainer.getShell(), SWT.SAVE);
		fileDialog.setText(Messages.getString("UPLOAD_KEY"));

		// Upload key text field
		fileDialogText = new Text(fileDialogContainer, SWT.BORDER);
		fileDialogText.setEnabled(false);
		GridData gdFileDialogTxt = new GridData();
		gdFileDialogTxt.widthHint = 247;

		fileDialogText.setLayoutData(gdFileDialogTxt);
		fileDialogText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				updatePageCompleteStatus();
			}
		});

		// Upload key push button
		fileDialogBtn = new Button(fileDialogContainer, SWT.PUSH);
		fileDialogBtn.setText(Messages.getString("UPLOAD_KEY"));

		GridData gdFileDialogBtn = new GridData();
		gdFileDialogBtn.heightHint = 25;
		gdFileDialogBtn.widthHint = 125;
		fileDialogBtn.setLayoutData(gdFileDialogBtn);
		fileDialogBtn.setEnabled(false);

		fileDialogBtn.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fileDialogResult = fileDialog.open();
				if (fileDialogResult != null && !"".equals(fileDialogResult)) {
					fileDialogText.setText(fileDialogResult);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// Container for passphrase section
		passphraseContainer = new Composite(privateKeyContainer, SWT.NONE);

		// Passphrase label
		passphrase = new Label(passphraseContainer, SWT.SINGLE);
		passphrase.setText(Messages.getString("PASSPHRASE(OPTIONAL)"));
		GridLayout glPassphrase = new GridLayout(2, false);
		glPassphrase.marginLeft = -6;
		passphraseContainer.setLayout(glPassphrase);

		// Passphrase text field
		passphraseTxt = new Text(passphraseContainer, SWT.BORDER | SWT.PASSWORD);
		GridData gdPassphrase = new GridData();
		gdPassphrase.widthHint = 97;
		passphraseTxt.setLayoutData(gdPassphrase);
		passphraseTxt.setEnabled(false);

		// Port section
		Composite portComp = new Composite(mainContainer, SWT.NONE);

		GridLayout glPort = new GridLayout(2, false);
		portComp.setLayout(glPort);

		Label port = new Label(portComp, SWT.SINGLE);
		port.setText(Messages.getString("PLEASE_ENTER_PORT"));

		portTxt = GUIHelper.createText(portComp);
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
		boolean privateKeyEntered;
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

		// If "use private key is selected", private key must be selected from
		// file system
		if (usePrivateKey.getSelection()) {
			if (!"".equals(fileDialogText.getText()) && fileDialogText.getText() != null) {
				privateKeyEntered = true;
			} else {
				privateKeyEntered = false;
			}
		} else {
			privateKeyEntered = true;
		}

		if (!"".equals(portTxt.getText()) && portTxt.getText() != null) {
			portEntered = true;
		} else {
			portEntered = false;
		}

		setPageComplete(userInfoEntered && privateKeyEntered && portEntered);

		return userInfoEntered && privateKeyEntered && portEntered;
	}

	private void organizeFields() {

		if (userPassBtn.getSelection()) {
			userNameTxt.setEnabled(true);
			passwordTxt.setEnabled(true);

			passphraseTxt.setEnabled(false);
			fileDialogText.setEnabled(false);
			fileDialogBtn.setEnabled(false);
		}

		if (usePrivateKey.getSelection()) {
			userNameTxt.setEnabled(false);
			passwordTxt.setEnabled(false);

			passphraseTxt.setEnabled(true);
			fileDialogText.setEnabled(true);
			fileDialogText.setEditable(false);
			fileDialogBtn.setEnabled(true);
		}
	}

	/**
	 * Read the file from provided path and returns it as an array of bytes.
	 * 
	 * @author Caner Feyzullahoğlu <caner.feyzullahoglu@agem.com.tr>
	 * 
	 * @param filePath
	 *            Absolute path to file
	 * @return given file as byte[]
	 */
	private byte[] readFileAsByteArray(String pathOfFile) {
		
		String content = null;

		File file = new File(pathOfFile);
		
		try (FileReader reader = new FileReader(file)) {
			
			char[] chars = new char[(int) file.length()];
			
			reader.read(chars);
			
			content = new String(chars);
			
			content.getBytes();
			
			return content.getBytes();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new byte[0];
	}

	@Override
	public IWizardPage getNextPage() {

		if (userPassBtn.getSelection()) {
			config.setAccessMethod(AccessMethod.USERNAME_PASSWORD);
			config.setUsername(userNameTxt.getText());
			config.setPassword(passwordTxt.getText());
		} else {
			config.setAccessMethod(AccessMethod.PRIVATE_KEY);
			config.setUsername("root");
			config.setPrivateKeyFile((byte[]) readFileAsByteArray(fileDialogText.getText()));
			if (!"".equals(passphraseTxt.getText()) && passphraseTxt.getText() != null) {
				config.setPassphrase(passphraseTxt.getText());
			}
		}

		config.setPort(
				portTxt.getText() != null && !portTxt.getText().isEmpty() ? new Integer(portTxt.getText()) : null);

		return super.getNextPage();
	}
}