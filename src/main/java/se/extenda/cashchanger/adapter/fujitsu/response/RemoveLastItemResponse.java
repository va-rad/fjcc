package se.extenda.cashchanger.adapter.fujitsu.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Response DTO from the call RemoveLastItem to the Fujitsu SCOaaD API, which
 * handles a removal of the last item on the security scale. It Carries a status
 * value and deltaWeight (weight difference in grams) besides the properties in
 * GeneralFujitsuResponse.
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
public class RemoveLastItemResponse extends GeneralFujitsuResponse {

	private long status;

	private long deltaWeight;

	public long getStatus() {
		return status;
	}

	public void setStatus(long status) {
		this.status = status;
	}

	public long getDeltaWeight() {
		return deltaWeight;
	}

	public void setDeltaWeight(long deltaWeight) {
		this.deltaWeight = deltaWeight;
	}

	@Override
	public String toString() {
		return "[RemoveLastItemResponse [status=" + status + ", deltaWeight=" + deltaWeight + ", toString()="
				+ super.toString() + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			return true;
		}
		final RemoveLastItemResponse other = (RemoveLastItemResponse) obj;
		return other != null && deltaWeight == ((RemoveLastItemResponse) obj).deltaWeight
				&& status == ((RemoveLastItemResponse) obj).status;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (int) (deltaWeight ^ (deltaWeight >>> 32));
		result = prime * result + (int) (status ^ (status >>> 32));
		return result;
	}
}