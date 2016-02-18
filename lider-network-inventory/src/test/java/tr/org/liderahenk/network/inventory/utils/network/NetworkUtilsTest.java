package tr.org.liderahenk.network.inventory.utils.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.nmap4j.data.nmaprun.Host;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;
import tr.org.liderahenk.network.inventory.dto.NmapParametersDto;
import tr.org.liderahenk.network.inventory.utils.StringUtils;

@RunWith(JUnit4.class)
public class NetworkUtilsTest extends TestCase {
	
	private static final Logger logger = LoggerFactory.getLogger(NetworkUtilsTest.class);

	@Test
	public void ipShouldBeValid() {
		assertEquals(true, NetworkUtils.isIpValid("192.168.1.106"));
		assertEquals(true, NetworkUtils.isIpValid("localhost"));
	}
	
	@Test
	public void ipShouldBeReachable() {
		assertEquals(true, NetworkUtils.isIpReachable("192.168.1.106"));
		assertEquals(true, NetworkUtils.isIpReachable("192.168.1.40"));
	}
	
	@Test
	public void findAllIpAddresses() throws IOException, InterruptedException {
		List<String> ipAddresses = NetworkUtils.findIpAddresses();
		assertNotNull(ipAddresses);
		logger.info(StringUtils.join(",", ipAddresses));
	}
	
	@Test
	public void networkShouldBeScanned() throws IOException, InterruptedException {
		NmapParametersDto params = new NmapParametersDto();
		ArrayList<String> ipList = new ArrayList<String>();
		ipList.add("192.168.1.40");
		ipList.add("192.168.1.41");
		ipList.add("192.168.1.42");
		params.setIpList(ipList);
		params.setTimingTemplate("3");
		ArrayList<Host> hosts = NetworkUtils.scanNetwork(params);
		assertNotNull(hosts);
		logger.info(StringUtils.join(",", hosts));
	}

}
