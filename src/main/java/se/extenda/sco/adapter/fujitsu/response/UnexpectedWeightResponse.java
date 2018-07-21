package se.extenda.sco.adapter.fujitsu.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UnexpectedWeightResponse extends GeneralFujitsuResponse {
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
		return super.toString() + " UnexpectedWeightResponse [status=" + status + ", deltaWeight=" + deltaWeight
				+ ", toString()=" + super.toString() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (int) (deltaWeight ^ (deltaWeight >>> 32));
		result = prime * result + (int) (status ^ (status >>> 32));
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
		UnexpectedWeightResponse other = (UnexpectedWeightResponse) obj;
		if (deltaWeight != other.deltaWeight) {
			return false;
		}
		if (status != other.status) { // NOSONAR
			return false;
		}

		return true;
	}
}
