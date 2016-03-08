package com.crispico.app.create_device_identity;

import java.io.IOException;
import java.net.URISyntaxException;

import com.microsoft.azure.iot.service.exceptions.IotHubException;
import com.microsoft.azure.iot.service.sdk.Device;
import com.microsoft.azure.iot.service.sdk.RegistryManager;

public class CreateDeviceIdentity {

	private static final String connectionString = "HostName=arduino-mkr1000.azure-devices.net;SharedAccessKeyName=iothubowner;SharedAccessKey=atQwsAzJOGBo6A1tV2ZN55BizfzPVRv2ZIc1XmSN3Lo=";

	private static final String deviceId = "mkr1000";
	
	public static void main(String[] args) throws IOException, URISyntaxException, Exception {
		RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

		Device device = Device.createFromId(deviceId, null, null);
		try {
			device = registryManager.addDevice(device);
		} catch (IotHubException iote) {
			try {
				device = registryManager.getDevice(deviceId);
			} catch (IotHubException iotf) {
				iotf.printStackTrace();
			}
		}
		System.out.println("Device id: " + device.getDeviceId());
		System.out.println("Device key: " + device.getPrimaryKey());
	}
	
}
