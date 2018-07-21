package se.extenda.sco.adapter.fujitsu;

import se.extenda.sco.adapter.fujitsu.RegisterItemNewWeightState.NewWeightState;

/**
 * Interface used for listeners to be informed about state changes in the
 * Register item with new weight flow, see {@link RegisterItemNewWeightState}.
 *
 */
public interface WeightStateChangeListener {

	/**
	 * Notify the listener about a change in the state
	 * 
	 * @param weightState
	 *            - the new, i.e. current, state 
	 */
	void weightChangeStateChanged(NewWeightState weightState);
}
