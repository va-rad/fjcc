package se.extenda.cashchanger.adapter.fujitsu.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AcceptMoneyResponse extends GeneralFujitsuResponse {

	private long status;
	private int receivedAmount;
	private boolean changeEnabled;
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
	 * @return the changeEnabled
	 */
	public boolean isChangeEnabled() {
		return changeEnabled;
	}

	/**
	 * @param changeEnabled the changeEnabled to set
	 */
	public void setChangeEnabled(boolean changeEnabled) {
		this.changeEnabled = changeEnabled;
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
				+ ", changeEnabled=" + changeEnabled + ", cashUnits={{ "; 
		
		for (CashUnit cashUnit : getCashUnits()) {
			str += cashUnit.toString();
		}
		
		return str + " }} ]";
	}

}
