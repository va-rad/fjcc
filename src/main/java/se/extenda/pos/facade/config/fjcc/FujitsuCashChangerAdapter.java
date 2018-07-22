package se.extenda.pos.facade.config.fjcc;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

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
	// TODO: This is now hardcoded for EUR!!
    private int smallestDenominationFactor = 100;
    
    private String currency;
	
	@PostConstruct
	public void init() {
		
		if (this.api == null) {
			this.api = new FujitsuApiImpl();
		}
	}
	
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
			AcceptMoneyStateResponse stopResp = api.stopAcceptMoney();

			if (dispenseBeforeAccept.compareTo(BigDecimal.ZERO) > 0) {
				setCurrency(posAccessor);
				DispenseMoneyResponse dispResp = api.dispenseMoney(getAmountInSmallestDenomination(dispenseBeforeAccept),this.currency);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void beginAcceptingCash(PosAccessor posAccessor) throws PeripheralException {
		Logger.info(this, "beginAcceptingCash");

		PromptControl prompter = null;
		
		try {
			prompter = posAccessor.getUI().getCashierPrompter().showProgress("CashChanger", "Connecting to cashchanger");
			
			// Start accepting cash in the cashchanger. Need to add a max amount. Add a very large number since we don't
			// want the cashchanger to finalize this method by it self, we always use stopAcceptMoney instead.
			api.acceptMoney(smallestDenominationFactor*1000000);

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

		List<CashUnit> cashUnits = null;

		try {
			// get all cash units
			cashUnits = api.getDeviceCashUnits();
			
			if (cashUnits == null) {
				return false;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			

		/*
		 * Check if the cashchanger contains denominations to be able to dispense the correct amount
		 */
		// Sort cashUnits per denomination, the largest denom first
		Collections.sort(cashUnits);

		String msg = "Can dispense with denomination:\n";
		long remainingDispenseAmount = dispenseSmallestDenomAmount;
		boolean finished = false;
		for (CashUnit cashUnit : cashUnits) {
			long denomination = cashUnit.getDenomination();
			long count=0;

			try {
				for (int j = 0; j < cashUnit.getCount(); j++) {
					long rem = remainingDispenseAmount - cashUnit.getDenomination();
					
					if (rem > 0) {
						count++;
						remainingDispenseAmount = rem;
					} else if (rem == 0) {
						count++;
						finished = true;
						break;
					} else {
						break;
					} 
				}
			} finally {
				if (count > 0) {
					msg += String.valueOf(denomination) + ": " + String.valueOf(count) + "\n";				
				}
				
				if (finished) {
					Logger.info(this, msg);
					return true;
				}
			}
		}

		return false;
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

			api.stopAcceptMoney();

			//TODO: For Refund, shouldn't already inserted cash be trollbacked as well. Is that taken care of in ejectInsertedCash, or should this be taken
			// care of here as well?
			
			DispenseMoneyResponse resp = api.dispenseMoney(getAmountInSmallestDenomination(dispenseAmount),this.currency);

			
			/* 
			 * Check if success
			 * 			
			 * 0 – Success, 
			 * 1 - GeneralError,
			 * 2 – NotEnoughMoney
			 */
			if (resp != null && resp.getStatus() == 0) {
				if (acceptMoreMoney) {
					beginAcceptingCash(posAccessor);				
				}
			} else {
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
		
		// Do nothing?
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
		return CashbackActionType.POST_TENDER;
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
