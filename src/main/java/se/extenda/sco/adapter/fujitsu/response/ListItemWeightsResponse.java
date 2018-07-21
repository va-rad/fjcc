package se.extenda.sco.adapter.fujitsu.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ListItemWeightsResponse extends GeneralFujitsuResponse {

	private long status;

	@JsonProperty("items")
	private ItemResponse[] items;

	public long getStatus() {
		return status;
	}

	public void setStatus(long status) {
		this.status = status;
	}

	public ItemResponse[] getItems() {
		return items;
	}

	public void setItems(ItemResponse[] items) {
		this.items = items;
	}


	@Override
	public String toString() {
		return "ListItemWeightsResponse{" +
				"status=" + status +
				", items=" + Arrays.toString(items) +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}

		ListItemWeightsResponse that = (ListItemWeightsResponse) o;

		if (status != that.getStatus()) {
			return false;
		}
		return Arrays.toString(items).equals(Arrays.toString(that.items));
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (int) (status ^ (status >>> 32));
		result = 31 * result + Arrays.hashCode(items);
		return result;
	}
}
