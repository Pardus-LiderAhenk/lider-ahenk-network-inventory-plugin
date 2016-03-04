package tr.org.liderahenk.network.inventory.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.lider.core.api.log.IOperationLogService;
import tr.org.liderahenk.lider.core.api.plugin.CommandResultStatus;
import tr.org.liderahenk.lider.core.api.plugin.ICommandContext;
import tr.org.liderahenk.lider.core.api.plugin.ICommandResult;
import tr.org.liderahenk.lider.core.api.plugin.ICommandResultFactory;
import tr.org.liderahenk.lider.core.api.plugin.IPluginDbService;
import tr.org.liderahenk.network.inventory.contants.Constants;
import tr.org.liderahenk.network.inventory.contants.Constants.AccessMethod;
import tr.org.liderahenk.network.inventory.contants.Constants.InstallMethod;
import tr.org.liderahenk.network.inventory.entities.AhenkSetupParameters;
import tr.org.liderahenk.network.inventory.entities.AhenkSetupResultDetail;
import tr.org.liderahenk.network.inventory.runnables.RunnableAhenkInstaller;

/**
 * This class is responsible for installing Ahenk packages into the specified
 * machines. It can install via provided ahenk.deb file or apt-get.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class AhenkInstallationCommand extends BaseCommand {

	private Logger logger = LoggerFactory.getLogger(AhenkInstallationCommand.class);

	private ICommandResultFactory resultFactory;
	private IOperationLogService logService;
	private IPluginDbService pluginDbService;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ICommandResult execute(ICommandContext context) {

		logger.info("Executing command.");

		Map<String, Object> parameterMap = context.getRequest().getParameterMap();

		logger.info("Getting setup parameters.");
		List<String> ipList = (List<String>) parameterMap.get("ipList");
		AccessMethod accessMethod = AccessMethod.valueOf((String) parameterMap.get("accessMethod"));
		String username = (String) parameterMap.get("username");
		String password = (String) parameterMap.get("password");
		byte[] privateKeyFile = (byte[]) parameterMap.get("privateKeyFile");
		String passphrase = (String) parameterMap.get("passphrase");
		InstallMethod installMethod = InstallMethod.valueOf((String) parameterMap.get("installMethod"));
		byte[] debFile = (byte[]) parameterMap.get("debFile");
		Integer port = (Integer) parameterMap.get("port");

		LinkedBlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<Runnable>();

		final List<Runnable> running = Collections.synchronizedList(new ArrayList());

		logger.info("Creating a thread pool.");
		ThreadPoolExecutor executor = new ThreadPoolExecutor(Constants.SSH_CONFIG.NUM_THREADS,
				Constants.SSH_CONFIG.NUM_THREADS, 0L, TimeUnit.MILLISECONDS, taskQueue,
				Executors.defaultThreadFactory()) {

			@Override
			protected <T> RunnableFuture<T> newTaskFor(final Runnable runnable, T value) {
				return new FutureTask<T>(runnable, value) {
					@Override
					public String toString() {
						return runnable.toString();
					}
				};
			}

			@Override
			protected void beforeExecute(Thread t, Runnable r) {
				super.beforeExecute(t, r);
				running.add(r);
			}

			@Override
			protected void afterExecute(Runnable r, Throwable t) {
				super.afterExecute(r, t);
				running.remove(r);
				logger.info("Running threads: {}", running);
			}
		};

		logger.info("Saving installation parameters parent entity.");
		// Insert new Ahenk installation parameters.
		// Parent identity object contains installation parameters.
		AhenkSetupParameters setupParams = getParentEntityObject(ipList, accessMethod, username, password,
				privateKeyFile, passphrase, installMethod, debFile, port);

		 pluginDbService.save(setupParams);

		logger.info("Starting to create a new runnable to each Ahenk installation.");
		for (final String ip : ipList) {
			// Execute each installation in a new runnable.
			RunnableAhenkInstaller installer = new RunnableAhenkInstaller(ip, username,
					password, port, privateKeyFile, passphrase,
					debFile, installMethod);

			logger.info("Executing installation runnable for: " + ip);

			executor.execute(installer);
		}

		ICommandResult commandResult = resultFactory.create(CommandResultStatus.OK, new ArrayList<String>(), this,
				new HashMap<String, Object>());

		logger.info("Command executed successfully.");

		return commandResult;
	}

	private AhenkSetupParameters getParentEntityObject(List<String> ipList, AccessMethod accessMethod, String username,
			String password, byte[] privateKeyFile, String passphrase, InstallMethod installMethod, byte[] debFile,
			Integer port) {

		// Create an empty result detail entity list
		List<AhenkSetupResultDetail> detailList = new ArrayList<AhenkSetupResultDetail>();

		logger.info("Creating parent entity object that contains installation parameters");
		// Create setup parameters entity
		AhenkSetupParameters setupResult = new AhenkSetupParameters(null, installMethod.toString(),
				accessMethod.toString(), username, password, port,
				privateKeyFile, passphrase, new Date(), detailList);

		return setupResult;
	}

	@Override
	public ICommandResult validate(ICommandContext context) {
		return resultFactory.create(CommandResultStatus.OK, null, this, null);
	}

	@Override
	public String getCommandId() {
		return "INSTALLAHENK";
	}

	@Override
	public Boolean needsTask() {
		return false;
	}

	public void setResultFactory(ICommandResultFactory resultFactory) {
		this.resultFactory = resultFactory;
	}

	public void setLogService(IOperationLogService logService) {
		this.logService = logService;
	}

	public void setPluginDbService(IPluginDbService pluginDbService) {
		this.pluginDbService = pluginDbService;
	}

}
