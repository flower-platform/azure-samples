package com.crispico.app.read_messages;

import java.io.IOException;

import com.microsoft.eventhubs.client.ConnectionStringBuilder;
import com.microsoft.eventhubs.client.Constants;
import com.microsoft.eventhubs.client.EventHubClient;
import com.microsoft.eventhubs.client.EventHubEnqueueTimeFilter;
import com.microsoft.eventhubs.client.EventHubException;
import com.microsoft.eventhubs.client.EventHubMessage;
import com.microsoft.eventhubs.client.EventHubReceiver;

public class ReadMessages {

	private static EventHubClient client;
	private static long now = System.currentTimeMillis();


	public static void main(String[] args) throws IOException {
		String policyName = "iothubowner";
		String policyKey = "atQwsAzJOGBo6A1tV2ZN55BizfzPVRv2ZIc1XmSN3Lo=";
		String namespace = "ihsuprodamres007dednamespace";
		String name = "iothub-ehub-arduino-mk-21909-b272367e46";
		try {
		  ConnectionStringBuilder csb = new ConnectionStringBuilder(policyName, policyKey, namespace);
		  client = EventHubClient.create(csb.getConnectionString(), name);
		}
		catch(EventHubException e) {
		    System.out.println("Exception: " + e.getMessage());
		}

		MessageReceiver mr0 = new MessageReceiver("0");
		MessageReceiver mr1 = new MessageReceiver("1");
		Thread t0 = new Thread(mr0);
		Thread t1 = new Thread(mr1);
		t0.start(); t1.start();

		System.out.println("Press ENTER to exit.");
		System.in.read();
		mr0.stopThread = true;
		mr1.stopThread = true;
		client.close();
	}

	private static class MessageReceiver implements Runnable {
		
		public volatile boolean stopThread = false;
		
		private String partitionId;

		public MessageReceiver(String partitionId) {
			this.partitionId = partitionId;
		}

		public void run() {
			try {
				EventHubReceiver receiver = client.getConsumerGroup(null).createReceiver(partitionId, new EventHubEnqueueTimeFilter(now), Constants.DefaultAmqpCredits);
				System.out.println("** Created receiver on partition " + partitionId);
				while (!stopThread) {
					EventHubMessage message = EventHubMessage.parseAmqpMessage(receiver.receive(5000));
					if (message != null) {
						System.out.println("Received: (" + message.getOffset() + " | " + message.getSequence() + " | "
								+ message.getEnqueuedTimestamp() + ") => " + message.getDataAsString());
					}
				}
				receiver.close();
			} catch (EventHubException e) {
				System.out.println("Exception: " + e.getMessage());
			}
		}
	}

	
}

