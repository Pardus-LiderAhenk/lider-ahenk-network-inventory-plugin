package tr.org.liderahenk.network.inventory.commands;

import java.io.IOException;
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

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.lider.core.api.persistence.IPluginDbService;
import tr.org.liderahenk.lider.core.api.service.ICommandContext;
import tr.org.liderahenk.lider.core.api.service.ICommandResult;
import tr.org.liderahenk.lider.core.api.service.ICommandResultFactory;
import tr.org.liderahenk.lider.core.api.service.enums.CommandResultStatus;
import tr.org.liderahenk.network.inventory.contants.Constants;
import tr.org.liderahenk.network.inventory.contants.Constants.AccessMethod;
import tr.org.liderahenk.network.inventory.contants.Constants.InstallMethod;
import tr.org.liderahenk.network.inventory.dto.AhenkSetupDetailDto;
import tr.org.liderahenk.network.inventory.dto.AhenkSetupDto;
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
	private IPluginDbService pluginDbService;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ICommandResult execute(ICommandContext context) {

		Map<String, Object> parameterMap = context.getRequest().getParameterMap();

		logger.debug("Getting setup parameters.");
		List<String> ipList = (List<String>) parameterMap.get("ipList");
		AccessMethod accessMethod = AccessMethod.valueOf((String) parameterMap.get("accessMethod"));
		InstallMethod installMethod = InstallMethod.valueOf((String) parameterMap.get("installMethod"));
		String username = (String) parameterMap.get("username");
		Integer port = (Integer) parameterMap.get("port");

		String password = null;
		String privateKey = null;
		String passphrase = null;
		String downloadUrl = null;

		if (accessMethod == AccessMethod.USERNAME_PASSWORD) {
			password = (String) parameterMap.get("password");
		} else {
			// If access method is private key then set passphrase
			passphrase = (String) parameterMap.get("passphrase");

			// Get private key location in Lider machine from configuration file
			privateKey = getPrivateKeyLocation();
			logger.debug("Path of private key file: " + privateKey);
		}

		if (installMethod == InstallMethod.WGET) {
			downloadUrl = (String) parameterMap.get("downloadUrl");
		}

		LinkedBlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<Runnable>();

		final List<Runnable> running = Collections.synchronizedList(new ArrayList());

		logger.debug("Creating a thread pool.");
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
				logger.debug("Running threads: {}", running);
			}
		};

		logger.debug("Getting the location of private key file");

		logger.debug("Creating setup parameters parent entity.");
		// Insert new Ahenk installation parameters.
		// Parent identity object contains installation parameters.
		AhenkSetupParameters setupParams = getParentEntityObject(ipList, accessMethod, username, password, privateKey,
				passphrase, installMethod, port, downloadUrl);

		logger.debug("passphrase: " + passphrase);

		AhenkSetupDto ahenkSetupDto = new AhenkSetupDto(ipList, accessMethod, username, password, privateKey,
				passphrase, installMethod, port, Collections.synchronizedList(new ArrayList<AhenkSetupDetailDto>()));
		
		logger.debug("Starting to create a new runnable to each Ahenk installation.");
		for (final String ip : ipList) {
			// Execute each installation in a new runnable.
			RunnableAhenkInstaller installer = new RunnableAhenkInstaller(ahenkSetupDto, ip, username, password, port, privateKey,
					passphrase, installMethod, downloadUrl, setupParams);

			logger.debug("Executing installation runnable for: " + ip);

			executor.execute(installer);
		}


		try {
			logger.debug("Shutting down executor service.");
			executor.shutdown();

			logger.debug("Waiting for executor service to finish all tasks.");
			executor.awaitTermination(100000, TimeUnit.MILLISECONDS);

			logger.debug("Executor service finished all tasks.");

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		logger.debug("Saving entities to database.");
		pluginDbService.save(setupParams);

		logger.debug("Entities successfully saved.");

		Map<String, Object> resultMap = new HashMap<String, Object>();
		ObjectMapper mapper = new ObjectMapper();
		try {
			resultMap.put("result", mapper.writeValueAsString(ahenkSetupDto));
		} catch (JsonGenerationException e) {
			logger.error(e.getMessage(), e);
		} catch (JsonMappingException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}

		ICommandResult commandResult = resultFactory.create(CommandResultStatus.OK, new ArrayList<String>(), this,
				resultMap);

		return commandResult;
	}

	private String getPrivateKeyLocation() {
		// TODO change config file
		String privateKeyPath = "~/.ssh/id_rsa"; 

		return privateKeyPath;
	}

	private AhenkSetupParameters getParentEntityObject(List<String> ipList, AccessMethod accessMethod, String username,
			String password, String privateKey, String passphrase, InstallMethod installMethod, Integer port,
			String downloadUrl) {

		// Create an empty result detail entity list
		List<AhenkSetupResultDetail> detailList = new ArrayList<AhenkSetupResultDetail>();

		logger.debug("Creating parent entity object that contains installation parameters");
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

	public void setPluginDbService(IPluginDbService pluginDbService) {
		this.pluginDbService = pluginDbService;
	}

}
