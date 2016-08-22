package tr.org.liderahenk.network.inventory.utils.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

import org.nmap4j.core.nmap.NMapExecutionException;
import org.nmap4j.core.nmap.NMapInitializationException;
import org.nmap4j.data.NMapRun;
import org.nmap4j.data.host.Address;
import org.nmap4j.data.host.os.OsMatch;
import org.nmap4j.data.host.ports.Port;
import org.nmap4j.data.nmaprun.Host;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.network.inventory.contants.Constants;

/**
 * Utility class which provides common network methods (such as finding IP
 * addresses, scanning network etc.)
 *
 */
public class NetworkUtils {

	private static final Logger logger = LoggerFactory.getLogger(NetworkUtils.class);

	public static final String IPV4 = "ipv4";
	public static final String HOST_UP = "up";
	public static final String DISTANCE_UNIT = "hop";
	public static final String MAC = "mac";
	public static final String PORT_OPEN = "open";

	/**
	 * IP address pattern
	 */
	private static final Pattern IP_PATTERN = Pattern
			.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

	/**
	 * 
	 * @return all IP addresses of the network to which the machine belongs
	 * @throws UnknownHostException
	 * @throws SocketException
	 */
	public static List<String> findIpAddresses() throws UnknownHostException, SocketException {

		List<String> ipAddresses = new ArrayList<String>();

		// Find all IP ranges of network interfaces belonging to the local
		// machine.
		// (It may have multiple network interfaces, therefore might have
		// multiple IP addresses)
		Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
		while (netInterfaces.hasMoreElements()) {
			// http://bugs.java.com/view_bug.do?bug_id=6707289
			NetworkInterface iface = netInterfaces.nextElement();
			if (!iface.getName().contains("lo")) { // Consider only eth
													// networks, skip localhost
				for (InterfaceAddress ifaceAddress : iface.getInterfaceAddresses()) {
					if (ifaceAddress.getNetworkPrefixLength() <= (short) 32) { // Supports
																				// only
																				// IPv4
																				// at
																				// the
																				// moment,
																				// So
																				// max
																				// mask
																				// length
																				// cannot
																				// exceed
																				// 32!
						SubnetUtils subnet = new SubnetUtils(ifaceAddress.getAddress().getHostAddress() + "/"
								+ ifaceAddress.getNetworkPrefixLength());
						ipAddresses.addAll(Arrays.asList(subnet.getInfo().getAllAddresses()));

						logger.debug("iface {} has address {}/{}", new Object[] { iface.getName(),
								ifaceAddress.getAddress(), ifaceAddress.getNetworkPrefixLength() });
					}
				}
			}
		}

		logger.info("IP addresses have been found. Returning results.");

		return ipAddresses;
	}

	/**
	 * Finds all IP ranges of the network interfaces which the local machine has
	 * 
	 * @return array list of ip ranges in CIDR notation
	 * @throws UnknownHostException
	 * @throws SocketException
	 */
	public static List<String> findIpRanges() throws UnknownHostException, SocketException {

		ArrayList<String> ipRanges = new ArrayList<String>();

		// Find all IP ranges of network interfaces belonging to the local
		// machine.
		// (It may have multiple network interfaces, therefore might have
		// multiple IP addresses)
		Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
		while (netInterfaces.hasMoreElements()) {
			// http://bugs.java.com/view_bug.do?bug_id=6707289
			NetworkInterface iface = netInterfaces.nextElement();
			if (!iface.getName().contains("lo")) { // Consider only eth
													// networks, skip localhost
				for (InterfaceAddress ifaceAddress : iface.getInterfaceAddresses()) {
					if (ifaceAddress.getNetworkPrefixLength() <= (short) 32) { // Supports
																				// only
																				// IPv4
																				// at
																				// the
																				// moment,
																				// So
																				// max
																				// mask
																				// cannot
																				// exceed
																				// 32!
						SubnetUtils subnet = new SubnetUtils(ifaceAddress.getAddress().getHostAddress() + "/"
								+ ifaceAddress.getNetworkPrefixLength());
						String ipRange = subnet.getInfo().getLowAddress() + "/" + ifaceAddress.getNetworkPrefixLength();
						ipRanges.add(ipRange);

						logger.debug("iface {} has address {}/{}", new Object[] { iface.getName(),
								ifaceAddress.getAddress(), ifaceAddress.getNetworkPrefixLength() });
					}
				}
			}
		}

		logger.info("IP ranges have been found. Returning results.");

		return ipRanges;
	}

	public static ArrayList<Host> scanNetwork(String ipRange, String ports, String sudoUsername, String sudoPassword,
			String timingTemplate) throws IOException, InterruptedException, NMapInitializationException, NMapExecutionException {

		logger.debug("Scanning network with parameters IP range: {}, ports: {}, username: {}, timing template: {}",
				new Object[] { ipRange, ports, sudoUsername, timingTemplate });

		LiderNmap4j nmap = new LiderNmap4j(Constants.NMAP_CONFIG.NMAP_PATH);
		nmap.includeHosts(ipRange);

		// Build flags
		StringBuilder flags = new StringBuilder(" -v ");
		logger.debug("****Timing Template : " + timingTemplate);
		logger.debug("****IP Sub Range : " + ipRange);
		logger.debug("****Port Range : " + ports);
		if (ports != null && !ports.isEmpty()) {
			flags.append(" -p ").append(ports);
		} else {
			flags.append(" --top-ports 10 ");
		}
		if (sudoPassword != null && !sudoPassword.isEmpty()) {
			nmap.useSudo(sudoUsername == null ? "root" : sudoUsername, sudoPassword);
			flags.append(" -O --osscan-guess ");
		}
		if (timingTemplate != null && !timingTemplate.isEmpty()) {
			flags.append(" -T").append(timingTemplate).append(" ");
		} else {
			flags.append(" -T3 ");
		}
		nmap.addFlags(flags.toString());

		nmap.execute();

		if (!nmap.hasError()) {
			NMapRun nmapRun = nmap.getResult();
			logger.info("Finished scanning network. Returning results");
			return nmapRun != null ? nmapRun.getHosts() : null;
		} else {
			logger.warn(nmap.getExecutionResults().getErrors());
			return null;
		}
	}

	public static List<String> convertToIpList(String ipRange) {
		if (ipRange == null || ipRange.isEmpty()) {
			return null;
		}
		String[] nums = ipRange.split("-");
		List<String> ipList = new ArrayList<String>();
		
		if (nums.length == 1) {
			ipList.add(nums[0]);
		} else {
			String firstIp = nums[0];
			String lastIp = firstIp.substring(0, firstIp.lastIndexOf(".") + 1) + nums[1];
			String currentIp = firstIp;
			while (true) {
				ipList.add(currentIp);
				if (currentIp.equalsIgnoreCase(lastIp)) {
					break;
				}
				currentIp = getNextIPV4Address(currentIp);
			}
		}
		return ipList;
	}

	public static String getNextIPV4Address(String ip) {
		String[] nums = ip.split("\\.");
		int i = (Integer.parseInt(nums[0]) << 24 | Integer.parseInt(nums[2]) << 8 | Integer.parseInt(nums[1]) << 16
				| Integer.parseInt(nums[3])) + 1;

		// If you wish to skip over .255 addresses.
		if ((byte) i == -1)
			i++;

		return String.format("%d.%d.%d.%d", i >>> 24 & 0xFF, i >> 16 & 0xFF, i >> 8 & 0xFF, i >> 0 & 0xFF);
	}

	/**
	 * Converts a collection of ordered IP addresses to IP range string ( e.g.
	 * ipList = {"192.168.1.50", "192.168.1.51", "192.168.1.52"} returns ipRange
	 * = "192.168.1.50-52" )
	 * 
	 * @param ipList
	 * @return
	 */
	public static String convertToIpRange(List<String> ipList) {
		if (ipList == null || ipList.isEmpty()) {
			return null;
		}
		if (ipList.size() == 1) {
			return ipList.get(0);
		}
		String ipRange = ipList.get(0) + "-" + ipList.get(ipList.size() - 1).split("\\.")[3];
		return isIpRangeValid(ipRange) ? ipRange : null;
	}

	// TODO
	public static boolean isIpRangeValid(String ipRange) {
		return true;
	}

	public static boolean isLocal(String ip) {
		return "127.0.0.1".equals(ip) || "localhost".equals(ip);
	}

	/**
	 * 
	 * @param ip
	 * @return true if the given string is a valid IP address
	 */
	public static boolean isIpValid(String ip) {
		return "localhost".equals(ip) || IP_PATTERN.matcher(ip).matches();
	}

	/**
	 * Test whether that address is reachable. Best effort is made by the
	 * implementation to try to reach the host, but firewalls and server
	 * configuration may block requests resulting in a unreachable status while
	 * some specific ports may be accessible.
	 * 
	 * @param ip
	 * @return true if IP is reachable, false otherwise
	 */
	public static boolean isIpReachable(String ip) {
		try {
			return InetAddress.getByName(ip).isReachable(Constants.NMAP_CONFIG.NETWORK_TIMEOUT);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return false;
	}

	//
	// Helper methods for Host class
	//

	public static String getIpV4(Host host) {
		if (host != null && host.getAddresses() != null) {
			for (Address address : host.getAddresses()) {
				if (IPV4.equalsIgnoreCase(address.getAddrtype())) {
					return address.getAddr();
				}
			}
		}
		return null;
	}

	public static boolean isHostUp(Host host) {
		if (host != null && host.getStatus() != null) {
			return HOST_UP.equalsIgnoreCase(host.getStatus().getState());
		}
		return false;
	}

	public static String getHostname(Host host) {
		if (host != null && host.getHostnames() != null && host.getHostnames().getHostname() != null) {
			return host.getHostnames().getHostname().getName();
		}
		return null;
	}

	public static String getDistance(Host host) {
		if (host != null && host.getDistance() != null) {
			return host.getDistance().getValue() + DISTANCE_UNIT;
		}
		return null;
	}

	public static String getUptime(Host host) {
		if (host != null && host.getUptime() != null && host.getUptime().getLastboot() != null) {
			return host.getUptime().getLastboot();
		}
		return null;
	}

	public static String getMac(Host host) {
		if (host != null && host.getAddresses() != null) {
			for (Address address : host.getAddresses()) {
				if (MAC.equalsIgnoreCase(address.getAddrtype())) {
					return address.getAddr();
				}
			}
		}
		return null;
	}

	public static String getMacVendor(Host host) {
		if (host != null && host.getAddresses() != null) {
			for (Address address : host.getAddresses()) {
				if (MAC.equalsIgnoreCase(address.getAddrtype())) {
					return address.getVendor();
				}
			}
		}
		return null;
	}

	public static String getOpenPorts(Host host) {
		if (host != null && host.getPorts() != null && host.getPorts().getPorts() != null) {
			StringBuilder portsStr = new StringBuilder();
			for (Port port : host.getPorts().getPorts()) {
				if (PORT_OPEN.equalsIgnoreCase(port.getState().getState())) {
					portsStr.append(port.getPortId()).append("/").append(port.getProtocol()).append(" ")
							.append(port.getService().getName()).append("\n");
				}
			}
			return portsStr.toString();
		}
		return null;
	}

	public static String getOsGuess(Host host) {
		if (host != null && host.getOs() != null && host.getOs().getOsMatches() != null) {
			StringBuilder osStr = new StringBuilder();
			for (int i = 0, osCount = 0; i < host.getOs().getOsMatches().size()
					&& osCount < Constants.NMAP_CONFIG.OS_LIMIT; i++) {
				OsMatch os = host.getOs().getOsMatches().get(i);
				if (os.getAccuracy() != null
						&& Integer.parseInt(os.getAccuracy()) > Constants.NMAP_CONFIG.OS_ACCURACY_THRESHOLD) {
					osStr.append("(%").append(os.getAccuracy()).append(") ").append(os.getName()).append("\n");
					osCount++;
				}
			}
			return osStr.toString();
		}
		return null;
	}

	//
	// Helper methods end
	//

}
