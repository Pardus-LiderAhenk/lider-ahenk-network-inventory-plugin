package tr.org.liderahenk.network.inventory.commands;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

import junit.framework.TestCase;
import tr.org.liderahenk.lider.core.api.plugin.ICommandContext;
import tr.org.liderahenk.lider.core.api.plugin.ICommandResult;
import tr.org.liderahenk.lider.core.api.plugin.ICommandResultFactory;
import tr.org.liderahenk.lider.core.api.rest.IRestRequest;
import tr.org.liderahenk.network.inventory.dto.NmapParametersDto;

@RunWith(JUnit4.class)
public class NetworkScanCommandTest extends TestCase {

	@Test
	public void execute() {
		
		// Populate nmap parameters object
		NmapParametersDto nmapParams = Mockito.mock(NmapParametersDto.class);
		when(nmapParams.getIpRange()).thenReturn("192.168.1.80-120");
		when(nmapParams.getTimingTemplate()).thenReturn("3");
		
		// Populate request object
		IRestRequest request = Mockito.mock(IRestRequest.class);
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put("nmapParams", nmapParams);
		when(request.getParameterMap()).thenReturn(parameterMap);
		
		// Populate context object
		ICommandContext context = Mockito.mock(ICommandContext.class);
		when(context.getRequest()).thenReturn(request);
		
		// Populate command object
		NetworkScanCommand command = new NetworkScanCommand();
		ICommandResultFactory resultFactory = Mockito.mock(ICommandResultFactory.class);
		command.setResultFactory(resultFactory);
		
		ICommandResult result = command.execute(context);
		
		assertNotNull(result);
	}

}
