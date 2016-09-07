package tr.org.liderahenk.network.inventory.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import tr.org.liderahenk.network.inventory.dto.ScanResultDto;
import tr.org.liderahenk.network.inventory.dto.ScanResultHostDto;
import tr.org.liderahenk.network.inventory.entities.ScanResult;
import tr.org.liderahenk.network.inventory.entities.ScanResultHost;
import tr.org.liderahenk.network.inventory.plugininfo.PluginInfoImpl;

public class GetScanResultCommand implements ICommand {

	private Logger logger = LoggerFactory.getLogger(GetScanResultCommand.class);

	private ICommandResultFactory resultFactory;
	private IPluginDbService pluginDbService;
	private PluginInfoImpl pluginInfo;

	@Override
	public ICommandResult execute(ICommandContext context) throws Exception {
		logger.info("Executing command: GET-SCAN-RESULT");

		Map<String, Object> parameterMap = context.getRequest().getParameterMap();

		logger.info("Getting result ID from parameter map");
		Long resultId = new Long(parameterMap.get("resultId").toString());

		// Select scan results
		logger.info("Selecting scan results with result ID: {}", new Object[] { resultId });
		List<ScanResult> scanResultList = pluginDbService.findByProperty(ScanResult.class, "resultId", resultId, 1);
		
		logger.info("Scan result list size: {}", new Object[] { scanResultList.size()});
		ScanResult scanResult = scanResultList.get(0);

		// Select scan result hosts
		List<ScanResultHost> scanResultHostList = pluginDbService.findByProperty(ScanResultHost.class, "scanResult", scanResult, null);
		
		ScanResultDto scanResultDto = createResultDto(scanResult, scanResultHostList);

		logger.info("Creating result map");
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

		logger.info("Command executed successfully: GET-SCAN-RESULT");

		return resultFactory.create(CommandResultStatus.OK, new ArrayList<String>(), this, resultMap);
	}

	private ScanResultDto createResultDto(ScanResult scanResult, List<ScanResultHost> scanResultHostList) {

		List<ScanResultHostDto> scanResultHostDtoList = new ArrayList<ScanResultHostDto>();

		logger.info("scanResultHostList size: {}", new Object[] {scanResultHostDtoList.size()});

		for (ScanResultHost scanresultHost : scanResultHostList) {
			ScanResultHostDto scanresultHostDto = new ScanResultHostDto(scanresultHost.getHostname(),
					scanresultHost.getIp(), scanresultHost.isHostUp(), scanresultHost.getOpenPorts(),
					scanresultHost.getOsGuess(), scanresultHost.getDistance(), scanresultHost.getUptime(),
					scanresultHost.getMac(), scanresultHost.getVendor());
			scanResultHostDtoList.add(scanresultHostDto);
		}

		ScanResultDto scanResultDto = new ScanResultDto(scanResult.getIpRange(), scanResult.getTimingTemplate(),
				scanResult.getPorts(), scanResult.getSudoUsername(), scanResult.getSudoPassword(),
				scanResult.getScanDate(), scanResultHostDtoList);
		
		return scanResultDto;
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
		return "GET-SCAN-RESULT";
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

}
