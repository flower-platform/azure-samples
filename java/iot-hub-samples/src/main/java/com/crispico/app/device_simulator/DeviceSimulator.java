package com.crispico.app.device_simulator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Random;

import com.google.gson.Gson;
import com.microsoft.azure.iothub.DeviceClient;
import com.microsoft.azure.iothub.IotHubClientProtocol;
import com.microsoft.azure.iothub.IotHubEventCallback;
import com.microsoft.azure.iothub.IotHubStatusCode;
import com.microsoft.azure.iothub.Message;

public class DeviceSimulator {

	private static String connString = "HostName=arduino-mkr1000.azure-devices.net;DeviceId=mkr1000;SharedAccessKey=H3pZf6Y3MtKqJ1u5zQarKQ==";
	
	private static IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
	
	private static class TelemetryDataPoint {
		public String deviceId;
		public double windSpeed;

		public String serialize() {
			Gson gson = new Gson();
			return gson.toJson(this);
		}
	}

	private static class EventCallback implements IotHubEventCallback {
		public void execute(IotHubStatusCode status, Object context) {
			System.out.println("IoT Hub responded to message with status " + status.name());

			if (context != null) {
				synchronized (context) {
					context.notify();
				}
			}
		}
	}

	private static class MessageSender implements Runnable {
		public volatile boolean stopThread = false;

		public void run() {
			try {
				double avgWindSpeed = 10; // m/s
				Random rand = new Random();
				DeviceClient client;
				client = new DeviceClient(connString, protocol);
				client.open();

				while (!stopThread) {
					double currentWindSpeed = avgWindSpeed + rand.nextDouble() * 4 - 2;
					TelemetryDataPoint telemetryDataPoint = new TelemetryDataPoint();
					telemetryDataPoint.deviceId = "myFirstDevice";
					telemetryDataPoint.windSpeed = currentWindSpeed;

					String msgStr = telemetryDataPoint.serialize();
					Message msg = new Message(msgStr);
					System.out.println(msgStr);

					Object lockobj = new Object();
					EventCallback callback = new EventCallback();
					client.sendEventAsync(msg, callback, lockobj);

					synchronized (lockobj) {
						lockobj.wait();
					}
					Thread.sleep(1000);
				}
				client.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws IOException, URISyntaxException {

		MessageSender ms0 = new MessageSender();
		Thread t0 = new Thread(ms0);
		t0.start();

		System.out.println("Press ENTER to exit.");
		System.in.read();
		ms0.stopThread = true;
	}
	
}
