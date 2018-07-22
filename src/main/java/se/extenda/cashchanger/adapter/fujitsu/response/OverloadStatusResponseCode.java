package se.extenda.cashchanger.adapter.fujitsu.response;

import java.util.Arrays;

/**
 * Represents the Overload status from Fujitsu API.  
 */
public enum OverloadStatusResponseCode {
	NO_OVERLOAD(0),
	SOON_OVERLOADED_WEIGHT(1),
	OVERLOADED_WEIGHT(2);
		
	private int statusCode;

	OverloadStatusResponseCode(int statusCode) {
		this.statusCode = statusCode;
	}

	/**
	 * Returns the corresponding OverloadStatusResponseCode from a supplied
	 * long
	 * 
	 * @param statusCode
	 *            the long status code to get response code from
	 * @return the status code as OverloadStatusResponseCode
	 */
	public static OverloadStatusResponseCode fromLong(long statusCode) {
		return Arrays.stream(OverloadStatusResponseCode.values()).filter(it -> it.statusCode == statusCode).findFirst()
				.orElseThrow(() -> new EnumConstantNotPresentException(OverloadStatusResponseCode.class,
						Long.toString(statusCode)));
	}
}
