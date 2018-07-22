package se.extenda.cashchanger.adapter.fujitsu.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;

import se.extenda.cashchanger.adapter.fujitsu.FujitsuConfiguration;
import se.extenda.cashchanger.adapter.fujitsu.MessageId;
import se.extenda.cashchanger.adapter.fujitsu.RestUtil;
import se.extenda.cashchanger.adapter.fujitsu.response.AcceptMoneyResponse;
import se.extenda.cashchanger.adapter.fujitsu.response.AcceptMoneyStateResponse;
import se.extenda.cashchanger.adapter.fujitsu.response.CashUnit;
import se.extenda.cashchanger.adapter.fujitsu.response.Device;
import se.extenda.cashchanger.adapter.fujitsu.response.DeviceCashUnitResponse;
import se.extenda.cashchanger.adapter.fujitsu.response.DeviceTestAllResponse;
import se.extenda.cashchanger.adapter.fujitsu.response.DispenseMoneyResponse;
import se.extenda.cashchanger.adapter.fujitsu.response.GetListResponse;
import se.extenda.cashchanger.adapter.fujitsu.response.RollbackAcceptedMoneyResponse;

public class FujitsuApiImpl implements FujitsuApi {
    private enum FujitsuPath {
        MESSAGEID_STRING                   ("messageid"),
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

    private RestTemplate restTemplate;
    private FujitsuConfiguration configuration;
    private String hostname;
    private int portnumber;
    private MessageId messageId = MessageId.getInstance();
    
    private List<Device> cashDevices = new ArrayList<Device>();
	
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
					// Don't do anything about this.
					// AcceptMoney can respond quickly or not at all.
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
	        	cashUnits.addAll(resp.getCashUnits());
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
    public GetListResponse getDeviceList() throws IOException {
        final UriComponents request = RestUtil.getBaseUri(hostname, portnumber)
                .path("/api/admin/Device/GetList")
                .queryParam(String.valueOf(FujitsuPath.MESSAGEID_STRING), messageId.next()).build();
        return restTemplate.getForObject(request.toUri(), GetListResponse.class);
    }

    @Override
    public DeviceTestAllResponse adminDeviceTestAll() throws IOException {
        final UriComponents request = RestUtil.getBaseUri(hostname, portnumber)
                .path("/api/admin/Device/TestAll")
                .queryParam(String.valueOf(FujitsuPath.MESSAGEID_STRING), messageId.next()).build();
        return restTemplate.getForObject(request.toUri(), DeviceTestAllResponse.class);
    }
}
