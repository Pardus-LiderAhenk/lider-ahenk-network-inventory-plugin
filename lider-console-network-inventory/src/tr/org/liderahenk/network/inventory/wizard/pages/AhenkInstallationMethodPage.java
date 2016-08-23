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
import org.eclipse.swt.widgets.Text;

import tr.org.liderahenk.network.inventory.constants.AccessMethod;
import tr.org.liderahenk.network.inventory.constants.InstallMethod;
import tr.org.liderahenk.network.inventory.i18n.Messages;
import tr.org.liderahenk.network.inventory.model.AhenkSetupConfig;

/**
 * @author Caner Feyzullahoğlu <caner.feyzullahoglu@agem.com.tr>
 */
public class AhenkInstallationMethodPage extends WizardPage {

	private AhenkSetupConfig config = null;

	// Widgets
	private Composite mainContainer = null;

	// private Button useAptGetBtn = null;

	private Button useWgetBtn = null;

	private Text downloadUrlTxt = null;

	// Status variable for the possible errors on this page
	IStatus ipStatus;

	public AhenkInstallationMethodPage(AhenkSetupConfig config) {
		super(AhenkInstallationMethodPage.class.getName(), Messages.getString("INSTALLATION_OF_AHENK"), null);

		setDescription(Messages.getString("BY_WHICH_WAY_WOULD_YOU_LIKE_TO_INSTALL_AHENK"));

		this.config = config;

		ipStatus = new Status(IStatus.OK, "not_used", 0, "", null);
	}

	@Override
	public void createControl(Composite parent) {

		// create main container
		mainContainer = new Composite(parent, SWT.NONE);
		mainContainer.setLayout(new GridLayout(1, false));
		setControl(mainContainer);

		useWgetBtn = new Button(mainContainer, SWT.RADIO);
		useWgetBtn.setText(Messages.getString("INSTALL_FROM_GIVEN_URL"));

		useWgetBtn.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (useWgetBtn.getSelection()) {
					downloadUrlTxt.setEnabled(true);
					updatePageCompleteStatus();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		Composite downloadUrlContainer = new Composite(mainContainer, SWT.NONE);
		GridLayout glDownloadUrl = new GridLayout(1, false);
		downloadUrlContainer.setLayout(glDownloadUrl);

		downloadUrlTxt = new Text(downloadUrlContainer, SWT.BORDER);
		GridData gdDownloadUrlTxt = new GridData();
		gdDownloadUrlTxt.widthHint = 350;
		downloadUrlTxt.setLayoutData(gdDownloadUrlTxt);
		downloadUrlTxt.setEnabled(false);

		downloadUrlTxt.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				updatePageCompleteStatus();
			}
		});

	}

	private void updatePageCompleteStatus() {

		// If install from URL is selected URL must be given
		if (downloadUrlTxt.getText() != null && !"".equals(downloadUrlTxt.getText())) {
			setPageComplete(true);
		} else {
			setPageComplete(false);
		}
	}

	@Override
	public IWizardPage getNextPage() {

		AhenkConfirmPage confPage = (AhenkConfirmPage) super.getNextPage();

		config.setInstallMethod(InstallMethod.WGET);
		config.setDownloadUrl(downloadUrlTxt.getText());
		confPage.getInstallLabel().setText("- " + Messages.getString("USE_GIVEN_URL"));

		if (config.getAccessMethod() == AccessMethod.USERNAME_PASSWORD) {
			confPage.getAccessLabel().setText("- " + Messages.getString("ACCESSING_WITH_USERNAME_AND_PASSWORD"));
		} else {
			confPage.getAccessLabel().setText("- " + Messages.getString("ACCESSING_WITH_PRIVATE_KEY"));
		}

		return super.getNextPage();
	}

}
