package se.extenda.sco.adapter.fujitsu.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Response DTO from the call ReturnLastItem to the Fujitsu SCOaaD API, which
 * handles a return of the last item removed from the security scale. It Carries
 * a status value and deltaWeight (weight difference in grams) besides the
 * properties in GeneralFujitsuResponse.
 *
 * Possible status responses:
 * <ul>
 * <li>GeneralError = 1,</li>
 * <li>ItemWeightGreater = 8,</li>
 * <li>ItemWeightLess = 16,</li>
 * <li>FinishedByTimeout = 32,</li>
 * <li>Success = 64,</li>
 * <li>WrongOperation = 128,</li>
 * <li>OperationInterrupted = 512</li>
 * </ul>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReturnLastItemResponse extends GeneralFujitsuResponse {

	private long status;

	public long getStatus() {
		return status;
	}

	public void setStatus(long status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "[ReturnLastItemResponse [status=" + status + ", toString()=" + super.toString() + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			return true;
		}
		final ReturnLastItemResponse other = (ReturnLastItemResponse) obj;
		return other != null && status == ((ReturnLastItemResponse) obj).getStatus();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (int) (status ^ (status >>> 32));
		return result;
	}
}
