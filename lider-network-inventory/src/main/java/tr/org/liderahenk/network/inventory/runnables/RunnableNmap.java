package tr.org.liderahenk.network.inventory.runnables;

import java.util.ArrayList;

import org.nmap4j.data.nmaprun.Host;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.network.inventory.dto.ScanResultDto;
import tr.org.liderahenk.network.inventory.dto.ScanResultHostDto;
import tr.org.liderahenk.network.inventory.utils.network.NetworkUtils;

/**
 * A runnable that is responsible of performing a network scan over given IP
 * range.
 * 
 * @author <a href="mailto:caner.feyzullahoglu@agem.com.tr">Caner
 *         Feyzullahoğlu</a>
 */
public class RunnableNmap implements Runnable {

	private Logger logger = LoggerFactory.getLogger(RunnableNmap.class);

	private ScanResultDto scanResultDto;
	private String ipRange;
	private String ports;
	private String sudoUsername;
	private String sudoPassword;
	private String timingTemplate;
	private ArrayList<String> messages;

	public RunnableNmap(ScanResultDto scanResultDto, String ipRange, String ports, String sudoUsername,
			String sudoPassword, String timingTemplate, ArrayList<String> messages) {
		this.scanResultDto = scanResultDto;
		this.ipRange = ipRange;
		this.ports = ports;
		this.sudoUsername = sudoUsername;
		this.sudoPassword = sudoPassword;
		this.timingTemplate = timingTemplate;
		this.messages = messages;
	}

	@Override
	public void run() {
		try {
			ArrayList<Host> hosts = NetworkUtils.scanNetwork(ipRange, ports, sudoUsername, sudoPassword,
					timingTemplate);
			if (hosts != null && hosts.size() > 0) {
				for (Host host : hosts) {
					String ip = NetworkUtils.getIpV4(host);
					if (ip != null && !ip.isEmpty()) {

						ScanResultHostDto hostDto = new ScanResultHostDto();
						hostDto.setHostname(NetworkUtils.getHostname(host));
						hostDto.setDistance(NetworkUtils.getDistance(host));
						hostDto.setIp(NetworkUtils.getIpV4(host));
						hostDto.setHostUp(NetworkUtils.isHostUp(host));
						hostDto.setOpenPorts(NetworkUtils.getOpenPorts(host));
						hostDto.setOsGuess(NetworkUtils.getOsGuess(host));
						hostDto.setUptime(NetworkUtils.getUptime(host));
						hostDto.setMac(NetworkUtils.getMac(host));
						hostDto.setVendor(NetworkUtils.getMacVendor(host));

						scanResultDto.getHosts().add(hostDto);
					}
				}
			}
		} catch (Exception e) {
			messages.add(e.getMessage());
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public String toString() {
		return "RunnableNmap [ipRange=" + ipRange + ", ports=" + ports + ", sudoUsername=" + sudoUsername
				+ ", sudoPassword=" + sudoPassword + ", timingTemplate=" + timingTemplate + ", messages=" + messages
				+ "]";
	}

}
