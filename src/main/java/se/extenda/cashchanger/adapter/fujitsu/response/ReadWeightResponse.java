package se.extenda.cashchanger.adapter.fujitsu.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents the response received for a ReadWeight call to the Fujitsu SCO
 * aaD API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReadWeightResponse extends GeneralFujitsuResponse{
	private int weight;
	private int overloadStatus;
	private String stayToOverloaded;
	
	/**
	 * The weight currently read from the scale
	 * 
	 * @return the weight on the scale
	 */
	public int getWeight() {
		return weight;
	}
	
	/**
	 * Set the weight on the scale
	 * 
	 * @param weight
	 *            the weight on the scale
	 */
	public void setWeight(int weight) {
		this.weight = weight;
	}
	
	/**
	 * Get the overload status of the scale, i.e. if the scale has reached, or is
	 * close to being in overloaded status.
	 * 
	 * @return the overload status
	 */
	public int getOverloadStatus() {
		return overloadStatus;
	}
	
	/**
	 * Set the overload status.
	 * 
	 * @param overloadStatus
	 *            the overload status
	 */
	public void setOverloadStatus(int overloadStatus) {
		this.overloadStatus = overloadStatus;
	}
	
	/**
	 * Get stay weight to overloaded in gram
	 * 
	 * @return the stay to overload weight
	 */
	public String getStayToOverloaded() {
		return stayToOverloaded;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + overloadStatus;
		result = prime * result + ((stayToOverloaded == null) ? 0 : stayToOverloaded.hashCode());
		result = prime * result + weight;
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
		ReadWeightResponse other = (ReadWeightResponse) obj;
		if (overloadStatus != other.overloadStatus) {
			return false;
		}
		if (stayToOverloaded == null) {
			if (other.stayToOverloaded != null) {
				return false;
			}
		} else if (!stayToOverloaded.equals(other.stayToOverloaded)) {
			return false;
		}
		if (weight != other.weight) { //NOSONAR
			return false;
		}
		return true;
	}

	/**
	 * Set stay weight to overloaded
	 * 
	 * @param stayToOverloaded
	 *            the stay to overload weight
	 */
	public void setStayToOverloaded(String stayToOverloaded) {
		this.stayToOverloaded = stayToOverloaded;
	}
	
}
