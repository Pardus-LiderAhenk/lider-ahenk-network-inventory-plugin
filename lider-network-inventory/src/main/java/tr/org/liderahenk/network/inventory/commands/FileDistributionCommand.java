package tr.org.liderahenk.network.inventory.commands;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
import tr.org.liderahenk.network.inventory.runnables.RunnableFileDistributor;

/**
 * This class is responsible for distributing a file to a number of machines in
 * the given IP list. Safe-copy (SCP) utility command is used to copy file to
 * its destination and it can be configured via plugin configuration file.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class FileDistributionCommand extends BaseCommand {

	private Logger logger = LoggerFactory.getLogger(FileDistributionCommand.class);

	private ICommandResultFactory resultFactory;
	private IOperationLogService logService;
	private IPluginDbService pluginDbService;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ICommandResult execute(ICommandContext context) {

		logger.debug("Executing command.");

		Map<String, Object> parameterMap = context.getRequest().getParameterMap();
		ArrayList<String> ipAddresses = (ArrayList<String>) parameterMap.get("ipAddresses");
		File fileToTransfer = getFileInstance((String) parameterMap.get("file"), (String) parameterMap.get("filename"));
		String username = (String) parameterMap.get("username");
		String password = (String) parameterMap.get("password");
		Integer port = (Integer) (parameterMap.get("port") == null ? 22 : parameterMap.get("port"));
		String privateKey = (String) parameterMap.get("privateKey");
		String destDirectory = (String) parameterMap.get("destDirectory");

		logger.debug("Parameter map: {}", parameterMap);

		// Distribute the provided file via threads
		// Each thread is responsible for a limited number of hosts!
		if (ipAddresses != null && !ipAddresses.isEmpty() && fileToTransfer != null) {

			// Create thread pool executor!
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

			int numberOfHosts = ipAddresses.size();
			int hostsPerThread = numberOfHosts / Constants.SSH_CONFIG.NUM_THREADS;

			logger.debug("Hosts: {}, Threads:{}, Host per Thread: {}",
					new Object[] { numberOfHosts, Constants.SSH_CONFIG.NUM_THREADS, hostsPerThread });

			for (int i = 0; i < numberOfHosts; i += hostsPerThread) {
				int toIndex = i + hostsPerThread;
				List<String> ipSubList = ipAddresses.subList(i,
						toIndex < ipAddresses.size() ? toIndex : ipAddresses.size() - 1);

				RunnableFileDistributor nmap4jThread = new RunnableFileDistributor(ipSubList, username, password, port,
						privateKey, fileToTransfer, destDirectory);
				executor.execute(nmap4jThread);
			}

			executor.shutdown();
		}

		ICommandResult commandResult = resultFactory.create(CommandResultStatus.OK, new ArrayList<String>(), this,
				new HashMap<String, Object>());

		logger.info("Command executed successfully.");

		return commandResult;
	}

	private File getFileInstance(String contents, String filename) {
		File temp = null;
		try {
			temp = File.createTempFile(filename, "");
			// Delete temp file when program exits.
			temp.deleteOnExit();

			// Write to temp file
			BufferedWriter out = new BufferedWriter(new FileWriter(temp));
			out.write(contents);
			out.close();
		} catch (IOException e) {
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
		return "DISTRIBUTEFILE";
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
