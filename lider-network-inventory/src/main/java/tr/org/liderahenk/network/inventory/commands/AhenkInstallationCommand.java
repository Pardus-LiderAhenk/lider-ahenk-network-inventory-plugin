package tr.org.liderahenk.network.inventory.commands;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
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

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.lider.core.api.log.IOperationLogService;
import tr.org.liderahenk.lider.core.api.persistence.IPluginDbService;
import tr.org.liderahenk.lider.core.api.service.ICommandContext;
import tr.org.liderahenk.lider.core.api.service.ICommandResult;
import tr.org.liderahenk.lider.core.api.service.ICommandResultFactory;
import tr.org.liderahenk.lider.core.api.service.enums.CommandResultStatus;
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

		Map<String, Object> parameterMap = context.getRequest().getParameterMap();

		logger.warn("Getting setup parameters.");
		List<String> ipList = (List<String>) parameterMap.get("ipList");
		AccessMethod accessMethod = AccessMethod.valueOf((String) parameterMap.get("accessMethod"));
		InstallMethod installMethod = InstallMethod.valueOf((String) parameterMap.get("installMethod"));
		String username = (String) parameterMap.get("username");
		Integer port = (Integer) parameterMap.get("port");

		String password = null;
		byte[] debFile = null;
		String privateKey = null;
		String passphrase = null;
		String downloadUrl = null;

		if (accessMethod == AccessMethod.USERNAME_PASSWORD) {
			password = (String) parameterMap.get("password");
		} else {
			passphrase = (String) parameterMap.get("passphrase");

			// Get private key location in Lider machine from configuration file
			privateKey = getPrivateKeyLocation();
			logger.warn("Path of private key file: " + privateKey);

			// Passphrase can be null, check it
			// String passphrase = parameterMap.get("passphrase") == null ? null
			// : (String) parameterMap.get("passphrase");
		}
		if (installMethod == InstallMethod.PROVIDED_DEB) {
			// Deserialize before assigning
			// TODO file sending does not work stable
			debFile = Base64.decodeBase64(deserialize(parameterMap.get("debFile")));
		} else if (installMethod == InstallMethod.WGET) {
			downloadUrl = (String) parameterMap.get("downloadUrl");
		}

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

		logger.warn("Getting the location of private key file");

		logger.warn("Creating setup parameters parent entity.");
		// Insert new Ahenk installation parameters.
		// Parent identity object contains installation parameters.
		AhenkSetupParameters setupParams = getParentEntityObject(ipList, accessMethod, username, password, privateKey,
				passphrase, installMethod, debFile, port, downloadUrl);

		logger.warn("passphrase: " + passphrase);

		logger.warn("Starting to create a new runnable to each Ahenk installation.");
		for (final String ip : ipList) {
			// Execute each installation in a new runnable.
			RunnableAhenkInstaller installer = new RunnableAhenkInstaller(ip, username, password, port, privateKey,
					passphrase, debFile, installMethod, downloadUrl, setupParams);

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

		return commandResult;
	}

	private String getPrivateKeyLocation() {
		BufferedReader reader = null;

		// TODO change config file
		try {

			reader = new BufferedReader(new FileReader("/home/caner/lider.config"));

			String sCurrentLine;

			StringBuilder builder = new StringBuilder();

			// TODO this is for temporary testing
			// actually it will read just one property
			// from configuration file
			while ((sCurrentLine = reader.readLine()) != null) {
				builder.append(sCurrentLine);
			}

			return builder.toString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return "";
	}

	public static byte[] deserialize(Object obj) {
		try {
			if (obj != null) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				ObjectOutputStream os = new ObjectOutputStream(out);
				os.writeObject(obj);
				return out.toByteArray();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private AhenkSetupParameters getParentEntityObject(List<String> ipList, AccessMethod accessMethod, String username,
			String password, String privateKey, String passphrase, InstallMethod installMethod, byte[] debFile,
			Integer port, String downloadUrl) {

		// Create an empty result detail entity list
		List<AhenkSetupResultDetail> detailList = new ArrayList<AhenkSetupResultDetail>();

		logger.warn("Creating parent entity object that contains installation parameters");
		// Create setup parameters entity
		AhenkSetupParameters setupResult = new AhenkSetupParameters(null, installMethod.toString(),
				accessMethod.toString(), username, password, port, privateKey, passphrase, new Date(), downloadUrl,
				detailList);

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
	public Boolean executeOnAgent() {
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
