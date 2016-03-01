package tr.org.liderahenk.network.inventory.commands;

import java.io.File;
import java.io.FileOutputStream;
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
	private IOperationLogService logService;
	private IPluginDbService pluginDbService;

	@Override
	public ICommandResult execute(ICommandContext context) {

		logger.debug("Executing command.");

		Map<String, Object> parameterMap = context.getRequest().getParameterMap();

		final AhenkSetupDto config = (AhenkSetupDto) parameterMap.get("config");

		// TODO farklı threadlerde ahenk yükle.
		// final ExecutorService executor = Executors.newCachedThreadPool();

		LinkedBlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<Runnable>();

		final List<Runnable> running = Collections.synchronizedList(new ArrayList());

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

		// Insert new Ahenk installation parameters.
		// Parent identity object contains installation parameters.
		pluginDbService.save(getParentEntityObject(config));

		for (final String ip : config.getIpList()) {
			// Execute each installation in a new runnable.
			RunnableAhenkInstaller installer = new RunnableAhenkInstaller(ip, config.getUsername(),
					config.getPassword(), config.getPort(), config.getPrivateKeyFile(), config.getPassphrase(),
					config.getDebFile(), config.getInstallMethod());

			executor.execute(installer);
		}

		ICommandResult commandResult = resultFactory.create(CommandResultStatus.OK, new ArrayList<String>(), this,
				new HashMap<String, Object>());

		logger.info("Command executed successfully.");

		return commandResult;
	}

	private AhenkSetupParameters getParentEntityObject(AhenkSetupDto config) {
		
		// Create an empty result detail entity list
		List<AhenkSetupResultDetail> detailList = new ArrayList<AhenkSetupResultDetail>();
		
		// Create setup parameters entity
		AhenkSetupParameters setupResult = new AhenkSetupParameters(null, config.getInstallMethod().toString(),
				config.getAccessMethod().toString(), config.getUsername(), config.getPassword(), config.getPort(),
				config.getPrivateKeyFile(), config.getPassphrase(), new Date(), detailList);

		return setupResult;
	}

	/**
	 * Creates a temporary file from an array of bytes.
	 * 
	 * @author Caner Feyzullahoğlu <caner.feyzullahoglu@agem.com.tr>
	 * 
	 * @param contents
	 * 
	 * @param filename
	 * 
	 * @return File
	 */
	private File byteArrayToFile(byte[] content, String filename) {

		FileOutputStream fileOutputStream = null;

		File temp = null;

		try {

			fileOutputStream = new FileOutputStream(temp);

			temp = File.createTempFile(filename, "");
			// Delete temp file when program exits.
			temp.deleteOnExit();

			// Write to temp file
			fileOutputStream.write(content);
			fileOutputStream.close();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return temp;
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
