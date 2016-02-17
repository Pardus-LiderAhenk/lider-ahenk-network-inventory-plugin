package tr.org.liderahenk.network.inventory.utils.setup;

import org.slf4j.LoggerFactory;
import org.vngx.jsch.util.Logger;

public class DefaultSSHLogger implements Logger {
	
	private org.slf4j.Logger logger = LoggerFactory.getLogger(DefaultSSHLogger.class);

	@Override
	public boolean isEnabled(Level level) {
		return logger.isDebugEnabled() || level != Level.DEBUG;
	}

	@Override
	public void log(Level level, String message) {
		if (level == Level.DEBUG) {
			logger.debug(message);
		} else if (level == Level.INFO) {
			logger.info(message);
		} else if (level == Level.WARN) {
			logger.warn(message);
		} else if (level == Level.ERROR) {
			logger.error(message);
		}
		// TODO
	}

	@Override
	public void log(Level level, String message, Object... args) {
		
	}

	@Override
	public void log(Level level, String message, Throwable exception) {
		
	}

}
