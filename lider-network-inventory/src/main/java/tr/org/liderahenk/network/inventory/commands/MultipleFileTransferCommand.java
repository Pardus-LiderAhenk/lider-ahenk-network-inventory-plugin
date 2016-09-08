package tr.org.liderahenk.network.inventory.commands;

import java.util.ArrayList;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.lider.core.api.configuration.IConfigurationService;
import tr.org.liderahenk.lider.core.api.plugin.ICommand;
import tr.org.liderahenk.lider.core.api.service.ICommandContext;
import tr.org.liderahenk.lider.core.api.service.ICommandResult;
import tr.org.liderahenk.lider.core.api.service.ICommandResultFactory;
import tr.org.liderahenk.lider.core.api.service.enums.CommandResultStatus;
import tr.org.liderahenk.lider.core.api.utils.FileCopyUtils;
import tr.org.liderahenk.network.inventory.plugininfo.PluginInfoImpl;

public class MultipleFileTransferCommand implements ICommand {

	private Logger logger = LoggerFactory.getLogger(MultipleFileTransferCommand.class);

	private ICommandResultFactory resultFactory;
	private PluginInfoImpl pluginInfo;
	private IConfigurationService configurationService;

	@Override
	public ICommandResult execute(ICommandContext context) throws Exception {
		logger.info("Executing command: MULTIPLE-FILE-TRANSFER");

		Map<String, Object> parameterMap = context.getRequest().getParameterMap();

		// Get parameters
		logger.info("Getting parameters from parameter map");
		byte[] fileAsByteArr = DatatypeConverter.parseBase64Binary((String) parameterMap.get("encodedFile"));
		
		// Send file to file server
		logger.info("Sending file to file server");
		String absPathOfRemoteFile = new FileCopyUtils().sendFile(
				configurationService.getFileServerHost(), 
				configurationService.getFileServerPort(),
				configurationService.getFileServerUsername(), 
				configurationService.getFileServerPassword(),
				fileAsByteArr, 
				configurationService.getFileServerAgentFilePath().replace("{0}", "lider"));
		
		logger.info("Putting remote path and removing encoded file at parameter map");
		parameterMap.put("remotePath", absPathOfRemoteFile);
		parameterMap.remove("encodedFile");
		
		logger.info("MULTIPLE-FILE-TRANSFER executed successfully, creating result");
		return resultFactory.create(CommandResultStatus.OK, new ArrayList<String>(), this);
	}

	@Override
	public ICommandResult validate(ICommandContext context) {
		return resultFactory.create(CommandResultStatus.OK, null, this);
	}

	@Override
	public String getPluginName() {
		return pluginInfo.getPluginName();
	}

	@Override
	public String getPluginVersion() {
		return pluginInfo.getPluginVersion();
	}

	@Override
	public String getCommandId() {
		return "MULTIPLE-FILE-TRANSFER";
	}

	@Override
	public Boolean executeOnAgent() {
		return true;
	}

	public void setResultFactory(ICommandResultFactory resultFactory) {
		this.resultFactory = resultFactory;
	}

	public void setPluginInfo(PluginInfoImpl pluginInfo) {
		this.pluginInfo = pluginInfo;
	}

	public void setConfigurationService(IConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

}
