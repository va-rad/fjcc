package se.extenda.cashchanger.adapter.fujitsu.api;

import java.io.IOException;
import java.util.List;

import se.extenda.cashchanger.adapter.fujitsu.response.AcceptMoneyStateResponse;
import se.extenda.cashchanger.adapter.fujitsu.response.CashUnit;
import se.extenda.cashchanger.adapter.fujitsu.response.DeviceTestAllResponse;
import se.extenda.cashchanger.adapter.fujitsu.response.DispenseMoneyResponse;
import se.extenda.cashchanger.adapter.fujitsu.response.GetListResponse;
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
	 * Get available devices connected to SCOaaD, e.g:
	 * AlarmBoard, CashDevice, CouponSensor, default, Light, SecureScale
	 *
	 * @return GetListResponse with available devices
	 * @throws IOException
	 */
	GetListResponse getDeviceList() throws IOException;
	
	/**
	 * Test that all devices configured are are responsive and works
	 *
	 * @return DeviceTestAllResponse, which holds the status code, possible errors etc.
	 * @throws IOException
	 */
	DeviceTestAllResponse adminDeviceTestAll() throws IOException;
}
