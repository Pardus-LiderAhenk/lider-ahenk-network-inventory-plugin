package tr.org.liderahenk.network.inventory.utils.network;

import org.nmap4j.Nmap4j;
import org.nmap4j.core.flags.ArgumentProperties;
import org.nmap4j.core.nmap.ExecutionResults;
import org.nmap4j.core.nmap.NMapExecutionException;
import org.nmap4j.core.nmap.NMapExecutor;
import org.nmap4j.core.nmap.NMapInitializationException;
import org.nmap4j.data.NMapRun;
import org.nmap4j.parser.OnePassParser;

public class LiderNmap4j extends Nmap4j {

	private LiderNMapProperties nmapProperties;
	private ArgumentProperties flags;
	private NMapExecutor nmapExecutor;
	private ExecutionResults results;
	
	public LiderNmap4j(String path) {
		super(path);
		nmapProperties = new LiderNMapProperties(path) ;
		flags = new ArgumentProperties();
	}

	/**
	 * Executes the nmap scan with the parameters set.  You should have 
	 * called addFlags() with appropriate Nmap flags prior to executing the
	 * scan.
	 * 
	 * @throws NMapInitializationException
	 * @throws NMapExecutionException
	 */
	@Override
	public void execute() throws NMapInitializationException,
			NMapExecutionException {
		nmapExecutor = new LiderNMapExecutor(flags, nmapProperties );
		results = nmapExecutor.execute();
	}
	
	/**
	 * Add the appropriate flags to your scan.  Call this method with all the 
	 * flags you will want.  For example, if you want to scan for hosts, OS
	 * information and service information you would pass "-sV -O -T4". This 
	 * method will append "-oX -" if you did not supply it.
	 * 
	 * @param flagSet
	 */
	@Override
	public void addFlags( String flagSet ) {
		StringBuilder sb = new StringBuilder() ;
		sb.append( flagSet ) ;
		if( !flagSet.contains( "-oX" ) ) {
			sb.append( " -oX -" ) ;
		}
		flags.addFlag( sb.toString() ) ;
	}
	
	/**
	 * Add a list of space delimited hosts that you want to scan.  This 
	 * list conforms to the requirements that Nmap sets forth.
	 * 
	 * @param hosts
	 */
	@Override
	public void includeHosts( String hosts )  {
		flags.addIncludedHost( hosts ) ;
	}
		
	/**
	 * Add a list of space delimited hosts to exclude.  Usually this is used
	 * when you specify a large included host list.  This allows you specify
	 * broad ranges host addresses and exclude some hosts within that range.
	 * 
	 * @param hosts
	 */
	@Override
	public void excludeHosts( String hosts ) {
		flags.addExcludedHost( hosts ) ;
	}
	
	/**
	 * Returns the raw output of the execution.
	 * @return
	 */
	@Override
	public String getOutput() {
		return results.getOutput() ;
	}
	
	/**
	 * This method returns an object tree representing the XML nodes.
	 *  
	 * @return
	 */
	@Override
	public synchronized NMapRun getResult() {
		OnePassParser parser = new OnePassParser() ;
		NMapRun nmapRun = parser.parse(results.getOutput(), OnePassParser.STRING_INPUT ) ;
		return nmapRun;
	}
	
	/**
	 * Checks the output for the word "ERROR" as Nmap will usually produce an
	 * error message that starts with ERROR though there are other scenarios. If
	 * the call to getResult() fails check the error output.
	 * @return
	 */
	@Override
	public boolean hasError() {
		return results.getErrors().contains( "ERROR") ;
	}
	
	/**
	 * Use this method to get the raw results of the execution.  The 
	 * ExecutionResults contains the raw output, the errors and the
	 * command that was executed.
	 * @return
	 */
	@Override
	public ExecutionResults getExecutionResults() {
		return results ;
	}

	public void useSudo(String sudoUser, String sudoUserPassword) {
		nmapProperties.setSudoUser(sudoUser);
		nmapProperties.setSudoUserPassword(sudoUserPassword);		
	}
	
}
