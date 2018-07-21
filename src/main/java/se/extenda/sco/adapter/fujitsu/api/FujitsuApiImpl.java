package se.extenda.sco.adapter.fujitsu.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;

import se.extenda.sco.adapter.fujitsu.FujitsuConfiguration;
import se.extenda.sco.adapter.fujitsu.FujitsuSelfCheckoutResponse;
import se.extenda.sco.adapter.fujitsu.MessageId;
import se.extenda.sco.adapter.fujitsu.RestUtil;
import se.extenda.sco.adapter.fujitsu.UnexpectedWeightChangeResponse;
import se.extenda.sco.adapter.fujitsu.response.AcceptMoneyResponse;
import se.extenda.sco.adapter.fujitsu.response.AcceptMoneyStateResponse;
import se.extenda.sco.adapter.fujitsu.response.CashUnit;
import se.extenda.sco.adapter.fujitsu.response.Device;
import se.extenda.sco.adapter.fujitsu.response.DeviceCashUnitResponse;
import se.extenda.sco.adapter.fujitsu.response.DeviceTestAllResponse;
import se.extenda.sco.adapter.fujitsu.response.DispenseMoneyResponse;
import se.extenda.sco.adapter.fujitsu.response.ErrorResponseCode;
import se.extenda.sco.adapter.fujitsu.response.GeneralFujitsuResponse;
import se.extenda.sco.adapter.fujitsu.response.GetListResponse;
import se.extenda.sco.adapter.fujitsu.response.InterruptOperationResponseCode;
import se.extenda.sco.adapter.fujitsu.response.InterruptResponse;
import se.extenda.sco.adapter.fujitsu.response.IsZeroResponse;
import se.extenda.sco.adapter.fujitsu.response.ReadWeightResponse;
import se.extenda.sco.adapter.fujitsu.response.RollbackAcceptedMoneyResponse;
import se.extenda.sco.adapter.fujitsu.response.UnexpectedWeightResponse;

public class FujitsuApiImpl implements FujitsuApi {
    private enum FujitsuPath {
        MESSAGEID_STRING                   ("messageid"),
        OPTIONAL_QUANTITY                  ("{quantity}"),
        OPTIONAL_ITEMWEIGHT                ("{itemweight}"),
        NULL                               ("null");

        FujitsuPath(String value) {
            this.value = value;
        }

        private final String value;

        @Override
        public String toString() {
            return value;
        }
    }

    private static final Logger logger = Logger.getLogger(FujitsuApiImpl.class);

    private static final int WAIT_FOR_SCALE_TO_CLEAR_TIMEOUT = 3000;

    private RestTemplate restTemplate;

    private FujitsuConfiguration configuration;

    private String hostname;

    private int portnumber;

    private MessageId messageId = MessageId.getInstance();

    private static ScheduledExecutorService service = new ScheduledThreadPoolExecutor(1);
    private static ScheduledFuture<?> schedFuture = null;
    private volatile AtomicBoolean unexpectedWeightChangeMonitorEnabled = new AtomicBoolean(false);
    private volatile UnexpectedWeightResponse lastUnexpectedWeightChange;
    
    private List<Device> cashDevices;
    
	
	private void setCashUnitDevices(List<Device> devices) {
		for (Device device : devices) {
			if (device.getDeviceType().equals(Device.DEVICE_TYPE_COIN_RECYCLER)
				|| device.getDeviceType().equals(Device.DEVICE_TYPE_NOTE_RECYCLER)){
				this.cashDevices.add(device);
			}
		}
	}

    

    public FujitsuApiImpl() {
        try {
            configuration = FujitsuConfiguration.getInstance();
            restTemplate = RestUtil.createRestTemplate(configuration, messageId);
            hostname = configuration.getHostname();
            portnumber = configuration.getPortNumber();
 
    		try {
    			GetListResponse response = getDeviceList();
    			setCashUnitDevices(response.getDataDeviceResponses());
    		} catch (RestClientException e) {
    			logger.error("Non connection with ScoAAd, lets try in separate thread", e);
//    			waitForConnection();	
    		}
        } catch (IOException e) {
//            throw new SelfCheckoutException("Cannot load configuration file", e);
        }
    }
    
	@Override
	public void acceptMoney(long totalAmount) throws IOException {
		logger.info("acceptMoney()");
		
        final UriComponents request = RestUtil.getBaseUri(hostname, portnumber)
                .path("/api/CashDevice/AcceptMoney/{totalAmount}")
                .queryParam(String.valueOf(FujitsuPath.MESSAGEID_STRING), messageId.next())
                .buildAndExpand(String.valueOf(totalAmount));
                
        Thread t1 = new Thread(new Runnable() {
        	public void run() {
        		try {
                    restTemplate.getForObject(request.toUri(), AcceptMoneyResponse.class);        							
				} catch (Exception e) {
					// TODO: handle exception
				}
        	}
        });
        t1.start();
	}

	@Override
	public AcceptMoneyStateResponse stopAcceptMoney() throws IOException {
		logger.info("stopAcceptMoney()");
		
        final UriComponents request = RestUtil.getBaseUri(hostname, portnumber)
                .path("/api/CashDevice/StopAcceptMoney")
                .queryParam(String.valueOf(FujitsuPath.MESSAGEID_STRING), messageId.next())
                .build();
        return restTemplate.getForObject(request.toUri(), AcceptMoneyStateResponse.class);	
	}

	@Override
	public RollbackAcceptedMoneyResponse rollbackAcceptedMoney() throws IOException {
		logger.info("rollbackAcceptedMoney()");
		
		//TODO: dispenseAmount need to be converted to the smallest denominator, e.g. cents for EUR
		
        final UriComponents request = RestUtil.getBaseUri(hostname, portnumber)
                .path("/api/CashDevice/RollbackAcceptMoney")
                .queryParam(String.valueOf(FujitsuPath.MESSAGEID_STRING), messageId.next())	
                .build();
        return restTemplate.getForObject(request.toUri(), RollbackAcceptedMoneyResponse.class);
	}

	@Override
	public AcceptMoneyStateResponse getAcceptMoneyState() throws IOException {
		logger.info("getAcceptMoneyState()");
		
        final UriComponents request = RestUtil.getBaseUri(hostname, portnumber)
                .path("/api/CashDevice/GetAcceptMoneyState")
                .queryParam(String.valueOf(FujitsuPath.MESSAGEID_STRING), messageId.next())
                .build();
        return restTemplate.getForObject(request.toUri(), AcceptMoneyStateResponse.class);
		
	}

	@Override
	public DispenseMoneyResponse dispenseMoney(long dispenseAmount, String currency) throws IOException {
		logger.info("dispenseMoney()");
		
        final UriComponents request = RestUtil.getBaseUri(hostname, portnumber)
                .path("/api/CashDevice/DispenseMoney/{amount}/{currency}")
                .queryParam(String.valueOf(FujitsuPath.MESSAGEID_STRING), messageId.next())
                .buildAndExpand(String.valueOf(dispenseAmount),currency);
        return restTemplate.getForObject(request.toUri(), DispenseMoneyResponse.class);
	}
	
    


	@Override
	public List<CashUnit> getDeviceCashUnits() throws IOException {
		logger.info("getDeviceCashUnits()");

		List<CashUnit> cashUnits = new ArrayList<CashUnit>();
		
		// get all cash units from all cash devices
		for (Device device : this.cashDevices) {
	        final UriComponents request = RestUtil.getBaseUri(hostname, portnumber)
	                .path("/api/CashDevice/GetDeviceCashUnits/{deviceName}")
	                .queryParam(String.valueOf(FujitsuPath.MESSAGEID_STRING), messageId.next())
	                .buildAndExpand(device.getDeviceName());
	        DeviceCashUnitResponse resp = restTemplate.getForObject(request.toUri(),DeviceCashUnitResponse.class);
	        
	        if (resp.getStatus() == 0) {
	        	cashUnits.add(resp.getCashUnits());
	        }
		}
		
        return cashUnits;
	}

	@Override
	public void getDeviceState() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDeviceCashUnits() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void emptyDeviceCashUnits() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void emptyAllDeviceContent() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void refillDeviceCashUnits() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getNoteSignature() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void scanMedia() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void withdrawCashbox() throws IOException {
		// TODO Auto-generated method stub
		
	}

    
    
    
    
    
    
    
    
    
    

    @Override
    public DeviceTestAllResponse adminDeviceTestAll() throws IOException {
        final UriComponents request = RestUtil.getBaseUri(hostname, portnumber)
                .path("/api/admin/Device/TestAll")
                .queryParam(String.valueOf(FujitsuPath.MESSAGEID_STRING), messageId.next()).build();
        return restTemplate.getForObject(request.toUri(), DeviceTestAllResponse.class);
    }

    @Override
    public GetListResponse getDeviceList() throws IOException {
        final UriComponents request = RestUtil.getBaseUri(hostname, portnumber)
                .path("/api/admin/Device/GetList")
                .queryParam(String.valueOf(FujitsuPath.MESSAGEID_STRING), messageId.next()).build();
        return restTemplate.getForObject(request.toUri(), GetListResponse.class);
    }

    @Override
    public void startTransaction() throws IOException {
        final UriComponents request =  RestUtil.getBaseUri(hostname, portnumber).path("/api/SecureScale/StartTransaction")
                .build();
        GeneralFujitsuResponse response = restTemplate.getForObject(request.toUriString(), GeneralFujitsuResponse.class);
        RestUtil.handlePossibleError(response);
    }

//    @Override
//    public ClearWeightResponse clearWeights(Barcode barcode) throws IOException {
//        final UriComponents request = RestUtil.getBaseUri(hostname, portnumber)
//                .path("/api/SecureScale/ClearWeights/{itemCode}/{weight}/{quantity}")
//                .queryParam(String.valueOf(FujitsuPath.MESSAGEID_STRING), messageId.next())
//                .buildAndExpand(barcode.getData(), "{weight}", FujitsuPath.OPTIONAL_QUANTITY);
//        ClearWeightResponse response = restTemplate.getForObject(request.toUri(), ClearWeightResponse.class);
//        logger.info("Clear weights");
//        return response;
//    }

    @Override
    public void setScaleSoftZero() throws IOException {
        final UriComponents request = RestUtil.getBaseUri(hostname, portnumber)
                .path("/api/SecureScale/SoftZero")
                .queryParam(String.valueOf(FujitsuPath.MESSAGEID_STRING), messageId.next()).build();
        GeneralFujitsuResponse softZeroResponse = restTemplate.getForObject(request.toUri(),
                GeneralFujitsuResponse.class);
        logger.info("Set scale soft zero");
        RestUtil.handlePossibleError(softZeroResponse);
    }

    @Override
    public void confirmBagScaleWeightOffset() throws IOException {
        final UriComponents request = RestUtil.getBaseUri(hostname, portnumber)
                .path("/api/SecureScale/ConfirmBagScaleWeightOffset")
                .queryParam(String.valueOf(FujitsuPath.MESSAGEID_STRING), messageId.next())
                .build();
        GeneralFujitsuResponse response = restTemplate.getForObject(request.toUri(), GeneralFujitsuResponse.class);
        logger.info("Confirm bag scale weight offset");
        RestUtil.handlePossibleError(response);
    }

    @Override
    public void clearTransaction() throws IOException {
        final UriComponents request = RestUtil.getBaseUri(hostname, portnumber)
                .path("/api/SecureScale/ClearTransaction")
                .queryParam(String.valueOf(FujitsuPath.MESSAGEID_STRING), messageId.next()).build();
        GeneralFujitsuResponse clearTransactionResponse = restTemplate.getForObject(request.toUri(),
                GeneralFujitsuResponse.class);
        logger.info("Transaction cleared");
        RestUtil.handlePossibleError(clearTransactionResponse);
    }

    @Override
    public void finishTransaction() throws IOException {
        final UriComponents request = RestUtil.getBaseUri(hostname, portnumber)
                .path("/api/SecureScale/FinishTransaction")
                .queryParam(String.valueOf(FujitsuPath.MESSAGEID_STRING), messageId.next()).build();
        GeneralFujitsuResponse finishTransactionResponse = restTemplate.getForObject(request.toUri(),
                GeneralFujitsuResponse.class);
        RestUtil.handlePossibleError(finishTransactionResponse);
        logger.info("Transaction finished");
    }

    @Override
    public boolean isUnexpectedWeightChangeListenerActive() {
        return unexpectedWeightChangeMonitorEnabled.get();
    }

    @Override
    public synchronized void stopUnexpectedWeightChangeListener() {
        logger.info("Shutting down unexpected weight change listener");
        if (schedFuture != null) {
            schedFuture.cancel(true);
        }
        service.shutdownNow();
        unexpectedWeightChangeMonitorEnabled.set(false);
        lastUnexpectedWeightChange = null;
    }

//    public synchronized void startUnexpectedWeightChangeListener() throws IOException {
//        if (!isUnexpectedWeightChangeListenerActive()) {
//            if (schedFuture == null || schedFuture.isDone() || schedFuture.isCancelled()) {
//                service = new ScheduledThreadPoolExecutor(1);
//                logger.info("Starting unexpected weight change listener");
//                try {
//                    schedFuture = service.scheduleWithFixedDelay(this::pollUnexpectedWeightChange, 0,
//                    		configuration.getUnexpectedWeightChangeDelayInMillis(), TimeUnit.MILLISECONDS);
//                    unexpectedWeightChangeMonitorEnabled.set(true);
//                } catch (RejectedExecutionException e) {
//                    logger.error("Failed to enqueue worker thread for unexpected weight change listener");
//                    unexpectedWeightChangeMonitorEnabled.set(false);
//                    throw new IOException(e);
//                }
//            }
//        } else {
//            logger.warn("Unexpected weight change listener already started, ignoring...");
//        }
//    }

	/**
	 * Performs the actual work of polling the unexpected weight change method
	 * of the Fujitsu API when ordered to by the polling thread.
	 */
//    private synchronized void pollUnexpectedWeightChange() {
//    	try {
//    		UnexpectedWeightResponse unexpectedWeightChangeResponse = unexpectedWeightChange();
//    		if(responseContainsErrorOverload(unexpectedWeightChangeResponse)) {
//    			checkAndNotifyOverload(ErrorResponseCode.PLATE_IS_OVERLOADED.getStatus());
//    		} else {
//    			handleUnexpectedWeightChange(unexpectedWeightChangeResponse);
//    		}
//    	} catch (IOException e) {
//    			fujitsuApiListener.handleErrorDuringUnexpectedWeightChange(e);
//    	} catch(Exception e) {
//    		logger.error("Unexpected error", e);
//    	}
//    }

	/**
	 * Checks if we have received an unexpected weight change. If there is an (non duplicate)
	 * unexpected weight change, we notify the client. If no unexpected weight
	 * change has been received, we clear the unexpected weight change
	 * indicator, and notify the client that there is not unexpected weight
	 * change in progress.
	 *
	 * @param unexpectedWeightChangeResponse
	 *            the response to check
	 * @throws IOException
	 */
	private void handleUnexpectedWeightChange(UnexpectedWeightResponse unexpectedWeightChangeResponse)
			throws IOException {
		RestUtil.handlePossibleError(unexpectedWeightChangeResponse);

		if (hasWeightChanged(unexpectedWeightChangeResponse)) {
			if (lastUnexpectedWeightChange == null || !lastUnexpectedWeightChange.equals(unexpectedWeightChangeResponse)) {
				setUnexpectedWeightChangeInProgress(unexpectedWeightChangeResponse);
			} else {
				logger.info("Unexpected weight change of " + unexpectedWeightChangeResponse.getStatus() + " type is already being handled, ignoring...");
			}
		} else {
			clearUnexpectedWeightChangeInProgress();
		}
	}

	/**
	 * Determines if a UnexpectedWeightResponse contains the error code for an
	 * overloaded scale.
	 *
	 * @param response
	 *            the response to check
	 * @return true, if there is a scale overload error in the response, false
	 *         otherwise
	 */
    private boolean responseContainsErrorOverload(UnexpectedWeightResponse response) {
    	return response.errorsContainsCode(ErrorResponseCode.PLATE_IS_OVERLOADED.getStatus());
    }

    /**
     * Notifies the SCO that an unexpected weight change has occurred, and sets
     * the cached unexpected weight change kept in the adapter.
     *
     * @param unexpectedWeightChangeResponse
     *            the unexpected weight change response to set in adapter
     */
    private void setUnexpectedWeightChangeInProgress(UnexpectedWeightResponse unexpectedWeightChangeResponse) {
        logger.info("Detected unexpected weight change of type: " + unexpectedWeightChangeResponse.getStatus());
        lastUnexpectedWeightChange = unexpectedWeightChangeResponse;
//        fujitsuApiListener.handleUnexpectedWeightChange(UnexpectedWeightChangeResponse.fromLong(unexpectedWeightChangeResponse.getStatus()));
    }

    /**
     * If an unexpected weight change has been handled, the SCO is notified, and
     * the state of the cached unexpected weight change kept in the adapter, is
     * cleared.
     */
    private void clearUnexpectedWeightChangeInProgress() {
        if (lastUnexpectedWeightChange != null) {
            logger.info("Unexpected weight change has been handled");
//            fujitsuApiListener.unexpectedWeightChangeHandled(
//                    UnexpectedWeightChangeResponse.fromLong(lastUnexpectedWeightChange.getStatus()));
            lastUnexpectedWeightChange = null;
        } else {
//            fujitsuApiListener.handleNoUnexpectedWeightChange();
        }
    }

    /**
     * Checks is a supplied response from an unexpected weight change call is
     * successful or not.
     *
     * @param unexpectedWeightChangeResponse
     * @return true, if successful, false otherwise
     */
    private boolean hasWeightChanged(UnexpectedWeightResponse unexpectedWeightChangeResponse) throws IOException {
        if (unexpectedWeightChangeResponse != null) {
            return UnexpectedWeightChangeResponse
                    .fromLong(unexpectedWeightChangeResponse.getStatus()) != UnexpectedWeightChangeResponse.SUCCESS;
        } else {
            throw new IOException("Response is null");
        }
    }

//    @Override
//    public FujitsuSelfCheckoutResponse checkLightItem(final Barcode barcode) throws IOException {
//        final UriComponents request = RestUtil.getBaseUri(hostname, portnumber)
//                .path("/api/SecureScale/LightItemCheck/{itemcode}")
//                .queryParam(String.valueOf(FujitsuPath.MESSAGEID_STRING), messageId.next())
//                .buildAndExpand(barcode.getData());
//        return checkItemInternal(request, barcode);
//    }

    /**
     * Call to Fujitsu API for checking if there has been an unexpected weight
     * change, i.e. and abuse or error. This method is used by the polling
     * mechanism which wants to handle errors separately.
     *
     */
    private synchronized UnexpectedWeightResponse unexpectedWeightChange() {
        final UriComponents request = RestUtil.getBaseUri(hostname, portnumber)
                .path("/api/SecureScale/UnexpectedWeightChange")
                .queryParam(String.valueOf(FujitsuPath.MESSAGEID_STRING), messageId.next()).build();
        return restTemplate.getForObject(request.toUri(), UnexpectedWeightResponse.class);
    }

//    @Override
//    public FujitsuSelfCheckoutResponse checkNormalItem(final Barcode barcode, final long timeout) throws IOException {
//        final UriComponents request = RestUtil.getBaseUri(hostname, portnumber)
//                .path("/api/SecureScale/CheckItem/{itemCode}/{timeout}/{quantity}/{itemweight}")	//NOSONAR
//                .queryParam(String.valueOf(FujitsuPath.MESSAGEID_STRING), messageId.next())
//                .buildAndExpand(barcode.getData(), timeout, FujitsuPath.OPTIONAL_QUANTITY.toString(),
//                        FujitsuPath.OPTIONAL_ITEMWEIGHT.toString());
//        return checkItemInternal(request, barcode);
//    }

//    @Override
//    public FujitsuSelfCheckoutResponse checkWeightItem(final Barcode barcode, final String itemWeight, final long timeout)
//            throws IOException {
//        final UriComponents request = RestUtil.getBaseUri(hostname, portnumber)
//                .path("/api/SecureScale/CheckItem/{itemCode}/{timeout}/{quantity}/{itemweight}") //NOSONAR
//                .queryParam(String.valueOf(FujitsuPath.MESSAGEID_STRING), messageId.next())
//                .buildAndExpand(barcode.getData(), timeout, FujitsuPath.OPTIONAL_QUANTITY.toString(), itemWeight);
//        return checkItemInternal(request, barcode);
//    }
//
//    @Override
//    public FujitsuSelfCheckoutResponse checkQuantityItem(final Barcode barcode, final int quantity, final long timeout)
//            throws IOException {
//        final UriComponents request = RestUtil.getBaseUri(hostname, portnumber)
//                .path("/api/SecureScale/CheckItem/{itemCode}/{timeout}/{quantity}/{itemweight}") //NOSONAR
//                .queryParam(String.valueOf(FujitsuPath.MESSAGEID_STRING), messageId.next())
//                .buildAndExpand(barcode.getData(), timeout, quantity, FujitsuPath.OPTIONAL_ITEMWEIGHT.toString());
//        return checkItemInternal(request, barcode);
//    }

    /**
     * Private method responsible for making requests of previously built URI:s,
     * checking a weight, quantity or a normal item and handling the response from the fujitsu sco device
     * @param checkItemRequest
     * @throws IOException
     */
//    private FujitsuSelfCheckoutResponse checkItemInternal(final UriComponents checkItemRequest, final Barcode barcode) throws IOException {
//        CheckItemResponse checkItemResponse = restTemplate.getForObject(checkItemRequest.toUri(), CheckItemResponse.class);
//        checkAndNotifyOverload(checkItemResponse.getOverloadStatus(), barcode);
//        RestUtil.handlePossibleError(checkItemResponse); 
//        
//        FujitsuSelfCheckoutResponse response = FujitsuSelfCheckoutResponse.fromLong(checkItemResponse.getStatus());
//        switch (response) {
//            case SUCCESS:
//                fujitsuApiListener.handleValidateOk(barcode);
//                break;
//            case FINISHED_BY_TIMEOUT:
//            	fujitsuApiListener.handleValidateTimeout(barcode);	
//                break;
//            case ITEM_WEIGHT_NOT_FOUND:
//				fujitsuApiListener.handleItemWeightNotFound(barcode, checkItemResponse.getDeltaWeight());
//				break;
//			case ITEM_NOT_FOUND:
//				fujitsuApiListener.handleItemNotFound(barcode, checkItemResponse.getDeltaWeight());
//				break;
//            case OPERATION_INTERRUPTED:
//                fujitsuApiListener.handleOperationInterrupted();
//                break;
//            default:
//        }
//        return response;
//    }

    /**
     * Check if the latest response from CheckItem has returned an overload of
     * the scale. If the scale is overloaded, then notify listeners.
     * <p>
     * <b>NOTE:</b> Delegates to {@link FujitsuApiImpl#checkAndNotifyOverload(long, Barcode)}
     * </p>
     * @param responseCode
     *            the last received response
     */
//    private void checkAndNotifyOverload(long responseCode) {
//    	checkAndNotifyOverload(responseCode, null);
//    }
    
	/**
	 * Check if the latest response from Fujitsu API has returned an overload of
	 * the scale. If the scale is overloaded, then notify listeners.
	 * 
	 * @param responseCode
	 *            the last received response
	 * @param barcode
	 *            the barcode of the current item being handled. Null is
	 *            allowed.
	 */
//    private void checkAndNotifyOverload(long responseCode, Barcode barcode) {
//    	if(isStatusCodeOverload(responseCode) || isErrorCodeOverload(responseCode)) {
//    		fujitsuApiListener.notifyScaleOverloaded(barcode);	
//    	} 	
//    }

	/**
	 * Checks is the response code provided corresponds to an overload of the
	 * scale. Applicable for CheckItem, ReadWeight etc, where we get a specific
	 * parameter to indicate overload.
	 * 
	 * @param responseCode
	 *            the response code to check
	 * @return true, if there is an overload, false otherwise
	 */
    private boolean isErrorCodeOverload(long responseCode) {
    	try {
    		return ErrorResponseCode.fromLong(responseCode) == ErrorResponseCode.PLATE_IS_OVERLOADED;	
    	} catch (EnumConstantNotPresentException e) {
    		logger.debug("No match for error response code: " + responseCode);
    		return false;
    	}
    }
    
	/**
	 * Checks is the response code provided corresponds to an overload of the
	 * scale. Applicable for UnexpectedWeightChange. where we get only an
	 * error to indicate overload.
	 * 
	 * @param responseCode
	 *            the response code to check
	 * @return true, if there is an overload, false otherwise
	 */
//    private boolean isStatusCodeOverload(long responseCode) { 
//    	try {
//    		return OverloadStatusResponseCode.fromLong(responseCode) == OverloadStatusResponseCode.OVERLOADED_WEIGHT;	
//    	}catch (EnumConstantNotPresentException e) {
//    		logger.debug("No match for status response code: " + responseCode);
//    		return false;
//    	}
//    }
//    
//    @Override
//    public FujitsuSelfCheckoutResponse removeLastItem(long timeout, Barcode barcode) {
//    	final UriComponents request = RestUtil.getBaseUri(hostname, portnumber)
//                .path("/api/SecureScale/RemoveLastItem/{timeout}")
//                .queryParam(String.valueOf(FujitsuPath.MESSAGEID_STRING), messageId.next()).buildAndExpand(timeout);
//
//        RemoveLastItemResponse removeLastItemResponse = restTemplate.getForObject(request.toUri(),
//                RemoveLastItemResponse.class);
//
//        if (removeLastItemResponse == null) {
//            logger.warn("No response from fujitsu SCO");
//            return FujitsuSelfCheckoutResponse.GENERAL_ERROR;
//        }
//
//        if (removeLastItemResponse.getErrorResponses() != null) {
//            notifyInCaseOfOverload(removeLastItemResponse, barcode);
//            fujitsuApiListener.handleErrorDuringItemRemoval(Arrays.asList(removeLastItemResponse.getErrorResponses()));
//        }
//        
//        final FujitsuSelfCheckoutResponse fujitsuSelfCheckoutResponse = FujitsuSelfCheckoutResponse.fromLong(removeLastItemResponse.getStatus());
//        if(fujitsuSelfCheckoutResponse.equals(FujitsuSelfCheckoutResponse.OPERATION_INTERRUPTED)) {
//            fujitsuApiListener.handleOperationInterrupted();
//        }
//        return fujitsuSelfCheckoutResponse;
//    }
//
//    @Override
//    public FujitsuSelfCheckoutResponse returnLastItem(long timeout, Barcode barcode) {
//        final UriComponents request = RestUtil.getBaseUri(hostname, portnumber)
//                .path("/api/SecureScale/ReturnLastItem/{timeout}")
//                .queryParam(String.valueOf(FujitsuPath.MESSAGEID_STRING), messageId.next())
//                .buildAndExpand(timeout);
//        ReturnLastItemResponse returnLastItemResponse = restTemplate.getForObject(request.toUri(), ReturnLastItemResponse.class);
//
//        if (returnLastItemResponse == null) {
//            logger.warn("No response from fujitsu SCO");
//            return FujitsuSelfCheckoutResponse.GENERAL_ERROR;
//        }
//
//        if (returnLastItemResponse.getErrorResponses() != null) {
//            // Check for overload
//            notifyInCaseOfOverload(returnLastItemResponse, barcode);
//            fujitsuApiListener.handleErrorDuringItemReturn(Arrays.asList(returnLastItemResponse.getErrorResponses()));
//        }
//
//        final FujitsuSelfCheckoutResponse status = FujitsuSelfCheckoutResponse.fromLong(returnLastItemResponse.getStatus());
//        if(status.equals(FujitsuSelfCheckoutResponse.OPERATION_INTERRUPTED)) {
//            fujitsuApiListener.handleOperationInterrupted();
//        }
//        return status;
//    }
//
//    private void notifyInCaseOfOverload(GeneralFujitsuResponse response, Barcode barcode) {
//        Arrays.stream(response.getErrorResponses())
//                .filter(it -> it.getErrorCode() == ErrorResponseCode.PLATE_IS_OVERLOADED.getStatus())
//                .findFirst()
//                .ifPresent( it -> checkAndNotifyOverload(it.getErrorCode(), barcode));
//    }

    @Override
    public FujitsuSelfCheckoutResponse isZero() throws IOException {
        final UriComponents request = RestUtil.getBaseUri(hostname, portnumber)
                .path("/api/SecureScale/IsZero/{timeout}")
                .queryParam(String.valueOf(FujitsuPath.MESSAGEID_STRING), messageId.next())
                .buildAndExpand(WAIT_FOR_SCALE_TO_CLEAR_TIMEOUT);

        IsZeroResponse isZeroTransactionResponse = restTemplate.getForObject(request.toUriString(),
                IsZeroResponse.class);
        RestUtil.handlePossibleError(isZeroTransactionResponse);
        return FujitsuSelfCheckoutResponse.fromLong(isZeroTransactionResponse.getStatus());
    }

    @Override
    public void clearLastUnexpectedWeightChange() {
        lastUnexpectedWeightChange = null;
    }

//    @Override
//    public ListItemWeightsResponse listItemWeights(Barcode barcode) throws IOException {
//        final UriComponents request = RestUtil.getBaseUri(hostname, portnumber)
//                .path("/api/SecureScale/ListItemWeights/{itemCode}")
//                .queryParam(String.valueOf(FujitsuPath.MESSAGEID_STRING), messageId.next())
//                .buildAndExpand(barcode.getData());
//        
//        return restTemplate.getForObject(request.toUriString(), ListItemWeightsResponse.class);
//    }
	
	@Override
	public ReadWeightResponse readWeight() throws IOException {
		final UriComponents request =  RestUtil.getBaseUri(hostname, portnumber).path("/api/SecureScale/ReadWeight")
				.build();
		ReadWeightResponse readWeightResponse = restTemplate.getForObject(request.toUriString(), ReadWeightResponse.class);
		RestUtil.handlePossibleError(readWeightResponse);
		
		return readWeightResponse;
	}
	
//	@Override
//	public RegisterWeightResponseCode storeWeight(Barcode barcode, int weight, int quantity) throws IOException {
//		final UriComponents request =  RestUtil.getBaseUri(hostname, portnumber).path("/api/SecureScale/StoreWeight/{itemcode}/{weight}/{quantity}")
//				.buildAndExpand(barcode.getData(), weight, quantity);
//		StoreWeightResponse storeWeightResponse = restTemplate.getForObject(request.toUriString(), StoreWeightResponse.class);
//		RestUtil.handlePossibleError(storeWeightResponse);
//		return RegisterWeightResponseCode.fromLong(storeWeightResponse.getStatus());
//	}
//
//	@Override
//	public RegisterWeightResponseCode setWeight(Item item, boolean isLightItem, int weight, int quantity) throws IOException{
//		final UriComponents request =  RestUtil.getBaseUri(hostname, portnumber).path("/api/SecureScale/SetWeight/{itemcode}/{isLightItem}/{weight}/{tolerance}/{quantity}")
//				.buildAndExpand(item.getBarcode().getData(), String.valueOf(isLightItem), weight, String.valueOf(FujitsuPath.NULL), quantity);
//		SetWeightResponse setWeightResponse = restTemplate.getForObject(request.toUriString(), SetWeightResponse.class);
//		RestUtil.handlePossibleError(setWeightResponse);
//		return RegisterWeightResponseCode.fromLong(setWeightResponse.getStatus());
//	}

//    @Override
//    public FujitsuSelfCheckoutResponse updateTolerance(Barcode barcode, final Integer tolerance) throws IOException {
//        UriComponents request;
//        if(tolerance == null) {
//            request = RestUtil.getBaseUri(hostname, portnumber)
//                    .path("/api/SecureScale/UpdateTolerance/{itemCode}/{tolerance}")
//                    .queryParam(String.valueOf(FujitsuPath.MESSAGEID_STRING), messageId.next())
//                    .buildAndExpand(barcode.getData(), FujitsuPath.NULL.toString() );
//        } else {
//            request = RestUtil.getBaseUri(hostname, portnumber)
//                    .path("/api/SecureScale/UpdateTolerance/{itemCode}/{tolerance}")
//                    .queryParam(String.valueOf(FujitsuPath.MESSAGEID_STRING), messageId.next())
//                    .buildAndExpand(barcode.getData(), tolerance);
//        }
//        UpdateToleranceResponse response = restTemplate.getForObject(request.toString(),
//                UpdateToleranceResponse.class);
//        return FujitsuSelfCheckoutResponse.fromLong(response.getStatus());
//    }

    @Override
    public InterruptOperationResponseCode interruptOperation() throws IOException {
        UriComponents request = RestUtil.getBaseUri(hostname, portnumber)
                .path("/api/SecureScale/InterruptOperation")
                .queryParam(String.valueOf(FujitsuPath.MESSAGEID_STRING), messageId.next())
                .build();
        InterruptResponse response = restTemplate.getForObject(request.toString(),
                InterruptResponse.class);
        return InterruptOperationResponseCode.fromLong(response.getStatus());
    }

	@Override
	public void startUnexpectedWeightChangeListener() throws IOException {
		// TODO Auto-generated method stub
		
	}

}
