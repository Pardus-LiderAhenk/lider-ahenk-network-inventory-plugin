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
