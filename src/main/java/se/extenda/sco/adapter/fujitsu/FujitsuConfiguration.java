package se.extenda.sco.adapter.fujitsu;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import se.extenda.pos.facade.config.fjcc.FujitsuCashChangerAdapter;

public class FujitsuConfiguration {

	private final Properties properties;

	private static final Logger logger = Logger.getLogger(FujitsuConfiguration.class);
	
	private static FujitsuConfiguration instance = null;

	
	public static FujitsuConfiguration getInstance() throws IOException {
		if (instance == null) {
			instance = new FujitsuConfiguration();
		}
		return instance;
	}
	
	private FujitsuConfiguration() throws IOException {
		properties = loadFujitsuProperties();
	}
	
	private Properties loadFujitsuProperties() throws IOException {
		String externalOverrideConfigFileLocation = System.getProperty("fujitsuconfig");
		Properties props = new Properties();
		ClassLoader loader = FujitsuCashChangerAdapter.class.getClassLoader();
		InputStream fujitsuPropertyStream = loader.getResourceAsStream("fujitsuconfig.properties");

		props.load(fujitsuPropertyStream);
		logger.info("Loaded default sco properties " + props);
		if (externalOverrideConfigFileLocation != null) {
			Properties customProps = new Properties();
			try {
				customProps.load(new FileInputStream(externalOverrideConfigFileLocation));
			} catch (FileNotFoundException e) {
				logger.warn("Custom fujitsu file sent as system property could not be found: "
						+ externalOverrideConfigFileLocation, e);
			}
			props.putAll(customProps);
			logger.info("Loaded override fujitsu properties " + customProps);
		}
		return props;
	}
	
	public String getHostname() {
		return properties.getProperty("fujitsu.selfcheckout.hostname");
	}
	
	public int getPortNumber() {
		String portString = properties.getProperty("fujitsu.selfcheckout.port");
		return Integer.parseInt(portString);
	}

	public int getReadTimeoutInMillis() {
		String readTimeout = properties.getProperty("fujitsu.selfcheckout.readtimeout");
		return Integer.parseInt(readTimeout);
	}
	
	public int getConnectTimeoutInMillis() {
		String connectTimeout = properties.getProperty("fujitsu.selfcheckout.connecttimeout");
		return Integer.parseInt(connectTimeout);
	}

	public int getUnexpectedWeightChangeDelayInMillis() {
		String value = properties.getProperty("fujitsu.selfcheckout.unexpectedweightchange.delay");
		return Integer.parseInt(value);
	}

	public int getItemValidationTimeoutDelayInMillis() {
		String value = properties.getProperty("fujitsu.selfcheckout.itemvalidation.delay");
		return Integer.parseInt(value);
	}

}
