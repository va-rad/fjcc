package se.extenda.cashchanger.adapter.fujitsu.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataDeviceResponse {

	private String deviceName;

	private String componentName;

	private String scriptPath;

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getComponentName() {
		return componentName;
	}

	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}

	public String getScriptPath() {
		return scriptPath;
	}

	public void setScriptPath(String scriptPath) {
		this.scriptPath = scriptPath;
	}

	@Override
	public String toString() {
		return super.toString() + " DataDeviceResponse [deviceName=" + deviceName + ", componentName=" + componentName
				+ ", scriptPath=" + scriptPath + "]";
	}

}
