package se.extenda.cashchanger.adapter.fujitsu.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RollbackAcceptedMoneyResponse extends GeneralFujitsuResponse {

	private long status;

	/**
	 * @return the status
	 */
	public long getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(long status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return super.toString() + " RollbackAcceptedMoneyResponse [status=" + status + "]";
	}

}
