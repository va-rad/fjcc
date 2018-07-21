package se.extenda.pos.facade.config.fjcc;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Component;

import se.extenda.pos.facade.api.Currency;
import se.extenda.pos.facade.api.Logger;
import se.extenda.pos.facade.api.PosAccessor;
import se.extenda.pos.facade.api.Session;
import se.extenda.pos.facade.api.Transaction;
import se.extenda.pos.facade.api.ui.PromptControl;
import se.extenda.pos.facade.spi.peripherals.cashchanger.CashChangerAdapter;
import se.extenda.pos.facade.spi.peripherals.cashchanger.CashChangerDenomination;
import se.extenda.pos.facade.spi.peripherals.cashchanger.CashChangerOverdispenceException;
import se.extenda.pos.facade.spi.peripherals.cashchanger.CashExchangeData;
import se.extenda.pos.facade.spi.peripherals.cashchanger.CashbackActionType;
import se.extenda.pos.facade.spi.peripherals.cashchanger.PeripheralException;
import se.extenda.sco.adapter.fujitsu.api.FujitsuApi;
import se.extenda.sco.adapter.fujitsu.api.FujitsuApiImpl;
import se.extenda.sco.adapter.fujitsu.response.AcceptMoneyStateResponse;
import se.extenda.sco.adapter.fujitsu.response.CashUnit;
import se.extenda.sco.adapter.fujitsu.response.DispenseMoneyResponse;

@Component
public class FujitsuCashChangerAdapter implements CashChangerAdapter {
	
	private FujitsuApi api;

	// Is the factor the local currency needs to be multiplied with to get the smallest 
    // denomination of the local currency
    // TODO: This needs to be configurable for each currency!
    private int smallestDenominationFactor = 100;
    
    private String currency;
	
//	@PostConstruct
//	public void init() {
//		// TODO: This is now hardcoded for EUR!!
//		this.smallestDenominationFactor = 100;
//		
//		if (this.api == null) {
//			this.api = new FujitsuApiImpl();
//		}
//	}
	
	private void setCurrency(PosAccessor posAccessor) {
		if (this.currency == null || this.currency.isEmpty()) {
			Currency currency = posAccessor.getRepositories().getCurrencyRepository().getLocalCurrency();
			this.currency = currency.getCurrencyID();
		}
	}
	
	private long getAmountInSmallestDenomination(BigDecimal amount) {
		long longAmount = amount.multiply(new BigDecimal(smallestDenominationFactor)).longValue();
		
		return longAmount;
	}

	@Override
	public void abortReplenish(PosAccessor arg0) throws PeripheralException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void acceptInsertedCash(PosAccessor posAccessor, BigDecimal dispenseBeforeAccept) throws PeripheralException {
		Logger.info(this, "acceptInsertedCash");
		
		try {
			setCurrency(posAccessor);
		
			api.dispenseMoney(getAmountInSmallestDenomination(dispenseBeforeAccept),this.currency);
			
			api.stopAcceptMoney();
			
			beginAcceptingCash(posAccessor);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void beginAcceptingCash(PosAccessor posAccessor) throws PeripheralException {
		Logger.info(this, "beginAcceptingCash");
		
		if (this.api == null) {
			this.api = new FujitsuApiImpl();
		}

		PromptControl prompter = null;
		
		try {
			prompter = posAccessor.getUI().getCashierPrompter().showProgress("CashChanger", "Connecting to cashchanger");
			
			// check the state of the cc before
			api.acceptMoney(1000000000);
			
			// check the state of the cc after to see that everything went okay
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			prompter.stop();
		}
	}

	@Override
	public void beginAcceptingCashForExchange(PosAccessor arg0) throws PeripheralException {
		Logger.info(this, "beginAcceptingCashForExchange");
		
	}

	@Override
	public void beginReplenish(PosAccessor arg0) throws PeripheralException {
		Logger.info(this, "beginReplenish");
		
	}

	@Override
	public boolean canDispense(PosAccessor posAccessor, BigDecimal dispenseAmount) throws PeripheralException {
		Logger.info(this, "canDispense");
		
		if (dispenseAmount.compareTo(BigDecimal.ZERO) < 1) {
			return true;
		}
		
		long dispenseSmallestDenomAmount = dispenseAmount.multiply(new BigDecimal(smallestDenominationFactor)).longValue(); 
		
		try {
			// get all cash units
			List<CashUnit> cashUnits = api.getDeviceCashUnits();
			
			// get the sum of all denominations
			long totalSum = 0;
			
			for (CashUnit cashUnit : cashUnits) {
				totalSum += cashUnit.getDenomination()*cashUnit.getCount();
			}
			
			if (dispenseSmallestDenomAmount > totalSum) {
				return false;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}

	@Override
	public void closeDevice(PosAccessor posAccessor) throws PeripheralException {
		Logger.info(this, "closeDevice");
		
	}

	@Override
	public void dispense(PosAccessor posAccessor, BigDecimal dispenseAmount, boolean acceptMoreMoney) throws PeripheralException {
		Logger.info(this, "dispense");
		
		
		try {
			setCurrency(posAccessor);

			//TODO: For Refund, shouldn't already inserted cash be trollbacked as well. Is that taken care of in ejectInsertedCash, or should this be taken
			// care of here as well?
			
			DispenseMoneyResponse resp = api.dispenseMoney(getAmountInSmallestDenomination(dispenseAmount),this.currency);

			/*
			 * 0 – Success, 
			 * 1 - GeneralError,
			 * 2 – NotEnoughMoney
			 */
			
			// if success
			if (resp != null && resp.getStatus() == 0) {
				if (acceptMoreMoney) {
					beginAcceptingCash(posAccessor);				
				}
				api.stopAcceptMoney();
			} else {
				api.stopAcceptMoney();
				posAccessor.getUI().getCashierPrompter().showErrorMessage("CashChanger", "Unable to dispense money");
				throw new PeripheralException("Unable to dispense money");
			
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public boolean ejectInsertedCash(PosAccessor posAccessor, boolean arg1) throws PeripheralException {
		Logger.info(this, "ejectInsertedCash");
		
		try {
			api.rollbackAcceptedMoney();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}

	@Override
	public void endDeposit(PosAccessor arg0, int arg1) throws PeripheralException {
		Logger.info(this, "endDeposit");
		
	}

	@Override
	public BigDecimal endReplenish(PosAccessor arg0) throws PeripheralException {
		Logger.info(this, "endReplenish");
		return null;
	}

	@Override
	public void exchange(PosAccessor arg0, CashExchangeData arg1) throws PeripheralException {
		Logger.info(this, "exchange");
		
	}

	@Override
	public void fixDeposit(PosAccessor arg0) throws PeripheralException {
		Logger.info(this, "fixDeposit");
		
	}

	@Override
	public List<CashChangerDenomination> getCashCounts(PosAccessor arg0) throws PeripheralException {
		Logger.info(this, "getCashCounts");
		return null;
	}

	@Override
	public CashbackActionType getCashbackActionType(PosAccessor arg0) {
		Logger.info(this, "getCashbackActionType");
		return CashbackActionType.POST_FINALIZATION;
	}

	@Override
	public int getDeviceStatus(PosAccessor arg0) throws PeripheralException {
		Logger.info(this, "getDeviceStatus");
		return 0;
	}

	@Override
	public BigDecimal getFixedAmount(PosAccessor arg0) {
		Logger.info(this, "getFixedAmount");
		return null;
	}

	@Override
	public int getFullStatus(PosAccessor arg0) throws PeripheralException {
		Logger.info(this, "getFullStatus");
		return 0;
	}

	@Override
	public BigDecimal getInsertedAmount(PosAccessor posAccessor) throws PeripheralException {
		Logger.info(this, "getInsertedAmount");
		return peekInsertedAmount(posAccessor);
	}

	@Override
	public BigDecimal getPeekAmount(PosAccessor arg0) {
		Logger.info(this, "getPeekAmount");
		return null;
	}

	@Override
	public void handleOverdispenseExeption(PosAccessor arg0, CashChangerOverdispenceException arg1, Transaction arg2,
			Session arg3) {
		Logger.info(this, "handleOverdispenseExeption");
		
	}

	@Override
	public void lockSafe(PosAccessor arg0) throws PeripheralException {
		Logger.info(this, "lockSafe");
		
	}

	@Override
	public void noteTransfer(PosAccessor arg0) throws PeripheralException {
		Logger.info(this, "noteTransfer");
		
	}

	@Override
	public void openCover(PosAccessor arg0) throws PeripheralException {
		Logger.info(this, "openCover");
		
	}

	@Override
	public Object operation(PosAccessor arg0, String arg1) throws PeripheralException {
		Logger.info(this, "operation");
		return null;
	}

	@Override
	public BigDecimal peekInsertedAmount(PosAccessor posAccessor) {
		Logger.info(this, "peekInsertedAmount");
		
		try {
			AcceptMoneyStateResponse resp = api.getAcceptMoneyState();
			
			if (resp != null) {
				return new BigDecimal(resp.getReceivedAmount()).divide(new BigDecimal(smallestDenominationFactor));
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public BigDecimal pickup(PosAccessor arg0) throws PeripheralException {
		Logger.info(this, "pickup");
		return null;
	}

	@Override
	public BigDecimal pickup(PosAccessor arg0, Collection<CashChangerDenomination> arg1) throws PeripheralException {
		Logger.info(this, "pickup");
		return null;
	}

	@Override
	public void releaseSafe(PosAccessor arg0) throws PeripheralException {
		Logger.info(this, "releaseSafe");
		
	}

	@Override
	public void secureCash(PosAccessor arg0, String arg1) throws PeripheralException {
		Logger.info(this, "secureCash");
		
	}

	@Override
	public void setFixedAmount(PosAccessor arg0, BigDecimal arg1) {
		Logger.info(this, "setFixedAmount");
		
	}

	@Override
	public void setPeekAmount(PosAccessor arg0, BigDecimal arg1) {
		Logger.info(this, "setPeekAmount");
		
	}

	@Override
	public void weakInitialiseDevice(PosAccessor arg0) throws PeripheralException {
		Logger.info(this, "weakInitialiseDevice");
		
	}

}
