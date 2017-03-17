package tr.org.liderahenk.network.inventory.dialogs;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.dialogs.DefaultTaskDialog;
import tr.org.liderahenk.liderconsole.core.exceptions.ValidationException;
import tr.org.liderahenk.network.inventory.constants.NetworkInventoryConstants;
import tr.org.liderahenk.network.inventory.i18n.Messages;

public class MultipleFileTransferTaskDialog extends DefaultTaskDialog {
	
	private static final Logger logger = LoggerFactory.getLogger(MultipleFileTransferTaskDialog.class);

	private Text txtFilePath;
	private Text txtDestDirectory;

	private Button btnEditUserPermissions;

	private Button btnCanReadUser;
	private Button btnCanWriteUser;
	private Button btnCanExecuteUser;

	private Text txtFileOwnerUser;

	private Button btnEditGroupPermissions;

	private Button btnCanReadGroup;
	private Button btnCanWriteGroup;
	private Button btnCanExecuteGroup;

	private Text txtFileOwnerGroup;

	private Button btnEditOtherPermissions;
	
	private Button btnCanReadOther;
	private Button btnCanWriteOther;
	private Button btnCanExecuteOther;

	public MultipleFileTransferTaskDialog(Shell parentShell, Set<String> dnSet) {
		super(parentShell, dnSet);
	}
	
	@Override
	public String createTitle() {
		return Messages.getString("MULTIPLE_FILE_TRANSFER");
	}

	@Override
	public Control createTaskDialogArea(Composite parent) {

		// Main composite
		final Composite cmpMain = new Composite(parent, SWT.NONE);
		cmpMain.setLayout(new GridLayout(1, false));
		
		Label lblInfo = new Label(cmpMain, SWT.NONE | SWT.SINGLE);
		lblInfo.setText(Messages.getString("SELECT_FILE_TO_SEND"));
		
		Composite cmpBrowseFile= new Composite(parent, SWT.NONE);
		cmpBrowseFile.setLayout(new GridLayout(2, false));
		
		GridData gd = new GridData();
		gd.widthHint = 350;

		txtFilePath = new Text(cmpBrowseFile, SWT.NONE | SWT.BORDER | SWT.SINGLE);
		txtFilePath.setEditable(false);
		txtFilePath.setMessage(Messages.getString("BROWSE_AND_SELECT_FILE"));
		txtFilePath.setLayoutData(gd);
		
		Button button = new Button(cmpBrowseFile, SWT.PUSH | SWT.BORDER);
		button.setText(Messages.getString("BROWSE"));
		button.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				FileDialog dialog = new FileDialog(cmpMain.getShell(), SWT.OPEN);
				dialog.setFilterPath(System.getProperty("user.dir"));
				String open = dialog.open();
				if (open != null) {
					txtFilePath.setText(open);
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
			}
		});
		
		final Composite cmpFileDest = new Composite(parent, SWT.NONE);
		cmpFileDest.setLayout(new GridLayout(1, false));

		Label lblDestDirectory = new Label(cmpFileDest, SWT.NONE | SWT.SINGLE);
		lblDestDirectory.setText(Messages.getString("AHENK_DESTINATION_DIRECTORY"));
		
		txtDestDirectory = new Text(cmpFileDest, SWT.NONE | SWT.BORDER | SWT.SINGLE);
		txtDestDirectory.setMessage(Messages.getString("ENTER_DESTINATION_DIRECTORY_AT_AHENK"));
		
		final Composite cmpPermissions = new Composite(parent, SWT.BORDER);
		cmpPermissions.setLayout(new GridLayout(3, true));
		cmpPermissions.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final Composite cmpUserPermissions = new Composite(cmpPermissions, SWT.BORDER);
		cmpUserPermissions.setLayout(new GridLayout(1, true));
		cmpUserPermissions.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		btnEditUserPermissions = new Button(cmpUserPermissions, SWT.CHECK | SWT.BORDER);
		btnEditUserPermissions.setText(Messages.getString("EDIT_USER_PERMISSONS"));
		btnEditUserPermissions.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (btnEditUserPermissions.getSelection()) {
					btnCanReadUser.setEnabled(true);
					btnCanWriteUser.setEnabled(true);
					btnCanExecuteUser.setEnabled(true);
					txtFileOwnerUser.setEnabled(true);
				} else {
					btnCanReadUser.setEnabled(false);
					btnCanWriteUser.setEnabled(false);
					btnCanExecuteUser.setEnabled(false);
					txtFileOwnerUser.setEnabled(false);
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
			}
		});

		final Composite cmpUserRwe = new Composite(cmpUserPermissions, SWT.NONE);
		cmpUserRwe.setLayout(new GridLayout(3, true));

		btnCanReadUser = new Button(cmpUserRwe, SWT.CHECK | SWT.BORDER);
		btnCanReadUser.setText(Messages.getString("READ"));
		btnCanReadUser.setEnabled(false);
		btnCanWriteUser = new Button(cmpUserRwe, SWT.CHECK | SWT.BORDER);
		btnCanWriteUser.setText(Messages.getString("WRITE"));
		btnCanWriteUser.setEnabled(false);
		btnCanExecuteUser = new Button(cmpUserRwe, SWT.CHECK | SWT.BORDER);
		btnCanExecuteUser.setText(Messages.getString("EXECUTE"));
		btnCanExecuteUser.setEnabled(false);

		final Composite cmpUserOwner = new Composite(cmpUserPermissions, SWT.NONE);
		cmpUserOwner.setLayout(new GridLayout(2, false));

		Label lblFileOwnerUser = new Label(cmpUserOwner, SWT.NONE);
		lblFileOwnerUser.setText(Messages.getString("USER_OWNER"));
		
		txtFileOwnerUser = new Text(cmpUserOwner, SWT.NONE | SWT.BORDER | SWT.SINGLE);
		txtFileOwnerUser.setMessage(Messages.getString("ENTER_USER_OWNER_OF_FILE"));
		txtFileOwnerUser.setEnabled(false);
		
		final Composite cmpGroupPermissions = new Composite(cmpPermissions, SWT.BORDER);
		cmpGroupPermissions.setLayout(new GridLayout(1, true));
		cmpGroupPermissions.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		btnEditGroupPermissions = new Button(cmpGroupPermissions, SWT.CHECK | SWT.BORDER);
		btnEditGroupPermissions.setText(Messages.getString("EDIT_GROUP_PERMISSONS"));
		btnEditGroupPermissions.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (btnEditGroupPermissions.getSelection()) {
					btnCanReadGroup.setEnabled(true);
					btnCanWriteGroup.setEnabled(true);
					btnCanExecuteGroup.setEnabled(true);
					txtFileOwnerGroup.setEnabled(true);
				} else {
					btnCanReadGroup.setEnabled(false);
					btnCanWriteGroup.setEnabled(false);
					btnCanExecuteGroup.setEnabled(false);
					txtFileOwnerGroup.setEnabled(false);
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
			}
		});
		
		final Composite cmpGroupRwe = new Composite(cmpGroupPermissions, SWT.NONE);
		cmpGroupRwe.setLayout(new GridLayout(3, true));
		
		btnCanReadGroup = new Button(cmpGroupRwe, SWT.CHECK | SWT.BORDER);
		btnCanReadGroup.setText(Messages.getString("READ"));
		btnCanReadGroup.setEnabled(false);
		btnCanWriteGroup = new Button(cmpGroupRwe, SWT.CHECK | SWT.BORDER);
		btnCanWriteGroup.setText(Messages.getString("WRITE"));
		btnCanWriteGroup.setEnabled(false);
		btnCanExecuteGroup = new Button(cmpGroupRwe, SWT.CHECK | SWT.BORDER);
		btnCanExecuteGroup.setText(Messages.getString("EXECUTE"));
		btnCanExecuteGroup.setEnabled(false);
		
		final Composite cmpGroupOwner = new Composite(cmpGroupPermissions, SWT.NONE);
		cmpGroupOwner.setLayout(new GridLayout(2, false));
		
		Label lblFileOwnerGroup = new Label(cmpGroupOwner, SWT.NONE);
		lblFileOwnerGroup.setText(Messages.getString("GROUP_OWNER"));
		
		txtFileOwnerGroup = new Text(cmpGroupOwner, SWT.NONE | SWT.BORDER | SWT.SINGLE);
		txtFileOwnerGroup.setMessage(Messages.getString("ENTER_GROUP_OWNER_OF_FILE"));
		txtFileOwnerGroup.setEnabled(false);
		
		final Composite cmpOtherPermissions = new Composite(cmpPermissions, SWT.BORDER);
		cmpOtherPermissions.setLayout(new GridLayout(1, true));
		cmpOtherPermissions.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		btnEditOtherPermissions = new Button(cmpOtherPermissions, SWT.CHECK | SWT.BORDER);
		btnEditOtherPermissions.setText(Messages.getString("EDIT_OTHER_PERMISSONS"));
		btnEditOtherPermissions.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (btnEditOtherPermissions.getSelection()) {
					btnCanReadOther.setEnabled(true);
					btnCanWriteOther.setEnabled(true);
					btnCanExecuteOther.setEnabled(true);
				} else {
					btnCanReadOther.setEnabled(false);
					btnCanWriteOther.setEnabled(false);
					btnCanExecuteOther.setEnabled(false);
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
			}
		});

		final Composite cmpOtherRwe = new Composite(cmpOtherPermissions, SWT.NONE);
		cmpOtherRwe.setLayout(new GridLayout(3, true));

		btnCanReadOther = new Button(cmpOtherRwe, SWT.CHECK | SWT.BORDER);
		btnCanReadOther.setText(Messages.getString("READ"));
		btnCanReadOther.setEnabled(false);
		btnCanWriteOther = new Button(cmpOtherRwe, SWT.CHECK | SWT.BORDER);
		btnCanWriteOther.setText(Messages.getString("WRITE"));
		btnCanWriteOther.setEnabled(false);
		btnCanExecuteOther = new Button(cmpOtherRwe, SWT.CHECK | SWT.BORDER);
		btnCanExecuteOther.setText(Messages.getString("EXECUTE"));
		btnCanExecuteOther.setEnabled(false);

		return cmpMain;
	}

	@Override
	public void validateBeforeExecution() throws ValidationException {
		if (txtFilePath.getText().isEmpty()) {
			throw new ValidationException(Messages.getString("PLEASE_CHOOSE_A_FILE"));
		} else if (txtDestDirectory.getText().isEmpty()) {
			throw new ValidationException(Messages.getString("PLEASE_ENTER_DESTINATION_DIRECTORY"));
		}
	}

	@Override
	public Map<String, Object> getParameterMap() {
		
		// Read file
		byte[] fileArray = readFileAsByteArray(txtFilePath.getText());
		
		String encodedFile = DatatypeConverter.printBase64Binary(fileArray);
		
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put("encodedFile", encodedFile);
		
		if (txtDestDirectory.getText().endsWith("/")) {
			parameterMap.put("localPath", txtDestDirectory.getText());
		} else {
			parameterMap.put("localPath", txtDestDirectory.getText().trim() + "/");
		}
		parameterMap.put("fileName", Paths.get(txtFilePath.getText()).getFileName().toString());

		if (btnEditUserPermissions.getSelection()) {
			parameterMap.put("editUserPermissions", btnEditUserPermissions.getSelection());
			
			parameterMap.put("readUser", btnCanReadUser.getSelection());
			parameterMap.put("writeUser", btnCanWriteUser.getSelection());
			parameterMap.put("executeUser", btnCanExecuteUser.getSelection());
			
			parameterMap.put("ownerUser", txtFileOwnerUser.getText());
		}

		if (btnEditGroupPermissions.getSelection()) {
			parameterMap.put("editGroupPermissions", btnEditGroupPermissions.getSelection());
			
			parameterMap.put("readGroup", btnCanReadGroup.getSelection());
			parameterMap.put("writeGroup", btnCanWriteGroup.getSelection());
			parameterMap.put("executeGroup", btnCanExecuteGroup.getSelection());
			
			parameterMap.put("ownerGroup", txtFileOwnerGroup.getText());
		}

		if (btnEditOtherPermissions.getSelection()) {
			parameterMap.put("editOtherPermissions", btnEditOtherPermissions.getSelection());
			
			parameterMap.put("readOther", btnCanReadOther.getSelection());
			parameterMap.put("writeOther", btnCanWriteOther.getSelection());
			parameterMap.put("executeOther", btnCanExecuteOther.getSelection());
		}
		
		
		return parameterMap;
	}

	@Override
	public String getCommandId() {
		return NetworkInventoryConstants.MULTIPLE_FILE_TRANSFER_COMMAND;
	}

	@Override
	public String getPluginName() {
		return NetworkInventoryConstants.PLUGIN_NAME;
	}

	@Override
	public String getPluginVersion() {
		return NetworkInventoryConstants.PLUGIN_VERSION;
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
			logger.error("Error occurred while reading file: {}", e.getMessage());
			e.printStackTrace();
		}

		return new byte[0];
	}

}
