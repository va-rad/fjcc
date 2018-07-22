package se.extenda.cashchanger.adapter.fujitsu.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The response received when performing a StoreWeight call to the Fujitsu SCO aaD
 * API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StoreWeightResponse extends GeneralFujitsuResponse {
	private int status;

	/**
	 * Get the status
	 * 
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * Set the status
	 * 
	 * @param status
	 *            the status
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + status;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		StoreWeightResponse other = (StoreWeightResponse) obj;
		if (status != other.status) { //NOSONAR
			return false;
		}
		return true;
	}
}
