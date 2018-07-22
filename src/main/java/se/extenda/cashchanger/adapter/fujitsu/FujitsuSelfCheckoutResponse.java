package se.extenda.cashchanger.adapter.fujitsu;

import java.util.Arrays;

/**
 * Possible responses from the fujitsu selfcheckout api
 */
public enum FujitsuSelfCheckoutResponse {

	UNKNOWN_STATUS(0),

	GENERAL_ERROR(1),

	ITEM_NOT_FOUND(2),

	ITEM_WEIGHT_NOT_FOUND(4),

	ITEM_WEIGHT_GREATER(8),

	ITEM_WEIGHT_LESS(16),

	FINISHED_BY_TIMEOUT(32),

	SUCCESS(64),

	WRONG_OPERATION(128),

	SCALES_NOT_EMPTY(256),

	OPERATION_INTERRUPTED(512);

	private int statusCode;

	FujitsuSelfCheckoutResponse(int statusCode) {
		this.statusCode = statusCode;
	}

	public static FujitsuSelfCheckoutResponse fromLong(long statusCode) {
		return Arrays.stream(FujitsuSelfCheckoutResponse.values()).filter(it -> it.statusCode == statusCode).findFirst()
				.orElseThrow(() -> new EnumConstantNotPresentException(FujitsuSelfCheckoutResponse.class,
						Long.toString(statusCode)));
	}

}
