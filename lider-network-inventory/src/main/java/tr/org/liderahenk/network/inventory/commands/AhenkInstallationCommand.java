package tr.org.liderahenk.network.inventory.commands;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.lider.core.api.log.IOperationLogService;
import tr.org.liderahenk.lider.core.api.plugin.CommandResultStatus;
import tr.org.liderahenk.lider.core.api.plugin.ICommandContext;
import tr.org.liderahenk.lider.core.api.plugin.ICommandResult;
import tr.org.liderahenk.lider.core.api.plugin.ICommandResultFactory;
import tr.org.liderahenk.lider.core.api.plugin.IPluginDbService;
import tr.org.liderahenk.network.inventory.contants.Constants.InstallMethod;
import tr.org.liderahenk.network.inventory.dto.AhenkSetupDto;
import tr.org.liderahenk.network.inventory.exception.CommandExecutionException;
import tr.org.liderahenk.network.inventory.exception.SSHConnectionException;
import tr.org.liderahenk.network.inventory.utils.setup.SetupUtils;

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
		final ExecutorService executor = Executors.newCachedThreadPool();
		
		// Check installation method
		if (config.getInstallMethod() == InstallMethod.APT_GET) {
			for (final String ip : config.getIpList()) {
				// Execute each installation in a new runnable.
				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						try {
							// Check authorization before starting installation
							final boolean canConnect = SetupUtils.canConnectViaSsh(ip,
									config.getUsername(), config.getPassword(), config.getPort(),
									config.getPrivateKeyFile(), config.getPassphrase());
							
							// If we can connect to machine install Ahenk
							if (canConnect) {
								// TODO gedit değiştirilecek
								SetupUtils.installPackage(ip, config.getUsername(),
										config.getPassword(), config.getPort(), config.getPrivateKeyFile(), "gedit",
										null);
								
								// TODO successfully installed logu düş
								
							} else {
								// TODO could not connect logu düş
							}
							
						} catch (SSHConnectionException e) {
							e.printStackTrace();
						} catch (CommandExecutionException e) {
							e.printStackTrace();
						}
					}
				};
				
				executor.execute(runnable);
			}
		}
		else if (config.getInstallMethod() == InstallMethod.PROVIDED_DEB) {
			for (final String ip : config.getIpList()) {
				// Execute each installation in a new runnable.
				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						try {
							// Check authorization before starting installation
							final boolean canConnect = SetupUtils.canConnectViaSsh(ip,
									config.getUsername(), config.getPassword(), config.getPort(),
									config.getPrivateKeyFile(), config.getPassphrase());
							
							// If we can connect to machine install Ahenk
							if (canConnect) {
								
								File debPackage = byteArrayToFile(config.getDebFile(), "ahenk.deb"); 
								
								SetupUtils.installPackage(ip, config.getUsername(),
										config.getPassword(), config.getPort(), config.getPrivateKeyFile(),
										config.getPassphrase(), debPackage);
								
								// TODO successfully installed logu düş
								
							} else {
								// TODO could not connect logu düş
							}
							
						} catch (SSHConnectionException e) {
							e.printStackTrace();
						} catch (CommandExecutionException e) {
							e.printStackTrace();
						}
					}

					
				};
				
				executor.execute(runnable);
			}
			
		}
		else {
			
		}
		

		ICommandResult commandResult = resultFactory.create(CommandResultStatus.OK, new ArrayList<String>(), this,
				new HashMap<String, Object>());

		logger.info("Command executed successfully.");

		return commandResult;
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
	private File byteArrayToFile(byte[]content, String filename) {
		
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
