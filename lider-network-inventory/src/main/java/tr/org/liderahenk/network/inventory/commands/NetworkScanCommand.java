package tr.org.liderahenk.network.inventory.commands;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
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

import tr.org.liderahenk.lider.core.api.log.IOperationLogService;
import tr.org.liderahenk.lider.core.api.persistence.IPluginDbService;
import tr.org.liderahenk.lider.core.api.service.ICommandContext;
import tr.org.liderahenk.lider.core.api.service.ICommandResult;
import tr.org.liderahenk.lider.core.api.service.ICommandResultFactory;
import tr.org.liderahenk.lider.core.api.service.enums.CommandResultStatus;
import tr.org.liderahenk.network.inventory.contants.Constants;
import tr.org.liderahenk.network.inventory.dto.ScanResultDto;
import tr.org.liderahenk.network.inventory.dto.ScanResultHostDto;
import tr.org.liderahenk.network.inventory.entities.ScanResult;
import tr.org.liderahenk.network.inventory.entities.ScanResultHost;
import tr.org.liderahenk.network.inventory.runnables.RunnableNmap;
import tr.org.liderahenk.network.inventory.utils.network.NetworkUtils;

/**
 * This class is responsible for scanning network and retrieving information
 * about open ports, services, OS guess, IP & MAC addresses. In order to scan
 * network faster, the operation will be divided and executed by a number of
 * threads. Network mapper (nmap) utility command is used to scan a network
 * which is highly reliable and configurable.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class NetworkScanCommand extends BaseCommand {

	private Logger logger = LoggerFactory.getLogger(NetworkScanCommand.class);

	private ICommandResultFactory resultFactory;
	private IOperationLogService logService;
	private IPluginDbService pluginDbService;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ICommandResult execute(ICommandContext context) {

		logger.debug("Executing command.");

		ScanResultDto scanResultDto = null;

		// Read command parameters.
		Map<String, Object> parameterMap = context.getRequest().getParameterMap();
		Boolean readLast = (Boolean) parameterMap.get("readLast");
		String ipRange = (String) parameterMap.get("ipRange");
		String ports = (String) parameterMap.get("ports");
		String sudoUsername = (String) parameterMap.get("sudoUsername");
		String sudoPassword = (String) parameterMap.get("sudoPassword");
		String timingTemplate = (String) parameterMap.get("timingTemplate");

		logger.debug("Parameter map: {}", parameterMap);

		// Find last network scan!
		if (readLast != null && readLast.booleanValue()) {
			// TODO scanResult
		}
		// New network scan.
		else {

			// Create new instance to send back to Lider Console
			scanResultDto = new ScanResultDto(ipRange, timingTemplate, ports, sudoUsername, sudoPassword, new Date(),
					Collections.synchronizedList(new ArrayList<ScanResultHostDto>()));

			// If user provides an IP range, scan only it!
			// otherwise find all IP addresses on the connected networks
			List<String> ipAddresses = null;
			try {
				if (ipRange != null && !ipRange.isEmpty()) {
					ipAddresses = NetworkUtils.convertToIpList(ipRange);
				} else {
					ipAddresses = NetworkUtils.findIpAddresses();
				}
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			} catch (SocketException e1) {
				e1.printStackTrace();
			}

			// Scan network via threads.
			// Each thread is responsible for a limited number of hosts!
			if (ipAddresses != null && !ipAddresses.isEmpty()) {

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

				logger.debug("Created thread pool executor for network scan.");

				// Calculate number of the hosts a thread can process
				int numberOfHosts = ipAddresses.size();
				int hostsPerThread = numberOfHosts / Constants.SSH_CONFIG.NUM_THREADS;

				logger.debug("Hosts: {}, Threads:{}, Host per Thread: {}",
						new Object[] { numberOfHosts, Constants.SSH_CONFIG.NUM_THREADS, hostsPerThread });

				// Create & execute threads
				for (int i = 0; i < numberOfHosts; i += hostsPerThread) {
					int toIndex = i + hostsPerThread;
					List<String> ipSubList = ipAddresses.subList(i,
							toIndex < ipAddresses.size() ? toIndex : ipAddresses.size() - 1);
					String ipSubRange = NetworkUtils.convertToIpRange(ipSubList);

					RunnableNmap nmap = new RunnableNmap(scanResultDto, ipSubRange, ports, sudoUsername, sudoPassword,
							timingTemplate);
					executor.execute(nmap);
				}

				executor.shutdown();

				// Insert new scan result record
				pluginDbService.save(getEntityObject(scanResultDto));
			}

		}

		logger.info("Command executed successfully.");

		Map<String, Object> resultMap = new HashMap<String, Object>();
		ObjectMapper mapper = new ObjectMapper();
		try {
			resultMap.put("result", mapper.writeValueAsString(scanResultDto));
		} catch (JsonGenerationException e) {
			logger.error(e.getMessage(), e);
		} catch (JsonMappingException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}

		return resultFactory.create(CommandResultStatus.OK, new ArrayList<String>(), this, resultMap);
	}

	/**
	 * Convert data transfer object to entity object.
	 * 
	 * @param dto
	 * @return
	 */
	private ScanResult getEntityObject(ScanResultDto dto) {
		ScanResult entity = new ScanResult(null, dto.getIpRange(), dto.getTimingTemplate(), dto.getPorts(),
				dto.getSudoUsername(), dto.getSudoPassword(), dto.getScanDate(), null);
		entity.setHosts(getEntityList(dto.getHosts(), entity));
		return entity;
	}

	/**
	 * Convert list of data transfer objects to list of entity objects.
	 * 
	 * @param dtoList
	 * @param parentEntity
	 * @return
	 */
	private List<ScanResultHost> getEntityList(List<ScanResultHostDto> dtoList, ScanResult parentEntity) {
		List<ScanResultHost> entityList = new ArrayList<ScanResultHost>();
		if (dtoList != null) {
			for (ScanResultHostDto dto : dtoList) {
				ScanResultHost entity = new ScanResultHost(null, parentEntity, dto.getHostname(), dto.getIp(),
						dto.isHostUp(), dto.getOpenPorts(), dto.getOsGuess(), dto.getDistance(), dto.getUptime(),
						dto.getMac(), dto.getVendor());
				entityList.add(entity);
			}
		}
		return entityList;
	}

	@Override
	public ICommandResult validate(ICommandContext context) {
		return resultFactory.create(CommandResultStatus.OK, null, this, null);
	}

	@Override
	public String getCommandId() {
		return "SCANNETWORK";
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
