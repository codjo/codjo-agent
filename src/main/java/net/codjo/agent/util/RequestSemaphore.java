package net.codjo.agent.util;

public class RequestSemaphore {
    public static final long DEFAULT_TIMEOUT = 2000;
    private long timeout = DEFAULT_TIMEOUT;

    private final Object lock = new Object();
    private long count = 0;


    public void waitAcknowledge() {
        synchronized (lock) {
            if (decrementCounter()) {
                return;
            }
            try {
                lock.wait(timeout);
            }
            catch (InterruptedException e) {
                ;
            }
            decrementCounter();
        }
    }


    public void acknowledgeReceived() {
        synchronized (lock) {
            count++;
            lock.notifyAll();
        }
    }


    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }


    private boolean decrementCounter() {
        boolean shouldDecrement = count > 0;
        if (shouldDecrement) {
            count--;
        }
        return shouldDecrement;
    }
}
