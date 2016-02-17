package tr.org.liderahenk.network.inventory.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.lider.core.api.log.IOperationLogService;
import tr.org.liderahenk.lider.core.api.plugin.CommandResultStatus;
import tr.org.liderahenk.lider.core.api.plugin.ICommandContext;
import tr.org.liderahenk.lider.core.api.plugin.ICommandResult;
import tr.org.liderahenk.lider.core.api.plugin.ICommandResultFactory;
import tr.org.liderahenk.lider.core.api.plugin.IPluginDbService;
import tr.org.liderahenk.lider.core.api.rest.IRestRequest;
import tr.org.liderahenk.network.inventory.utils.network.NetworkUtils;

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

	@Override
	public ICommandResult execute(ICommandContext context) {

		logger.debug("Executing command.");

		Map<String, Object> parameterMap = context.getRequest().getParameterMap();
		String file = (String) parameterMap.get("file");
		String filename = (String) parameterMap.get("filename");
		ArrayList<String> ipList = (ArrayList<String>) parameterMap.get("iplist");

		logger.debug("Parameter map: {}", parameterMap);

		// TODO

		ICommandResult commandResult = resultFactory.create(CommandResultStatus.OK, new ArrayList<String>(), this,
				new HashMap<String, Object>());

		logger.info("Command executed successfully.");

		return commandResult;
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
