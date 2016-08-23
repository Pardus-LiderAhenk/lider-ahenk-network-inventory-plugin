package tr.org.liderahenk.network.inventory.contants;

// TODO read these values from a config file! - emre
public class Constants {
	
	public static final class SSH_CONFIG {
		public static final int CONNECTION_PORT = 22;
		public static final int SESSION_TIMEOUT = 99000000;
		public static final int CHANNEL_TIMEOUT = 99000000;
		public static final int NUM_THREADS = 10;
	}
	
	public static final class NMAP_CONFIG {
		public static final int OS_ACCURACY_THRESHOLD = 90;
		public static final int OS_LIMIT = 3;
		public static final int NETWORK_TIMEOUT = 900000;
		public static final String NMAP_PATH = "/usr";
	}
	
	public static enum AccessMethod {
		USERNAME_PASSWORD,
		PRIVATE_KEY
	}
	
	public static enum InstallMethod {
		APT_GET,
		PROVIDED_DEB,
		WGET
	}

	public static enum PackageInstaller {
		DPKG,
		GDEBI,
	}
}
