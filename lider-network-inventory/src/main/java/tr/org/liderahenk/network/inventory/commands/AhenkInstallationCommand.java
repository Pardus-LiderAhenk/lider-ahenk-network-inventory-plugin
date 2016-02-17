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
