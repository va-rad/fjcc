package se.extenda.cashchanger.adapter.fujitsu.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CheckItemResponse extends GeneralFujitsuResponse {

	private long status;
	private long overloadStatus;
	private long deltaWeight;
	private Object stayToOverloaded;

	public long getStatus() {
		return status;
	}

	public void setStatus(long status) {
		this.status = status;
	}

	public long getOverloadStatus() {
		return overloadStatus;
	}

	public void setOverloadStatus(long overloadStatus) {
		this.overloadStatus = overloadStatus;
	}

	public long getDeltaWeight() {
		return deltaWeight;
	}

	public void setDeltaWeight(long deltaWeight) {
		this.deltaWeight = deltaWeight;
	}

	public Object getStayToOverloaded() {
		return stayToOverloaded;
	}

	public void setStayToOverloaded(Object stayToOverloaded) {
		this.stayToOverloaded = stayToOverloaded;
	}

	@Override
	public String toString() {
		return super.toString() + " CheckItemResponse [status=" + status + ", overloadStatus=" + overloadStatus
				+ ", deltaWeight=" + deltaWeight + ", stayToOverloaded=" + stayToOverloaded + "]";
	}

}
