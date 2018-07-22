package se.extenda.cashchanger.adapter.fujitsu.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Response DTO from the call isZero to the Fujitsu SCOaaD API, which detects if
 * the secure scale is empty. It carries a status value besides the properties
 * in GeneralFujitsuResponse with a possible value of:
 *
 * <ul>
 * <li>1 - GeneralError,</li>
 * <li>32 - FinishedByTimeout,</li>
 * <li>64 – Success,</li>
 * <li>512 – OperationInterrupted</li>
 * <ul>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class IsZeroResponse extends GeneralFujitsuResponse {
	private long status;

	/**
	 * Get the status of the response.
	 */
	public long getStatus() {
		return status;
	}

	/**
	 * Set the response status
	 */
	public void setStatus(long status) {
		this.status = status;
	}

	/**
	 * Convenience method for checking if the response has returned that scales are
	 * indeed empty
	 * 
	 * @return true, if the scales are empty, false otherwise
	 */
	public boolean isScalesFree() {
		return status == 64L;
	}
}
