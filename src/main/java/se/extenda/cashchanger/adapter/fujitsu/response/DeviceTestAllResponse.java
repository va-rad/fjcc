package se.extenda.cashchanger.adapter.fujitsu.response;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceTestAllResponse {

	@JsonProperty("data")
	private DeviceTestResult[] data;
	
	public void setData(DeviceTestResult[] data) {
		this.data = data;
	}

	public DeviceTestResult[] getData() {
		return data;
	}

	@Override
	public String toString() {
		String dataStrings = Stream.of(data)
				.map(device -> new StringBuilder("{").append(device.toString()).append("}"))
				.collect(Collectors.joining(", "));
		return "DeviceTestAllResponse [data=[" + dataStrings + "]";
	}
}
