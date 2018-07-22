package se.extenda.cashchanger.adapter.fujitsu.api;

import java.io.IOException;
import java.util.List;

import se.extenda.cashchanger.adapter.fujitsu.FujitsuSelfCheckoutResponse;
import se.extenda.cashchanger.adapter.fujitsu.response.AcceptMoneyStateResponse;
import se.extenda.cashchanger.adapter.fujitsu.response.CashUnit;
import se.extenda.cashchanger.adapter.fujitsu.response.DeviceTestAllResponse;
import se.extenda.cashchanger.adapter.fujitsu.response.DispenseMoneyResponse;
import se.extenda.cashchanger.adapter.fujitsu.response.GetListResponse;
import se.extenda.cashchanger.adapter.fujitsu.response.InterruptOperationResponseCode;
import se.extenda.cashchanger.adapter.fujitsu.response.ReadWeightResponse;
import se.extenda.cashchanger.adapter.fujitsu.response.RollbackAcceptedMoneyResponse;


/**
 * Communicates with the Fujitsu selfcheckout
 */
public interface FujitsuApi {
	
	void acceptMoney(long totalAmount) throws IOException;
	
	AcceptMoneyStateResponse stopAcceptMoney() throws IOException;
		
	RollbackAcceptedMoneyResponse rollbackAcceptedMoney() throws IOException;
	
	AcceptMoneyStateResponse getAcceptMoneyState() throws IOException;

	DispenseMoneyResponse dispenseMoney(long dispenseAmount, String currency) throws IOException;

	List<CashUnit> getDeviceCashUnits() throws IOException;

	// TODO: Add proper response
	void getDeviceState() throws IOException;

	// TODO: Add proper response
	void setDeviceCashUnits() throws IOException;

	// TODO: Add proper response
	void emptyDeviceCashUnits() throws IOException;
	
	// TODO: Add proper response
	void emptyAllDeviceContent() throws IOException;
	
	// TODO: Add proper response
	void refillDeviceCashUnits() throws IOException;

	// TODO: Add proper response
	void getNoteSignature() throws IOException;

	// TODO: Add proper response
	void scanMedia() throws IOException;
	
	// TODO: Add proper response
	void withdrawCashbox() throws IOException;
	
	
	

	/**
	 * Test that all devices configured are are responsive and works
	 *
	 * @return DeviceTestAllResponse, which holds the status code, possible errors etc.
	 * @throws IOException
	 */
	DeviceTestAllResponse adminDeviceTestAll() throws IOException;

	/**
	 * Clear the weights of a specific barcode
	 * @param barcode
	 * 			Barcode of the item which weights are to be cleared
	 * @return ClearWeightResponse, a response carrying information of either success or error
	 * @throws IOException
	 */
//	ClearWeightResponse clearWeights(Barcode barcode) throws IOException;
	/**
	 * A customer may put his bag on secure scales, so the weight needs to be set to
	 * zero by this command.
	 *
	 * @throws IOException
	 */
	void setScaleSoftZero() throws IOException;

	/**
	 * Sets the current control scale weight as normal.
	 *
	 * @throws IOException
	 */
	void confirmBagScaleWeightOffset() throws IOException;

	/**
	 * Clears the virtual basket and the bag-scale weight offset
	 *
	 * @throws IOException
	 */
	void clearTransaction() throws IOException;

	/**
	 * Call to Fujitsu API for finishing a transaction, used at the end of a trip.
	 *
	 * @throws IOException
	 */
	void finishTransaction() throws IOException;

	/**
	 * Checks is the unexpected weight change listener is currently active or
	 * not.
	 *
	 * @return true, if listener is active, false otherwise
	 */
	boolean isUnexpectedWeightChangeListenerActive();

	/**
	 * Turn off polling of Unexpected Weight Change from Fujitsu API. Stops the
	 * thread and does not wait for current executions to finish. <br>
	 * <br>
	 * Note: There is no guarantee that actively executing tasks will be
	 * successfully stopped.
	 */
	 void stopUnexpectedWeightChangeListener();

	/**
	 * Turn on polling of Unexpected Weight Change from Fujitsu API. A separate
	 * thread will poll the API for an updated state at a set interval defined
	 * by <b>UNEXPECTED_WEIGHT_CHANGE_TIMEOUT</b> constant.
	 *
	 * @throws IOException
	 */
	void startUnexpectedWeightChangeListener() throws IOException;

	/**
	 * Validate a light weight item
	 * @param barcode
	 * 			Barcode of the item to be (light) checked
	 * @return FujitsuSelfCheckoutResponse, a response enum of the operation
	 * @throws IOException
	 */
//	FujitsuSelfCheckoutResponse checkLightItem(final Barcode barcode) throws IOException;

	/**
	 * Check if a item (with no weight or quantity parameters) is put on secure
	 * scale
	 * @param barcode
	 * 			 Barcode of the item to be checked
	 * @param timeout
	 * 	 		 long with a time in milliseconds before timing out
	 * @return FujitsuSelfCheckoutResponse, an enum response value of the operation
	 * @throws IOException
	 */
//	FujitsuSelfCheckoutResponse checkNormalItem(final Barcode barcode, final long timeout) throws IOException;

	/**
	 * Check if a weight item is put on secure scale
	 *
	 * @param barcode
	 *            Barcode of the item to be checked
	 * @param itemWeight
	 *            Weight of the item to be checked
	 * @throws IOException
	 */
//	FujitsuSelfCheckoutResponse checkWeightItem(final Barcode barcode, final String itemWeight, final long timeout) throws IOException;
	/**
	 * Check if a quantity item is put on secure scale
	 * @param barcode
	 *            barcode of the item to be checked
	 * @param quantity
	 *            quantity of the item to be checked
	 * @throws IOException
	 */
//	FujitsuSelfCheckoutResponse checkQuantityItem(final Barcode barcode, final int quantity, final long timeout) throws IOException;
	
	/**
	 * Remove last item registered with CheckItem or LightCheckItem
	 * 
	 * @param timeout
	 *            long with a time in milliseconds before timing out
	 * @return RemoveLastItemResponse, an enum response value of the operation
	 */
//	FujitsuSelfCheckoutResponse removeLastItem(long timeout, Barcode barcode);

	/**
	 * Return the last item removed from the tray back to the tray again
	 * 
	 * @param timeout
	 *            long with a time in milliseconds before timing out
	 * @return ReturnLastItemResponse, an enum response value of the operation
	 */
//	FujitsuSelfCheckoutResponse returnLastItem(long timeout, Barcode barcode);

	/**
	 * Checks if the secure scale is empty.
	 * @return the response as a FujitsuSelfCheckoutResponse
	 *
	 * @throws IOException
	 */
	FujitsuSelfCheckoutResponse isZero() throws IOException;

	/**
	 * Removes the last cached unexpected weight change received from the Fujitsu API.
	 */
	void clearLastUnexpectedWeightChange();

	/**
	 * Lists the weights for a particular item
	 * @throws IOException
	 */
//	ListItemWeightsResponse listItemWeights(final Barcode barcode) throws IOException;
	
	/**
	 * Read the weight on the scale.
	 * 
	 * @return ReadWeightResponse
	 * @throws IOException
	 */
	ReadWeightResponse readWeight() throws IOException;
	
	/**
	 * Store the weight of an item (the item exists in the weight db).
	 * 
	 * @param barcode
	 *            the barcode of the item
	 * @param weight
	 *            the weight of the item
	 * @param quantity
	 *            the quantity of the item (API optional)
	 * @return StoreWeightResponseCode
	 * @throws IOException
	 */
//	RegisterWeightResponseCode storeWeight(Barcode barcode, int weight, int quantity)
//			throws IOException;

	/**
	 * Set the weight for an item which does not exist in the weight db.
	 * 
	 * @param item
	 *            the item to set weight for
	 * @param islightItem
	 *            if the item is a light item
	 * @param weight
	 *            the weight (API optional)
	 * @param quantity
	 *            the quantity of the item (API optional)
	 * @return SetWeightResponseCode
	 * @throws IOException
	 */
//	RegisterWeightResponseCode setWeight(Item item, boolean islightItem, int weight, int quantity) throws IOException;

	/**
	 * Set/change/remove tolerance of an item
	 * @param barcode
	 * 			Barcode of the item to be updated
	 * @param tolerance
	 * 			Integer of the item tolerance (0-100 percent), use null to clear.
	 *
	 * @return the response as a FujitsuSelfCheckoutResponse
	 * @throws IOException
	 */
//	FujitsuSelfCheckoutResponse updateTolerance(final Barcode barcode, final Integer tolerance) throws IOException;

	/**
	 * Interrupts the current SCOaaD request
	 * @throws IOException
	 */
	InterruptOperationResponseCode interruptOperation() throws IOException;

	/**
	 * Get available devices connected to SCOaaD, e.g:
	 * AlarmBoard, CashDevice, CouponSensor, default, Light, SecureScale
	 *
	 * @return GetListResponse with available devices
	 * @throws IOException
	 */
	GetListResponse getDeviceList() throws IOException;

	/**
	 * Clears the virtual basket of of SCOaaD and Bag-scale weight offset and
	 * start SCOaaD transaction. After calling of this method it is possible
	 * to use operations available in transaction.
	 * @throws IOException
	 */
	void startTransaction() throws IOException;

}
