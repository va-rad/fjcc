package se.extenda.sco.adapter.fujitsu.response;

import java.util.Arrays;

/**
 * Response codes for StoreWeight and SetWeight calls to the Fujitsu SCOaaD.
 */
public enum RegisterWeightResponseCode{
	UNKNOWN_STATUS(0),

	GENERAL_ERROR(1),

	WEIGHT_REQUIRED(2),
	
	NOT_LIGHT_ITEM(4),

	NOT_LIGHT_WEIGHT(8),

	ITEM_NOT_FOUND(16),

	SUCCESS(32);
	
	private int statusCode;

	RegisterWeightResponseCode(int statusCode) {
		this.statusCode = statusCode;
	}

	/**
	 * Returns the corresponding RegisterWeightResponseCode from a supplied
	 * integer
	 * 
	 * @param statusCode
	 *            the integer status code to get response code from
	 * @return the status code as RegisterWeightResponseCode
	 */
	public static RegisterWeightResponseCode fromLong(long statusCode) {
		return Arrays.stream(RegisterWeightResponseCode.values()).filter(it -> it.statusCode == statusCode).findFirst()
				.orElseThrow(() -> new EnumConstantNotPresentException(RegisterWeightResponseCode.class,
						Long.toString(statusCode)));
	}
}