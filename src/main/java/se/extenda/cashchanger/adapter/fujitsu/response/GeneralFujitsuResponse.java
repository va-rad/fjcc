package se.extenda.cashchanger.adapter.fujitsu.response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Operation States:
 * <ul>
 *	<li>0 Success,</li>
 *	<li>1 Failed, </li>
 *	<li>2 NotSupported ,</li>
 *	<li>4 Partially success. </li>
 *	<li>8 In progress</li>
 * </ul>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeneralFujitsuResponse {

	private int operationState;

	@JsonProperty("errors")
	private ErrorResponse[] errorResponses;

	public int getOperationState() {
		return operationState;
	}
	
	public void setOperationState(int operationState) {
		this.operationState = operationState;
	}

	public ErrorResponse[] getErrorResponses() {
		return errorResponses;
	}

	public void setErrorResponses(ErrorResponse[] errorResponses) {
		this.errorResponses = errorResponses;
	}

	@Override
	public String toString() {
		StringBuilder errorResponseStrings = new StringBuilder("");

		if(errorResponses != null) {
			String errors = Stream.of(errorResponses)
					.map(error -> "{" + error.toString() + "}")
					.collect(Collectors.joining(", "));
			errorResponseStrings.append(errors);
		}
		return "GeneralFujitsuResponse [operationState=" + operationState + ", errors=[" + errorResponseStrings.toString()
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(errorResponses);
		result = prime * result + operationState;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		GeneralFujitsuResponse other = (GeneralFujitsuResponse) obj;
		if (!Arrays.equals(errorResponses, other.errorResponses)) {
			return false;
		}
		if (operationState != other.operationState) { // NOSONAR
			return false;
		}

		return true;
	}
	
	/**
	 * Checks if the error responses contains the provided error code.
	 * 
	 * @param code
	 *            the error code to match
	 * @return true, if the error code is found, false otherwise
	 */
	public boolean errorsContainsCode(long code) {
		ArrayList<ErrorResponse> codes = new ArrayList<>(Arrays.asList(getErrorResponses()));
		return (codes.stream()
				.filter(e -> e.getErrorCode() == code)
				.count() != 0 ? true : false);
	}
}
