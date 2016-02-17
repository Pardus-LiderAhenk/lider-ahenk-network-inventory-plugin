package tr.org.liderahenk.network.inventory.exception;

/**
 * CommandExecutionException is fired when a command execution (locally or
 * remotely via SSH) fails. It contains error output of the executed command.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class CommandExecutionException extends Exception {

	private static final long serialVersionUID = -5978195767659202789L;

	public CommandExecutionException(String message) {
		super(message);
	}

	public CommandExecutionException(String message, Throwable throwable) {
		super(message, throwable);
	}

}
