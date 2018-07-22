package se.extenda.cashchanger.adapter.fujitsu;

import java.util.Arrays;

/**
 * Java representation of a response from the call UnexpectedWeightChange.
 */
public enum UnexpectedWeightChangeResponse {
	UNKNOWN_STATUS(0),

	GENERAL_ERROR(1),

	UNEXPECTED_REMOVE(2),

	UNEXPECTED_ADD(4),

	SUCCESS(8);

	private int statusCode;

	UnexpectedWeightChangeResponse(int statusCode) {
		this.statusCode = statusCode;
	}

	public static UnexpectedWeightChangeResponse fromLong(long statusCode) {
		return Arrays.stream(UnexpectedWeightChangeResponse.values()).filter(it -> it.statusCode == statusCode).findFirst()
				.orElseThrow(() -> new EnumConstantNotPresentException(UnexpectedWeightChangeResponse.class,
						Long.toString(statusCode)));
	}

}
