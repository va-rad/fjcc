package se.extenda.cashchanger.adapter.fujitsu.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *  Response to a api/admin/Device/GetList call
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetListResponse {

	@JsonProperty("data")
	private List<Device> dataDeviceResponses;

	public void setDataDeviceResponses(List<Device> dataDeviceResponses) {
		this.dataDeviceResponses = dataDeviceResponses;
	}

	/**
	 * @return List of devices
	 */
	public List<Device> getDataDeviceResponses() {
		return dataDeviceResponses;
	}

}
