package se.extenda.sco.adapter.fujitsu.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Representation of a CashUnit in /api/CashDevice/AcceptMoney
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CashUnit {
	
	/** Enumeration of different cashunit types, kept as constants for now */
	public static final String CASHUNIT_TYPE_COIN = "Coin";
	public static final String CASHUNIT_TYPE_NOTE = "Note";

	private String cashUnitId;
	private String cashUnitPosition;
	private int maximumCount;
	private long denomination;
	private String cashUnitType;
	private String currency;
	private int count;
	private int initialCount;
	private int cashUnitStatus;
	
	/**
	 * @return the cashUnitId
	 */
	public String getCashUnitId() {
		return cashUnitId;
	}

	/**
	 * @param cashUnitId the cashUnitId to set
	 */
	public void setCashUnitId(String cashUnitId) {
		this.cashUnitId = cashUnitId;
	}

	/**
	 * @return the cashUnitPosition
	 */
	public String getCashUnitPosition() {
		return cashUnitPosition;
	}

	/**
	 * @param cashUnitPosition the cashUnitPosition to set
	 */
	public void setCashUnitPosition(String cashUnitPosition) {
		this.cashUnitPosition = cashUnitPosition;
	}

	/**
	 * @return the maximumCount
	 */
	public int getMaximumCount() {
		return maximumCount;
	}

	/**
	 * @param maximumCount the maximumCount to set
	 */
	public void setMaximumCount(int maximumCount) {
		this.maximumCount = maximumCount;
	}

	/**
	 * @return the denomination
	 */
	public long getDenomination() {
		return denomination;
	}

	/**
	 * @param denomination the denomination to set
	 */
	public void setDenomination(long denomination) {
		this.denomination = denomination;
	}

	/**
	 * @return the cashUnitType
	 */
	public String getCashUnitType() {
		return cashUnitType;
	}

	/**
	 * @param cashUnitType the cashUnitType to set
	 */
	public void setCashUnitType(String cashUnitType) {
		this.cashUnitType = cashUnitType;
	}

	/**
	 * @return the currency
	 */
	public String getCurrency() {
		return currency;
	}

	/**
	 * @param currency the currency to set
	 */
	public void setCurrency(String currency) {
		this.currency = currency;
	}

	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * @param count the count to set
	 */
	public void setCount(int count) {
		this.count = count;
	}

	/**
	 * @return the initialCount
	 */
	public int getInitialCount() {
		return initialCount;
	}

	/**
	 * @param initialCount the initialCount to set
	 */
	public void setInitialCount(int initialCount) {
		this.initialCount = initialCount;
	}

	/**
	 * @return the cashUnitStatus
	 */
	public int getCashUnitStatus() {
		return cashUnitStatus;
	}

	/**
	 * @param cashUnitStatus the cashUnitStatus to set
	 */
	public void setCashUnitStatus(int cashUnitStatus) {
		this.cashUnitStatus = cashUnitStatus;
	}

	@Override
	public String toString() {
		return " CashUnit {denomination=" + denomination + ", cashUnitType=" + cashUnitType
				+ ", currency=" + currency + ", count=" + count + "}";
	}
}