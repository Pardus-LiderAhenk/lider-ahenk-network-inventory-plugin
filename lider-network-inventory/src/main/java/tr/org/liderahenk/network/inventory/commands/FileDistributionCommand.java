package tr.org.liderahenk.network.inventory.commands;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
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

import javax.xml.bind.DatatypeConverter;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.lider.core.api.persistence.IPluginDbService;
import tr.org.liderahenk.lider.core.api.plugin.ICommand;
import tr.org.liderahenk.lider.core.api.service.ICommandContext;
import tr.org.liderahenk.lider.core.api.service.ICommandResult;
import tr.org.liderahenk.lider.core.api.service.ICommandResultFactory;
import tr.org.liderahenk.lider.core.api.service.enums.CommandResultStatus;
import tr.org.liderahenk.network.inventory.contants.Constants;
import tr.org.liderahenk.network.inventory.contants.Constants.AccessMethod;
import tr.org.liderahenk.network.inventory.dto.FileDistResultDto;
import tr.org.liderahenk.network.inventory.dto.FileDistResultHostDto;
import tr.org.liderahenk.network.inventory.entities.FileDistResult;
import tr.org.liderahenk.network.inventory.entities.FileDistResultHost;
import tr.org.liderahenk.network.inventory.plugininfo.PluginInfoImpl;
import tr.org.liderahenk.network.inventory.runnables.RunnableFileDistributor;

/**
 * This class is responsible for distributing a file to a number of machines in
 * the given IP list. Safe-copy (SCP) utility command is used to copy file to
 * its destination and it can be configured via plugin configuration file.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class FileDistributionCommand implements ICommand {

	private Logger logger = LoggerFactory.getLogger(FileDistributionCommand.class);

	private ICommandResultFactory resultFactory;
	private IPluginDbService pluginDbService;
	private PluginInfoImpl pluginInfo;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ICommandResult execute(ICommandContext context) {

		FileDistResultDto fileDistResultDto = null;

		// Read command parameters.
		Map<String, Object> parameterMap = context.getRequest().getParameterMap();
		
		ArrayList<String> ipAddresses = (ArrayList<String>) parameterMap.get("ipAddresses");
		
		logger.debug("Getting file as byte array from parameter map");
		byte[] fileArray = DatatypeConverter.parseBase64Binary((String) parameterMap.get("file"));
		
		logger.debug("MD5: " + getMD5ofFile(fileArray));

		String filename = (String) parameterMap.get("filename");
		
		logger.debug("Getting file instances");
		File fileToTransfer = getFileInstance(fileArray, filename);
		
		String username = (String) parameterMap.get("username");
		String password = (String) parameterMap.get("password");
		Integer port = (Integer) (parameterMap.get("port") == null ? 22 : parameterMap.get("port"));
		String destDirectory = (String) parameterMap.get("destDirectory");
		AccessMethod accessMethod = AccessMethod.valueOf((String) parameterMap.get("accessMethod"));
		
		logger.debug("Parameter map: {}", parameterMap);

		logger.debug("Getting the location of private key file");

		String privateKey;
		// Get private key location in Lider machine from configuration file
		if (accessMethod == AccessMethod.PRIVATE_KEY) {
			privateKey = (String) parameterMap.get("privateKeyPath");
			logger.debug("Path of private key file: " + privateKey);
		} else {
			privateKey = null;
		}

		String passphrase = (String) parameterMap.get("passphrase");
		
		// Create new instance to send back to Lider Console
		fileDistResultDto = new FileDistResultDto(ipAddresses, fileToTransfer.getName(), username, password, port,
				privateKey, destDirectory, new Date(),
				Collections.synchronizedList(new ArrayList<FileDistResultHostDto>()));

		// Distribute the provided file via threads.
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

			logger.debug("Created thread pool executor for file distribution.");

			// Calculate number of the hosts a thread can process
			int numberOfHosts = ipAddresses.size();
			int hostsPerThread;
			if (numberOfHosts < Constants.SSH_CONFIG.NUM_THREADS) {
				hostsPerThread = 1;
	 		} else {
	 			hostsPerThread = numberOfHosts / Constants.SSH_CONFIG.NUM_THREADS;
	 		}

			logger.debug("Hosts: {}, Threads:{}, Host per Thread: {}",
					new Object[] { numberOfHosts, Constants.SSH_CONFIG.NUM_THREADS, hostsPerThread });

			logger.debug("hostsPerThread: " + hostsPerThread);
			// Create & execute threads
			for (int i = 0; i < numberOfHosts; i += hostsPerThread) {
				List<String> ipSubList;
				if (numberOfHosts < Constants.SSH_CONFIG.NUM_THREADS) {
					ipSubList = ipAddresses.subList(i, i + 1);
				} else {
		 			int toIndex = i + hostsPerThread;
		 			ipSubList = ipAddresses.subList(i,
		 					toIndex < ipAddresses.size() ? toIndex : ipAddresses.size() - 1);
		 		}
				
				logger.debug("Creating runnable no: " + (i + 1));
				RunnableFileDistributor distributor = new RunnableFileDistributor(fileDistResultDto, ipSubList,
						username, password, port, privateKey, passphrase, fileToTransfer, destDirectory);
				
				executor.execute(distributor);
				logger.debug("Runnable no: " + (i + 1) + " executed.");
			}

			logger.debug("Shutting down executor.");

			try {
				executor.shutdown();
				// Wait for all tasks to be completed.
				executor.awaitTermination(100000, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			logger.debug("Saving entity.");
			// Insert new distribution result record
			pluginDbService.save(getEntityObject(fileDistResultDto));
		}

		Map<String, Object> resultMap = new HashMap<String, Object>();
		ObjectMapper mapper = new ObjectMapper();
		try {
			resultMap.put("result", mapper.writeValueAsString(fileDistResultDto));
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
	private FileDistResult getEntityObject(FileDistResultDto dto) {
		FileDistResult entity = new FileDistResult(null, joinIpAddresses(dto.getIpAddresses()), dto.getFileName(),
				dto.getUsername(), dto.getPassword(), dto.getPort(), dto.getPrivateKey(), dto.getDestDirectory(),
				dto.getFileDistDate(), null);
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
	private List<FileDistResultHost> getEntityList(List<FileDistResultHostDto> dtoList, FileDistResult parentEntity) {
		List<FileDistResultHost> entityList = new ArrayList<FileDistResultHost>();
		if (dtoList != null) {
			for (FileDistResultHostDto dto : dtoList) {
				FileDistResultHost entity = new FileDistResultHost(null, parentEntity, dto.getIp(), dto.isSuccess(),
						dto.getErrorMessage());
				entityList.add(entity);
			}
		}
		return entityList;
	}

	/**
	 * Join given IP addresses by comma.
	 * 
	 * @param ipAddresses
	 * @return
	 */
	private String joinIpAddresses(ArrayList<String> ipAddresses) {
		if (ipAddresses != null) {
			StringBuilder ipAddressStr = new StringBuilder("");
			for (String ipAddress : ipAddresses) {
				if (ipAddressStr.length() > 0) {
					ipAddressStr.append(",");
				}
				ipAddressStr.append(ipAddress);
			}
		}
		return null;
	}

	/**
	 * Create a temporary file under /tmp/{timestamp} which can be used for SCP.
	 * 
	 * @author <a href="mailto:caner.feyzullahoglu@agem.com.tr">Caner Feyzullahoğlu</a>
	 * 
	 * @param fileArray
	 * @param filename
	 * @return File
	 */
	private File getFileInstance(byte[] fileArray, String filename) {
		
		// Get temp directory
		String property = "java.io.tmpdir";
		String tempDir = System.getProperty(property);
		
		// Get file separator
		String separator = FileSystems.getDefault().getSeparator();

		// In case of folder name clash use current time as postfix
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy-HH:mm:ss");
		String timestamp = dateFormat.format(date);
		
		String directoryToCreate = tempDir + separator + timestamp; 
		
		Path path = null;
		
		File temp = null;
		try {
			
			path = Paths.get(directoryToCreate);
			
			if (!Files.exists(path)) {
				// Create directory under /tmp
				Files.createDirectories(path);
			}
			
			temp = new File(directoryToCreate + separator + filename);
			
			// Delete temp file when program exits.
			temp.deleteOnExit();

			// Write to temp file
			FileOutputStream outputStream = new FileOutputStream(temp);
			outputStream.write(fileArray);
			outputStream.close();

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
	public Boolean executeOnAgent() {
		return false;
	}

	public void setResultFactory(ICommandResultFactory resultFactory) {
		this.resultFactory = resultFactory;
	}

	public void setPluginDbService(IPluginDbService pluginDbService) {
		this.pluginDbService = pluginDbService;
	}
	
	public void setPluginInfo(PluginInfoImpl pluginInfo) {
		this.pluginInfo = pluginInfo;
	}
	
	private String getMD5ofFile(byte[] inputBytes) {
		
		MessageDigest digest;
		String result=null;
		try {
			digest = MessageDigest.getInstance("MD5");
			byte[] hashBytes = digest.digest(inputBytes);
			
			final StringBuilder builder = new StringBuilder();
		    for(byte b : hashBytes) {
		        builder.append(String.format("%02x", b));
		    }
		    result=builder.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return result;
	}

	@Override
	public String getPluginName() {
		return pluginInfo.getPluginName();
	}

	@Override
	public String getPluginVersion() {
		return pluginInfo.getPluginVersion();
	}
	
}
