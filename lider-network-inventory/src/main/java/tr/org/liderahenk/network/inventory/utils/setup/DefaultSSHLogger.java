package tr.org.liderahenk.network.inventory.utils.setup;

import java.util.Hashtable;

import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Logger;

/**
 * DefaultSSHLogger works as a bridge between a logging framework and SSH
 * logger, it is used to wrap external logging framework such as slf4j to allow
 * for SSH logging integration.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class DefaultSSHLogger implements Logger {

	private org.slf4j.Logger logger = LoggerFactory.getLogger(DefaultSSHLogger.class);

	public static Hashtable<Integer, String> name = null;

	static {
		name = new Hashtable<Integer, String>();
		name.put(new Integer(DEBUG), "DEBUG: ");
		name.put(new Integer(INFO), "INFO: ");
		name.put(new Integer(WARN), "WARN: ");
		name.put(new Integer(ERROR), "ERROR: ");
		name.put(new Integer(FATAL), "FATAL: ");
	}

	@Override
	public boolean isEnabled(int level) {
		return logger.isDebugEnabled() || level != DEBUG;
	}

	@Override
	public void log(int level, String message) {
		if (level == DEBUG) {
			logger.debug(message);
		} else if (level == INFO) {
			logger.info(message);
		} else if (level == WARN) {
			logger.warn(message);
		} else if (level == ERROR || level == FATAL) {
			logger.error(message);
		}
	}

}
