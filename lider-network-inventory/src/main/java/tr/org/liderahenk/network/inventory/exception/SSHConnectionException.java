package tr.org.liderahenk.network.inventory.exception;

/**
 * SSHConnectionException is fired when SSHManager fails to connect to a remote
 * address. It is possibly due to wrong username-password pair or undefined SSH
 * key.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class SSHConnectionException extends Exception {

	private static final long serialVersionUID = 4717877839237702456L;

	public SSHConnectionException(String message) {
		super(message);
	}

	public SSHConnectionException(String message, Throwable throwable) {
		super(message, throwable);
	}

}
