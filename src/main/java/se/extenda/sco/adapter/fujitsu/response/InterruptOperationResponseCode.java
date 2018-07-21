package se.extenda.sco.adapter.fujitsu.response;

import java.util.Arrays;

public enum InterruptOperationResponseCode {

    NOTHING_TO_INTERRUPT(1),

    SUCCESS(2);

    private int statusCode;

    InterruptOperationResponseCode(int statusCode) {
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
    public static InterruptOperationResponseCode fromLong(long statusCode) {
        return Arrays.stream(InterruptOperationResponseCode.values()).filter(it -> it.statusCode == statusCode).findFirst()
                .orElseThrow(() -> new EnumConstantNotPresentException(InterruptOperationResponseCode.class,
                        Long.toString(statusCode)));
    }
}

