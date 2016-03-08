package tr.org.liderahenk.network.inventory.commands;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

		logger.warn("Executing command.");

		Map<String, Object> parameterMap = context.getRequest().getParameterMap();

		logger.warn("Getting setup parameters.");
		List<String> ipList = (List<String>) parameterMap.get("ipList");
		AccessMethod accessMethod = AccessMethod.valueOf((String) parameterMap.get("accessMethod"));
		String username = (String) parameterMap.get("username");
		String password = (String) parameterMap.get("password");
		// Deserialize before assigning
		byte[] privateKeyFile = deserialize(parameterMap.get("privateKeyFile"));
		String passphrase = (String) parameterMap.get("passphrase");
		InstallMethod installMethod = InstallMethod.valueOf((String) parameterMap.get("installMethod"));
		// Deserialize before assigning
		byte[] debFile = deserialize(parameterMap.get("debFile"));
		Integer port = (Integer) parameterMap.get("port");

		LinkedBlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<Runnable>();

		final List<Runnable> running = Collections.synchronizedList(new ArrayList());

		logger.warn("Creating a thread pool.");
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
				logger.warn("Running threads: {}", running);
			}
		};

		logger.warn("Creating setup parameters parent entity.");
		// Insert new Ahenk installation parameters.
		// Parent identity object contains installation parameters.
		AhenkSetupParameters setupParams = getParentEntityObject(ipList, accessMethod, username, password,
				privateKeyFile, passphrase, installMethod, debFile, port);


		logger.warn("Starting to create a new runnable to each Ahenk installation.");
		for (final String ip : ipList) {
			// Execute each installation in a new runnable.
			RunnableAhenkInstaller installer = new RunnableAhenkInstaller(ip, username,
					password, port, privateKeyFile, passphrase,
					debFile, installMethod, setupParams);

			logger.warn("Executing installation runnable for: " + ip);

			executor.execute(installer);
		}
		
		logger.warn("Shutting down executor service.");
		executor.shutdown();

		
		// TODO wait for all runnables.
		try {
			logger.warn("Waiting for executor service to finish all tasks.");
			
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

			logger.warn("Executor service finished all tasks.");
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		logger.warn("Saving entities to database.");
		pluginDbService.save(setupParams);

		logger.warn("Entities successfully saved.");

		ICommandResult commandResult = resultFactory.create(CommandResultStatus.OK, new ArrayList<String>(), this,
				new HashMap<String, Object>());

		logger.warn("Command executed successfully.");

		return commandResult;
	}
	
	public static byte[] deserialize(Object obj) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(out);
			os.writeObject(obj);
			return out.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private AhenkSetupParameters getParentEntityObject(List<String> ipList, AccessMethod accessMethod, String username,
			String password, byte[] privateKeyFile, String passphrase, InstallMethod installMethod, byte[] debFile,
			Integer port) {

		// Create an empty result detail entity list
		List<AhenkSetupResultDetail> detailList = new ArrayList<AhenkSetupResultDetail>();

		logger.warn("Creating parent entity object that contains installation parameters");
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
