package se.extenda.pos.facade.config.fjcc;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import se.extenda.cashchanger.adapter.fujitsu.api.FujitsuApi;
import se.extenda.cashchanger.adapter.fujitsu.api.FujitsuApiImpl;
import se.extenda.cashchanger.adapter.fujitsu.response.AcceptMoneyStateResponse;
import se.extenda.cashchanger.adapter.fujitsu.response.CashUnit;
import se.extenda.cashchanger.adapter.fujitsu.response.DispenseMoneyResponse;
import se.extenda.pos.facade.api.Currency;
import se.extenda.pos.facade.api.Logger;
import se.extenda.pos.facade.api.PosAccessor;
import se.extenda.pos.facade.api.Session;
import se.extenda.pos.facade.api.Transaction;
import se.extenda.pos.facade.api.contexts.AfterOrderFinalizedContext;
import se.extenda.pos.facade.api.ui.PromptControl;
import se.extenda.pos.facade.spi.actions.AfterOrderFinalizedAction;
import se.extenda.pos.facade.spi.peripherals.cashchanger.CashChangerAdapter;
import se.extenda.pos.facade.spi.peripherals.cashchanger.CashChangerDenomination;
import se.extenda.pos.facade.spi.peripherals.cashchanger.CashChangerOverdispenceException;
import se.extenda.pos.facade.spi.peripherals.cashchanger.CashExchangeData;
import se.extenda.pos.facade.spi.peripherals.cashchanger.CashbackActionType;
import se.extenda.pos.facade.spi.peripherals.cashchanger.PeripheralException;

@Component
public class FujitsuCashChangerAdapter implements CashChangerAdapter, AfterOrderFinalizedAction {
	
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
	public void abortReplenish(PosAccessor posAccessor) throws PeripheralException {
		Logger.info(this, "abortReplenish");
				
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
	public void beginAcceptingCashForExchange(PosAccessor posAccessor) throws PeripheralException {
		Logger.info(this, "beginAcceptingCashForExchange");
		
	}

	@Override
	public void beginReplenish(PosAccessor posAccessor) throws PeripheralException {
		Logger.info(this, "beginReplenish");
		
	}
	

	@Override
	public boolean canDispense(PosAccessor posAccessor, BigDecimal amountToDispense) throws PeripheralException {
		Logger.info(this, "canDispense");
		
		if (amountToDispense.compareTo(BigDecimal.ZERO) < 1) {
			return true;
		}
		
		long dispenseSmallestDenomAmount = amountToDispense.multiply(new BigDecimal(smallestDenominationFactor)).longValue(); 

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
	public void dispense(PosAccessor posAccessor, BigDecimal amountToDispense, boolean shouldStartAcceptingMore) throws PeripheralException {
		Logger.info(this, "dispense");
		
		try {
			AcceptMoneyStateResponse stopResp = api.stopAcceptMoney();

			//TODO: For Refund, shouldn't already inserted cash be trollbacked as well. Is that taken care of in ejectInsertedCash, or should this be taken
			// care of here as well?
			setCurrency(posAccessor);			
			DispenseMoneyResponse resp = api.dispenseMoney(getAmountInSmallestDenomination(amountToDispense),this.currency);

			// We must begin accepting cash again, otherwise there can be aa problem with a mixed refund receipt
			beginAcceptingCash(posAccessor);			
			
			/* 
			 * Check if success
			 * 			
			 * 0 – Success, 
			 * 1 - GeneralError,
			 * 2 – NotEnoughMoney
			 */
			if (resp != null && resp.getStatus() == 0) {
				if (shouldStartAcceptingMore) {
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
	public boolean ejectInsertedCash(PosAccessor posAccessor, boolean shouldStartAcceptingMore) throws PeripheralException {
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
	public void endDeposit(PosAccessor posAccessor, int type) throws PeripheralException {
		Logger.info(this, "endDeposit");
		
		// Do nothing?
	}

	@Override
	public BigDecimal endReplenish(PosAccessor posAccessor) throws PeripheralException {
		Logger.info(this, "endReplenish");
		return null;
	}

	@Override
	public void exchange(PosAccessor posAccessor, CashExchangeData exchangeData) throws PeripheralException {
		Logger.info(this, "exchange");
		
	}

	@Override
	public void fixDeposit(PosAccessor posAccessor) throws PeripheralException {
		Logger.info(this, "fixDeposit");
		
	}

	@Override
	public List<CashChangerDenomination> getCashCounts(PosAccessor posAccessor) throws PeripheralException {
		Logger.info(this, "getCashCounts");
		return null;
	}

	@Override
	public CashbackActionType getCashbackActionType(PosAccessor posAccessor) {
		Logger.info(this, "getCashbackActionType");		
		return CashbackActionType.POST_TENDER;
	}

	@Override
	public int getDeviceStatus(PosAccessor posAccessor) throws PeripheralException {
		Logger.info(this, "getDeviceStatus");
		return 0;
	}

	@Override
	public BigDecimal getFixedAmount(PosAccessor posAccessor) {
		Logger.info(this, "getFixedAmount");
		return null;
	}

	@Override
	public int getFullStatus(PosAccessor posAccessor) throws PeripheralException {
		Logger.info(this, "getFullStatus");
		return 0;
	}

	@Override
	public BigDecimal getInsertedAmount(PosAccessor posAccessor) throws PeripheralException {
		Logger.info(this, "getInsertedAmount");
		return peekInsertedAmount(posAccessor);
	}

	@Override
	public BigDecimal getPeekAmount(PosAccessor posAccessor) {
		Logger.info(this, "getPeekAmount");
		return null;
	}

	@Override
	public void handleOverdispenseExeption(PosAccessor posAccessor, CashChangerOverdispenceException e, Transaction involvedTransaction,
			Session session) {
		Logger.info(this, "handleOverdispenseExeption");
		
		posAccessor.getUI().getCashierPrompter().showErrorMessage("CashChanger", "Cannot dispense change");		
	}

	@Override
	public void lockSafe(PosAccessor posAccessor) throws PeripheralException {
		Logger.info(this, "lockSafe");
		
	}

	@Override
	public void noteTransfer(PosAccessor posAccessor) throws PeripheralException {
		Logger.info(this, "noteTransfer");
		
	}

	@Override
	public void openCover(PosAccessor posAccessor) throws PeripheralException {
		Logger.info(this, "openCover");
		
	}

	@Override
	public Object operation(PosAccessor posAccessor, String operation) throws PeripheralException {
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
	public BigDecimal pickup(PosAccessor posAccessor) throws PeripheralException {
		Logger.info(this, "pickup");
		return null;
	}

	@Override
	public BigDecimal pickup(PosAccessor posAccessor, Collection<CashChangerDenomination> targetLevels) throws PeripheralException {
		Logger.info(this, "pickup");
		return null;
	}

	@Override
	public void releaseSafe(PosAccessor posAccessor) throws PeripheralException {
		Logger.info(this, "releaseSafe");
		
	}

	@Override
	public void secureCash(PosAccessor posAccessor, String secureLevel) throws PeripheralException {
		Logger.info(this, "secureCash");
		
	}

	@Override
	public void setFixedAmount(PosAccessor posAccessor, BigDecimal fixedAmount) {
		Logger.info(this, "setFixedAmount");
		
	}

	@Override
	public void setPeekAmount(PosAccessor posAccessor, BigDecimal peekAmount) {
		Logger.info(this, "setPeekAmount");
		
	}

	@Override
	public void weakInitialiseDevice(PosAccessor posAccessor) throws PeripheralException {
		Logger.info(this, "weakInitialiseDevice");
		
	}

	@Override
	public void afterOrderFinalized(AfterOrderFinalizedContext context, PosAccessor posAccessor) {
		try {
			AcceptMoneyStateResponse stopResp = api.stopAcceptMoney();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
