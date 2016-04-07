package tr.org.liderahenk.network.inventory.wizard;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import tr.org.liderahenk.liderconsole.core.rest.requests.TaskRequest;
import tr.org.liderahenk.liderconsole.core.rest.responses.RestResponse;
import tr.org.liderahenk.liderconsole.core.rest.utils.TaskUtils;
import tr.org.liderahenk.network.inventory.constants.AccessMethod;
import tr.org.liderahenk.network.inventory.constants.InstallMethod;
import tr.org.liderahenk.network.inventory.model.AhenkSetupConfig;
import tr.org.liderahenk.network.inventory.wizard.pages.AhenkConfirmPage;
import tr.org.liderahenk.network.inventory.wizard.pages.AhenkConnectionMethodPage;
import tr.org.liderahenk.network.inventory.wizard.pages.AhenkInstallationMethodPage;
public class AhenkSetupWizard extends Wizard {

	public AhenkSetupWizard(List<String> ipList) {
		super();
		this.config.setIpList(ipList);
	}

	/**
	 * The instance which holds all the configuration variables throughout the
	 * setup process. It is shared by all wizard pages.
	 */
	private AhenkSetupConfig config = new AhenkSetupConfig();

	/**
	 * This wizard's list of pages (element type: <code>IWizardPage</code>).
	 */
	private LinkedList<IWizardPage> pages = new LinkedList<IWizardPage>();

	/**
	 * Setup wizard pages.
	 * 
	 * Other pages will be added dynamically according to user action!
	 */
	AhenkConnectionMethodPage accessPage = new AhenkConnectionMethodPage(config);
	AhenkInstallationMethodPage installMethodPage = new AhenkInstallationMethodPage(config);
	AhenkConfirmPage confirmPage = new AhenkConfirmPage(config);

	@Override
	public void addPages() {
		// Add first page as default, so the wizard can show it on startup
		addPage(accessPage);
		addPage(installMethodPage);
		addPage(confirmPage);

		// set this to true to override needsPreviousAndNextButtons() method
		setForcePreviousAndNextButtons(true);
	}

	/**
	 * Adds a new page to this wizard. The page is inserted at the end of the
	 * page list.
	 * 
	 * @param page
	 *            the new page
	 */
	public void addPage(IWizardPage page) {
		pages.add(page);
		page.setWizard(this);
	}

	/**
	 * Inserts a new page to this wizard at the specified position.
	 * 
	 * @param page
	 *            the new page
	 */
	public void addPage(IWizardPage page, int index) {
		pages.add(index, page);
		page.setWizard(this);
	}

	public LinkedList<IWizardPage> getPagesList() {
		return pages;
	}

	/**
	 * The <code>Wizard</code> implementation of this <code>IWizard</code>
	 * method creates all the pages controls using
	 * <code>IDialogPage.createControl</code>. Subclasses should reimplement
	 * this method if they want to delay creating one or more of the pages
	 * lazily. The framework ensures that the contents of a page will be created
	 * before attempting to show it.
	 */
	@Override
	public void createPageControls(Composite pageContainer) {
		// the default behavior is to create all the pages controls
		for (int i = 0; i < pages.size(); i++) {
			IWizardPage page = pages.get(i);
			page.createControl(pageContainer);
			// page is responsible for ensuring the created control is
			// accessible
			// via getControl.
			Assert.isNotNull(page.getControl(),
					"getControl() of wizard page returns null. Did you call setControl() in your wizard page?"); //$NON-NLS-1$
		}
	}

	/**
	 * The <code>Wizard</code> implementation of this <code>IWizard</code>
	 * method disposes all the pages controls using
	 * <code>DialogPage.dispose</code>. Subclasses should extend this method if
	 * the wizard instance maintains addition SWT resource that need to be
	 * disposed.
	 */
	@Override
	public void dispose() {
		super.dispose();
		// notify pages
		for (int i = 0; i < pages.size(); i++) {
			try {
				pages.get(i).dispose();
			} catch (Exception e) {
				Status status = new Status(IStatus.ERROR, Policy.JFACE, IStatus.ERROR, e.getMessage(), e);
				Policy.getLog().log(status);
			}
		}
	}

	/*
	 * (non-Javadoc) Method declared on IWizard. The default behavior is to
	 * return the page that was added to this wizard after the given page.
	 */
	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		int index = pages.indexOf(page);
		if (index == pages.size() - 1 || index == -1) {
			// last page or page not found
			return null;
		}
		return pages.get(index + 1);
	}

	/*
	 * (non-Javadoc) Method declared on IWizard.
	 */
	@Override
	public IWizardPage getPage(String name) {
		for (int i = 0; i < pages.size(); i++) {
			IWizardPage page = pages.get(i);
			String pageName = page.getName();
			if (pageName.equals(name)) {
				return page;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc) Method declared on IWizard.
	 */
	@Override
	public int getPageCount() {
		return pages.size();
	}

	/*
	 * (non-Javadoc) Method declared on IWizard.
	 */
	@Override
	public IWizardPage[] getPages() {
		return pages.toArray(new IWizardPage[pages.size()]);
	}

	/*
	 * (non-Javadoc) Method declared on IWizard. The default behavior is to
	 * return the page that was added to this wizard before the given page.
	 */
	@Override
	public IWizardPage getPreviousPage(IWizardPage page) {
		int index = pages.indexOf(page);
		if (index == 0 || index == -1) {
			// first page or page not found
			return null;
		}
		return pages.get(index - 1);
	}

	/*
	 * (non-Javadoc) Method declared on IWizard. By default this is the first
	 * page inserted into the wizard.
	 */
	@Override
	public IWizardPage getStartingPage() {
		if (pages.size() == 0) {
			return null;
		}
		return pages.get(0);
	}

	@Override
	public boolean performFinish() {
		
		final Display display = Display.getCurrent();
		
		Runnable runnable = new Runnable() {
			
			@Override
			public void run() {
				// -- TODO Here will be in different thread so wizard will be closed immediately. ---- //
				// Create request object
				TaskRequest task = new TaskRequest();
				task.setPluginName("network-inventory");
				task.setPluginVersion("1.0.0-SNAPSHOT");
				task.setCommandId("INSTALLAHENK");

				// Add config object as parameter. It has all information that Lider needs to know.
				Map<String, Object> parameterMap = new HashMap<String, Object>();

				// Put parameters to map
				parameterMap.put("ipList", config.getIpList());
				parameterMap.put("accessMethod", config.getAccessMethod());
				parameterMap.put("installMethod", config.getInstallMethod());
				parameterMap.put("username", config.getUsername());
				parameterMap.put("port", config.getPort());
				
				if (config.getAccessMethod() == AccessMethod.USERNAME_PASSWORD) {
					parameterMap.put("password", config.getPassword());
				}
				else {
					parameterMap.put("passphrase", config.getPassphrase());
				}

				if (config.getInstallMethod() == InstallMethod.WGET) {
					parameterMap.put("downloadUrl", config.getDownloadUrl());
				}
				
				task.setParameterMap(parameterMap);
				
				// TODO open a loading dialog(take it from installer project)
				
				// Send command
				RestResponse response;
				try {
					response = (RestResponse) TaskUtils.execute(task);
					Map<String, Object> resultMap = response.getResultMap();
				} catch (Exception e) {
					e.printStackTrace();
					// TODO change loading dialog as: "Error occured."
				}
				
				// ------------------------------------------------------------ //
				
			}
		};
		
		display.asyncExec(runnable);
		
		return true;
	}

	@Override
	public boolean canFinish() {

		// If current page is the last page of wizard.
		if (getContainer().getCurrentPage().getName().equals(pages.getLast().getName())) {
			return true;
		}

		return false;
	}

}
