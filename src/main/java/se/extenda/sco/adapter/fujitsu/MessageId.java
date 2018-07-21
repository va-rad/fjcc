package se.extenda.sco.adapter.fujitsu;

import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Generates unique message ID used in requests. The message ID consists of a
 * random UUID and a sequence number.
 *
 */
public class MessageId {

	private int sequenceNumber = 0;

	private static Lock lock = new ReentrantLock();

	private String currentMessageId;
	
	private static MessageId instance;

	public static MessageId getInstance() {
		if (instance == null) {
			instance = new MessageId();
		}
		return instance;
	}
	
	private MessageId() {
		// protected constructor.
	}
	
	/**
	 * @return a unique message id per request
	 */
	public String next() {
		lock.lock();
		try {
			sequenceNumber++;
			if (sequenceNumber == Integer.MAX_VALUE) {
				sequenceNumber = 0;
			}
		
			currentMessageId = UUID.randomUUID().toString() + "-" + sequenceNumber;
			return currentMessageId;
		} finally {
			lock.unlock();
		}
	}

	public String current() {
		return currentMessageId;
	}

}
