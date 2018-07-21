package se.extenda.sco.adapter.fujitsu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import se.extenda.sco.adapter.fujitsu.response.ErrorResponse;
import se.extenda.sco.adapter.fujitsu.response.ErrorResponseCode;
import se.extenda.sco.adapter.fujitsu.response.GeneralFujitsuResponse;

/**
 * REST client utility methods.
 *
 */
public class RestUtil {
	
	private RestUtil() { 
		// left empty
	}

	/**
	 * Create REST template
	 * @param config Fujitsu configuration
	 * @param messageId message identifier generator
	 * @return template
	 */
	public static RestTemplate createRestTemplate(FujitsuConfiguration config, MessageId messageId) {
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setReadTimeout(config.getReadTimeoutInMillis());
		requestFactory.setConnectTimeout(config.getConnectTimeoutInMillis());
		BufferingClientHttpRequestFactory bufferingRequestFactory = new BufferingClientHttpRequestFactory(
				requestFactory);
		RestTemplate template = new RestTemplate(bufferingRequestFactory);

		// add the interceptor that will handle logging
		List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
		interceptors.add(new CommunicationLog(messageId));
		template.setInterceptors(interceptors);
		template.setErrorHandler( new DefaultResponseErrorHandler() );
		return template;
	}

	/**
	 * Return a base URI using host name and port.
	 * @param hostname host name
	 * @param port port
	 * @return URI builder with filled in Base URI
	 */
	public static UriComponentsBuilder getBaseUri(String hostname, int port) {
		return UriComponentsBuilder.newInstance()
				.scheme("http")
				.host(hostname)
				.port(port);
	}
	
	public static void handlePossibleError(GeneralFujitsuResponse response) throws IOException {
		List<ErrorResponse> errorResponses = Arrays.asList(response.getErrorResponses());
		if (response.getErrorResponses() != null && !errorResponses.isEmpty()
				&& Arrays.stream(response.getErrorResponses())
						.noneMatch(it -> it.getErrorCode() == ErrorResponseCode.PLATE_IS_OVERLOADED.getStatus())) {
			throw new IOException("Error response starting fujitsu selfcheckout: " + response.toString());
		}
	}

}
