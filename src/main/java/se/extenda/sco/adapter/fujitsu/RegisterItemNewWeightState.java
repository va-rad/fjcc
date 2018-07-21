package se.extenda.sco.adapter.fujitsu;

import se.extenda.sco.adapter.fujitsu.response.RegisterWeightResponseCode;

/**
 * Keeps track of the current state of the attendant functionality "Register
 * item with new weight". This scenario spans several calls to the Fujitsu SCOaaD
 * API, and therefore must maintain a state.
 * 
 * The possible sequence of calls to the Fujitsu SCOaaD while registering a new weight
 * for an item is listed below, with corresponding NewWeightState:
 * 
 * <p>
 * 
 * <pre>
 * <b>Fujitsu API Call	State (after call finished)</b>
 * N/A					INACTIVE
 * CheckItem			IN_PROGRESS
 * StoreWeight			IN_PROGRESS (successful)
 * CheckItem			FINISHED			
 * N/A					INACTIVE
 *  
 * <b>Fujitsu API Call	State (after call finished)</b>		
 * N/A					INACTIVE
 * CheckItem			IN_PROGRESS
 * StoreWeight			ABORTED (unsuccessful)
 * RemoveItem			INACTIVE
 * 
 * <b>Fujitsu API Call	State (after call finished)</b>		
 * N/A					INACTIVE
 * CheckItem			IN_PROGRESS
 * SetWeight			IN_PROGRESS (successful)
 * CheckItem			FINISHED			
 * N/A					INACTIVE
 * 
 * <b>Fujitsu API Call	State (after call finished)</b>		
 * N/A					INACTIVE
 * CheckItem			IN_PROGRESS
 * SetWeight			ABORTED (unsuccessful)
 * RemoveItem			INACTIVE
 * </pre>
  */
public class RegisterItemNewWeightState {
	private NewWeightState newWeightState = NewWeightState.INACTIVE;
	
	/**
	 * The response which caused the new weight registration process to be aborted.
	 * Used for user message information.
	 */
	private RegisterWeightResponseCode abortResponse;
	
	/**
	 * Listener for changes in the state.
	 */
	private WeightStateChangeListener weightStateChangeListener; 

	/**
	 * Represents the different states used to keep track of the Register item
	 * with new weight flow.
	 * <p>
	 * There are four different states:
	 * <p>
	 * <b>IN_PROGRESS</b> - indicates that the Register item with new weight
	 * flow has been started.
	 * <p>
	 * <b>FINISHED</b> - the flow has been completed, but not deactivated, i.e.
	 * no more calls to the Fujitsu API must be done.
	 * <p>
	 * <b>INACTIVE</b> - when the new weight has been registered in the Fujitsu
	 * weight db, and all adapter states have been cleared.
	 * <p>
	 * <b>ABORTED</b> - the Register item with new weight flow has for some
	 * reason been aborted without finishing, i.e. due to some failure of some
	 * kind.
	 */
	public enum NewWeightState {
		IN_PROGRESS, ABORTED, INACTIVE, FINISHED
	}
	
	/**
	 * Create a new RegisterItemNewWeightState with a provided initial state.
	 * 
	 * @param newWeightState
	 *            the initial state to set
	 */
	public RegisterItemNewWeightState(NewWeightState newWeightState) {
		this(newWeightState, null);
	}

	/**
	 * Create a new RegisterItemNewWeightState with a provided initial state.
	 * 
	 * @param newWeightState
	 *            the initial state to set
	 * 
	 * @param weightStateChangeListener
	 *            the listener for weight state changes
	 */
	public RegisterItemNewWeightState(NewWeightState newWeightState, WeightStateChangeListener weightStateChangeListener) {
		this.newWeightState = newWeightState;
		this.weightStateChangeListener = weightStateChangeListener;
		notifyStateListener();
	}

	
	/**
	 * Gets the current state of the Register item with new weight flow
	 * 
	 * @return the current state
	 */
	public NewWeightState getRegistrationState() {
		return this.newWeightState;
	}
	
	/**
	 * Sets the current state of the Register item with new weight flow
	 * 
	 * @param newWeightState
	 *            the current state
	 */
	public void setRegistrationState(NewWeightState newWeightState) {
		this.newWeightState = newWeightState;
		notifyStateListener();
	}

	/**
	 * Convenience method for checking if Register item with new weight flow is
	 * active
	 * 
	 * @return true, if Register item with new weight flow is active, false
	 *         otherwise
	 */
	public boolean isRegistrationInProgress() {
		return this.newWeightState == NewWeightState.IN_PROGRESS;
	}

	/**
	 * Convenience method for checking if Register item with new weight flow is
	 * aborted
	 * 
	 * @return true, if Register item with new weight flow is aborted, false
	 *         otherwise
	 */
	public boolean isRegistrationAborted() {
		return this.newWeightState == NewWeightState.ABORTED;
	}

	/**
	 * Convenience method for checking if Register item with new weight flow is
	 * inactive
	 * 
	 * @return true, if Register item with new weight flow is inactive, false
	 *         otherwise
	 */
	public boolean isRegistrationInactive() {
		return this.newWeightState == NewWeightState.INACTIVE;
	}
	
	/**
	 * Convenience method for checking if Register item with new weight flow is
	 * finished
	 * 
	 * @return true, if Register item with new weight flow is finished, false
	 *         otherwise
	 */
	public boolean isRegistrationFinished() {
		return this.newWeightState == NewWeightState.FINISHED;
	}

	/**
	 * Get the original response which caused the weight registration process to
	 * be aborted.
	 * 
	 * Note: Will return null if no response is set.
	 * 
	 * @return the abort weight registration response received, or null
	 */
	public RegisterWeightResponseCode getAbortResponse() {
		return abortResponse;
	}

	/**
	 * Set the response which caused the weight registration process to be
	 * aborted. Used for user information.
	 * 
	 * @abortResponse the abort weight registration response
	 */
	public void setAbortResponse(RegisterWeightResponseCode abortResponse) {
		this.abortResponse = abortResponse;
	}
	
	/**
	 * Convenience method for reseting the registration state to normal, i.e. to
	 * inactive.
	 */
	public void clearRegistrationState() {
		this.abortResponse = null;
		this.newWeightState = NewWeightState.INACTIVE;
		notifyStateListener();
	}
	
	/**
	 * Set a listener to listen for changes in the Register item with new
	 * weight flow.
	 */
	public void setWeightStateChangeListener(WeightStateChangeListener weightStateChangeListener) {
		this.weightStateChangeListener = weightStateChangeListener;
	}

	/**
	 * Get the current weight state change listener. 
	 * <p>
	 * NOTE: Can be null
	 * 
	 * @return a WeightStateChangeListener instance if set, otherwise null.
	 */
	public WeightStateChangeListener getWeightStateChangeListener() {
		return this.weightStateChangeListener;
	}
	
	/**
	 * Notifies the listener when the weight state has changed.
	 */
	private void notifyStateListener() {
		if(this.weightStateChangeListener != null) {
			this.weightStateChangeListener.weightChangeStateChanged(newWeightState);
		}
	}
}
