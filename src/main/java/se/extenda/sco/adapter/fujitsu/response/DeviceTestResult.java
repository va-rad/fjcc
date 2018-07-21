package se.extenda.sco.adapter.fujitsu.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DeviceTestResult {
	
	@JsonProperty("deviceName")
	private String deviceName;

	@JsonProperty("state")
	private GeneralFujitsuResponse state;
	
	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	
	public GeneralFujitsuResponse getState() {
		return state;
	}

	public void setState(GeneralFujitsuResponse state) {
		this.state = state;
	}
	
	@Override
	public String toString() {
		return "DeviceTestResult [deviceName=" + deviceName + ", state=[" + state + "]";
	}

}
