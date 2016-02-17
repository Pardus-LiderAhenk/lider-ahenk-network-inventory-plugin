package tr.org.liderahenk.network.inventory.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class StringUtils {

	public static <T> String join(String separator, List<?> objects) {
		if (objects != null) {
			StringBuilder sb = new StringBuilder();
			String sep = "";
			for (Object o : objects) {
				sb.append(sep).append(o.toString());
				sep = separator;
			}
			return sb.toString();
		}
		return null;
	}

	/**
	 * Converts the given InputStream to a String. This is how the streams from
	 * executing NMap are converted and later stored in the ExecutionResults.
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static String convertStream(InputStream is) throws IOException {
		String output;
		StringBuffer outputBuffer = new StringBuffer();
		BufferedReader streamReader = new BufferedReader(new InputStreamReader(is));
		while ((output = streamReader.readLine()) != null) {
			outputBuffer.append(output);
			outputBuffer.append("\n");
		}
		return outputBuffer.toString();
	}
	
}
