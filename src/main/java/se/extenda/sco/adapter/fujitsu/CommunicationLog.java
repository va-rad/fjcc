package se.extenda.sco.adapter.fujitsu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.log4j.Logger;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Intercepts HTTP communication so it is possible to access both request and
 * response and log it properly.
 */
public class CommunicationLog implements ClientHttpRequestInterceptor {

	/**
	 * Regular trace log
	 */
	private static final Logger logger = Logger.getLogger(CommunicationLog.class);

	/**
	 * Logger for communication with Fujitsu API into a separate file
	 */
	private static final Logger SCO_API_LOGGER = Logger.getLogger("SCO_API");

	/** Lock to ensure that logging from different threads is regulated */
	private static Lock logLock = new ReentrantLock();

	private final MessageId messageId;

	public CommunicationLog(MessageId messageId) {
		this.messageId = messageId;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		ClientHttpResponse response = execution.execute(request, body);
		logFujitsuCommunication(request, body, response);
		return response;
	}

	/**
	 * Used for logging all communication between adapter and the fujitsu-api
	 * into a separate file.
	 * 
	 * @param request
	 *            the PROTOCOL request
	 * @param body
	 *            the request body
	 * @param response
	 *            the PROTOCOL response
	 * 
	 */
	private void logFujitsuCommunication(HttpRequest request, byte[] body, ClientHttpResponse response) {
		logLock.lock();
		try {
			try {
				SCO_API_LOGGER.info("\n\n<Request " + "id: " + messageId.current() + ">" + "\n" + request.getMethod()
						+ " " + request.getURI() + "\nHeaders : " + request.getHeaders().toString() + "\nBody : "
						+ toPrettyJson(new String(body, Charset.forName("UTF-8"))) + "\n</Request>\n");

				String prettyPrintedJson = toPrettyJson(getResponseBody(response));

				SCO_API_LOGGER.info("\n\n<Response " + "id: " + messageId.current() + ">" + "\n" + "OperationStatus : "
						+ response.getStatusCode().toString() + "\nHeaders : " + response.getHeaders() + "\nBody : "
						+ prettyPrintedJson + "\n</Response>\n");
			} catch (IOException | ScriptException e) {
				logger.error("Failed to log request/response communication", e);
			}
		} finally {
			logLock.unlock();
		}
	}

	/**
	 * Extracts the body contents from the response body.
	 * 
	 * @param response
	 *            the response to extract the content from
	 * @return the body content
	 * @throws IOException
	 */
	private static String getResponseBody(ClientHttpResponse response) throws IOException {
		return new BufferedReader(new InputStreamReader(response.getBody())).readLine();
	}

	/**
	 * Takes an unformatted JSON string and formats it.
	 * 
	 * @param unformattedJson
	 *            the JSON string to format
	 * @return the formatted JSON string
	 * @throws ScriptException
	 */
	private static String toPrettyJson(String unformattedJson) throws ScriptException {
		if (unformattedJson != null && !unformattedJson.isEmpty()) {
			ScriptEngineManager manager = new ScriptEngineManager();
			ScriptEngine scriptEngine = manager.getEngineByName("JavaScript");
			scriptEngine.put("jsonString", unformattedJson);
			scriptEngine.eval("result = JSON.stringify(JSON.parse(jsonString), null, 2)");
			return (String) scriptEngine.get("result");
		} else {
			return "";
		}
	}

}
