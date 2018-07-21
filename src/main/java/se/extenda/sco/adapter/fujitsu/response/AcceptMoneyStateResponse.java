package se.extenda.sco.adapter.fujitsu.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AcceptMoneyStateResponse extends GeneralFujitsuResponse {

	private long status;
	private int receivedAmount;
	private List<CashUnit> cashUnits;


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

	/**
	 * @return the receivedAmount
	 */
	public int getReceivedAmount() {
		return receivedAmount;
	}

	/**
	 * @param receivedAmount the receivedAmount to set
	 */
	public void setReceivedAmount(int receivedAmount) {
		this.receivedAmount = receivedAmount;
	}

	/**
	 * @return the cashUnits
	 */
	public List<CashUnit> getCashUnits() {
		return cashUnits;
	}

	/**
	 * @param cashUnits the cashUnits to set
	 */
	public void setCashUnits(List<CashUnit> cashUnits) {
		this.cashUnits = cashUnits;
	}

	@Override
	public String toString() {
		String str = super.toString() + " AcceptMoneyResponse [status=" + status + ", receivedAmount=" + receivedAmount
				+ ", cashUnits={{ "; 
		
		for (CashUnit cashUnit : getCashUnits()) {
			str += cashUnit.toString();
		}
		
		return str + " }} ]";
	}

}
