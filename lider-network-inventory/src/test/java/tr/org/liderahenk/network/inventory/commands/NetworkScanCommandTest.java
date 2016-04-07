package tr.org.liderahenk.network.inventory.commands;

import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

import junit.framework.TestCase;
//import tr.org.liderahenk.lider.core.api.rest.requests.ITaskCommandRequest;
import tr.org.liderahenk.lider.core.api.service.ICommandContext;
import tr.org.liderahenk.lider.core.api.service.ICommandResult;
import tr.org.liderahenk.lider.core.api.service.ICommandResultFactory;

@RunWith(JUnit4.class)
public class NetworkScanCommandTest extends TestCase {

	@Test
	public void execute() {

//		// Populate request object
//		Map<String, Object> parameterMap = new HashMap<String, Object>();
//		parameterMap.put("ipRange", "192.168.1.80-120");
//		parameterMap.put("timingTemplate", "3");
//		ITaskCommandRequest request = Mockito.mock(ITaskCommandRequest.class);
//		when(request.getParameterMap()).thenReturn(parameterMap);
//
//		// Populate context object
//		ICommandContext context = Mockito.mock(ICommandContext.class);
//		when(context.getRequest()).thenReturn(request);
//
//		// Populate command object
//		NetworkScanCommand command = new NetworkScanCommand();
//		ICommandResultFactory resultFactory = Mockito.mock(ICommandResultFactory.class);
//		command.setResultFactory(resultFactory);
//
//		ICommandResult result = command.execute(context);
//
//		assertNotNull(result);
	}

}
