package se.extenda.cashchanger.adapter.fujitsu.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Representation of Device in api/admin/Device/GetList
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Device {
	
	/** Enumeration of different device types, kept as constants for now */
	public static final String DEVICE_TYPE_LIGHT = "Light";
	public static final String DEVICE_TYPE_SECURE_SCALES = "SecureScales";
	public static final String DEVICE_TYPE_COIN_RECYCLER = "CoinRecycler";
	public static final String DEVICE_TYPE_NOTE_RECYCLER = "NoteRecycler";

	private String id;

	private String deviceName;

	private String deviceType;

	private String assemblyName;

	private String className;

	/**
	 * 
	 * @return device Id
	 */
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	/**
	 * 
	 * @return device name
	 */
	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	
	/**
	 * Device type
	 * @return device type
	 */
	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	/**
	 * @return assembly name
	 */
	public String getAssemblyName() {
		return assemblyName;
	}

	public void setAssemblyName(String assemblyName) {
		this.assemblyName = assemblyName;
	}

	/**
	 * @return class name
	 */
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	
}