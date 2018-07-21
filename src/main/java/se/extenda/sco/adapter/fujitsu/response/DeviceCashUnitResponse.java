package se.extenda.sco.adapter.fujitsu.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceCashUnitResponse extends GeneralFujitsuResponse {

	private long status;
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
		String str = super.toString() + " DeviceCashUnitResponse [status=" + status + ", cashUnits={{ "; 
		
		for (CashUnit cashUnit : cashUnits) {
			str += cashUnit.toString();
		}
		
		return str + " }} ]";
	}

}
