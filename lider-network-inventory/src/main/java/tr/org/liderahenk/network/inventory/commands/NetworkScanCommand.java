package tr.org.liderahenk.network.inventory.commands;

import java.util.ArrayList;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.nmap4j.data.nmaprun.Host;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.lider.core.api.log.IOperationLogService;
import tr.org.liderahenk.lider.core.api.plugin.CommandResultStatus;
import tr.org.liderahenk.lider.core.api.plugin.ICommandContext;
import tr.org.liderahenk.lider.core.api.plugin.ICommandResult;
import tr.org.liderahenk.lider.core.api.plugin.ICommandResultFactory;
import tr.org.liderahenk.lider.core.api.plugin.IPluginDbService;
import tr.org.liderahenk.network.inventory.dto.NmapParametersDto;
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

	@Override
	public ICommandResult execute(ICommandContext context) {

		logger.debug("Executing command.");

		ArrayList<Host> hosts = null;

		Map<String, Object> parameterMap = context.getRequest().getParameterMap();
		Boolean readLast = (Boolean) parameterMap.get("readLast");
		// Find last network scan!
		if (readLast != null && readLast.booleanValue()) {
			// TODO
		}
		// New network scan.
		else {

			// Nmap parameters
			ObjectMapper mapper = new ObjectMapper();
			NmapParametersDto nmapParams = null;
			try {
				nmapParams = mapper.readValue(mapper.writeValueAsBytes(parameterMap.get("nmapParams")),
						NmapParametersDto.class);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				ArrayList<String> messages = new ArrayList<String>();
				messages.add("Couldn't find or parse nmap parameters. Please see error log for more details.");
				return resultFactory.create(CommandResultStatus.ERROR, messages, this);
			}
			logger.debug("Nmap parameters: {}", nmapParams);

			// TODO nmapParams, ip listesini thread'lere böl!
			try {
				hosts = NetworkUtils.scanNetwork(nmapParams.getIpRange(), nmapParams.getPorts(),
						nmapParams.getSudoUsername(), nmapParams.getSudoPassword(), nmapParams.getTimingTemplate());
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				ArrayList<String> messages = new ArrayList<String>();
				messages.add("Couldn't scan network. Please see error log for more details.");
				return resultFactory.create(CommandResultStatus.ERROR, messages, this);
			}

			logger.debug("Scan finished, found hosts: {}", hosts);

			// TODO Save scan result to the database
		}

		logger.info("Command executed successfully.");

		return resultFactory.create(CommandResultStatus.OK, new ArrayList<String>(), this);
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
