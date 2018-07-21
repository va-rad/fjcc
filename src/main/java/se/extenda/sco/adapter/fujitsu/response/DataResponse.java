package se.extenda.sco.adapter.fujitsu.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataResponse {

	@JsonProperty("data")
	private DataDeviceResponse[] dataDeviceResponses;

	public void setDataDeviceResponses(DataDeviceResponse[] dataDeviceResponses) {
		this.dataDeviceResponses = dataDeviceResponses;
	}

	public DataDeviceResponse[] getDataDeviceResponses() {
		return dataDeviceResponses;
	}

	public String toStxring() {
		StringBuilder stringBuilder = new StringBuilder();
		Arrays.stream(dataDeviceResponses).forEach(stringBuilder::append);
		return stringBuilder.toString();
	}

	@Override
	public String toString() {
		return super.toString() + " DataResponse [dataDeviceResponses=" + Arrays.toString(dataDeviceResponses) + "]";
	}

}
