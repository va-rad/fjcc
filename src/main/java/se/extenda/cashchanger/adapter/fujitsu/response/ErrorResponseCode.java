package se.extenda.cashchanger.adapter.fujitsu.response;

import java.util.Arrays;

/**
 * Represents error response codes received from Fujitsu API.
 */
public enum ErrorResponseCode {
	VALIDATION_ERROR(100),
	EXCEPTION_DURING_PROCESSING_OF_DEVICE_ACTION(101),
	DEVICE_UNAVAILABLE_STATE(102),
	SYNCHRONIZATION_ERROR(106),
	INTERNAL_SERVER_ERROR(107),
	SCALE_COMMUNICATION_IS_UNSUCCESSFUL(103),
	CANNOT_READ_WEIGHT(104),
	PLATE_IS_OVERLOADED(108),
	NEGATIVE_WEIGHT(109),
	LIGHT_COMMUNICATION_IS_UNSUCCESSFUL(105);
	
	private long status;
	
	public long getStatus() {
		return status;
	}

	ErrorResponseCode(long status) {
		this.status = status;
	}
	
	 /**
     * Returns the corresponding ErrorResponseCode from a supplied
     * long
     *
     * @param statusCode
     *            the long status code to get response code from
     * @return the status code as ErrorResponseCode
     */
    public static ErrorResponseCode fromLong(long statusCode) {
        return Arrays.stream(ErrorResponseCode.values()).filter(it -> it.status == statusCode).findFirst()
                .orElseThrow(() -> new EnumConstantNotPresentException(ErrorResponseCode.class,
                        Long.toString(statusCode)));
    }
}