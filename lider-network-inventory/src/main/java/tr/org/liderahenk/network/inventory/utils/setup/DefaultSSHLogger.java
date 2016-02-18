package tr.org.liderahenk.network.inventory.utils.setup;

import org.slf4j.LoggerFactory;
import org.vngx.jsch.util.Logger;

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
		} else if (level == Level.ERROR || level == Level.FATAL) {
			logger.error(message);
		}
	}

	@Override
	public void log(Level level, String message, Object... args) {
		if (level == Level.DEBUG) {
			logger.debug(message, args);
		} else if (level == Level.INFO) {
			logger.info(message, args);
		} else if (level == Level.WARN) {
			logger.warn(message, args);
		} else if (level == Level.ERROR || level == Level.FATAL) {
			logger.error(message, args);
		}
	}

	@Override
	public void log(Level level, String message, Throwable exception) {
		if (level == Level.DEBUG) {
			logger.debug(message, exception);
		} else if (level == Level.INFO) {
			logger.info(message, exception);
		} else if (level == Level.WARN) {
			logger.warn(message, exception);
		} else if (level == Level.ERROR || level == Level.FATAL) {
			logger.error(message, exception);
		}
	}

}
