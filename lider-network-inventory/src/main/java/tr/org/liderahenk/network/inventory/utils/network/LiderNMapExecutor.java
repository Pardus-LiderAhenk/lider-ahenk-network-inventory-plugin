package tr.org.liderahenk.network.inventory.utils.network;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.nmap4j.core.flags.ArgumentProperties;
import org.nmap4j.core.nmap.ExecutionResults;
import org.nmap4j.core.nmap.NMapExecutionException;
import org.nmap4j.core.nmap.NMapExecutor;
import org.nmap4j.core.nmap.NMapInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.network.inventory.utils.StringUtils;

public class LiderNMapExecutor extends NMapExecutor {

	private static final Logger logger = LoggerFactory.getLogger(LiderNMapExecutor.class);

	private ArgumentProperties nmapArguments;
	private LiderNMapProperties nmapProperties;

	public LiderNMapExecutor(ArgumentProperties argProps, LiderNMapProperties nmapProps)
			throws NMapInitializationException {
		super(argProps, nmapProps);
		nmapArguments = argProps;
		nmapProperties = nmapProps;
		if (nmapArguments == null || nmapProperties == null) {
			throw new NMapInitializationException(
					"You cannot instantiate " + "an NMapExecutor with nulls in either argument. Please "
							+ "refer to the documentation if you aren't sure how to proceed.");
		}
		if (nmapProps.getPath() == null || (nmapProps.getPath() != null && nmapProps.getPath().length() <= 0)) {
			throw new NMapInitializationException(
					"the NMAP_HOME variable is not set " + "or you did not set this path.");
		}
	}

	/**
	 * Get the nmap command as a StringBuffer.
	 * 
	 * @return
	 */
	private StringBuffer getCommand() {

		StringBuffer fullCommand = new StringBuffer();
		fullCommand.append(nmapProperties.getFullyFormattedCommand());
		fullCommand.append(" ");
		fullCommand.append(nmapArguments.getFlags());

		return fullCommand;
	}

	/**
	 * This method attempts to execute NMap using the properties supplied when
	 * this object was constructed.
	 * <p>
	 * This method can throw an NMapExecutionException which will be a wrapper
	 * around an IO Exception.
	 * 
	 * @return
	 * @throws NMapExecutionException
	 */
	@Override
	public ExecutionResults execute() throws NMapExecutionException {
		StringBuffer command = getCommand();
		ExecutionResults results = new ExecutionResults();

		try {
			logger.info("Command: {}", command.toString());

			results.setExecutedCommand(command.toString());
			Process process = Runtime.getRuntime().exec(command.toString());

			// pass password as an argument
			if (nmapProperties.getSudoUser() != null) {
				OutputStream stdIn = process.getOutputStream();
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdIn));
				writer.write(nmapProperties.getSudoUserPassword());
				writer.write("\n"); // write newline char to mimic 'enter'
									// press.
				writer.flush();
			}

			results.setErrors(StringUtils.convertStream(process.getErrorStream()));
			results.setOutput(StringUtils.convertStream(process.getInputStream()));

		} catch (IOException e) {
			throw new NMapExecutionException(e.getMessage(), e);
		}

		return results;
	}

	@Override
	public String toString() {
		return getCommand().toString();
	}

}
