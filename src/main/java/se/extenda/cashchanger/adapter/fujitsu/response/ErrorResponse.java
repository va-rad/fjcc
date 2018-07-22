package se.extenda.cashchanger.adapter.fujitsu.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * <hERROR CODES:
	 <ul>
 		<li>Code	Description</li>
		<li>100	Validation error</li>
		<li>101	Exception during processing of device action</li>
		<li>102	The device is in unavailable state</li>
		<li>106	Synchronization error</li>
		<li>107	Internal server error</li>

		<li>Secure scale</li>
		<li>103	Communication response is unsuccessful</li>
		<li>104	Scales error, can't read weight</li>
		<li>108	Scales error, plate is overloaded</li>
		<li>109	Scales returned a negative weight</li>

		<li>Light device</li>
		<li>105	Communication response is unsuccessful</li>
 	</ul>
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorResponse {
	private long errorCode;
	private String errorText;
	private String extendedError;

	public long getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(long errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorText() {
		return errorText;
	}

	public void setErrorText(String errorText) {
		this.errorText = errorText;
	}

	public String getExtendedError() {
		return extendedError;
	}

	public void setExtendedError(String extendedError) {
		this.extendedError = extendedError;
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("errorCode=").append(errorCode).append(", ");
		if (errorText != null) {
			stringBuilder.append("errorText=\"").append(errorText).append("\", ");
		}
		if (extendedError != null) {
			stringBuilder.append("extendedError=\"").append(extendedError).append("\"");
		}
		return stringBuilder.toString();

	}
}
