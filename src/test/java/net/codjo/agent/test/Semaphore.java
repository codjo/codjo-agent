/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.test;
/**
 * Implantation d'un semaphore.
 */
public class Semaphore {
    static final long DEFAULT_TIMEOUT = 1000;
    private long timeout = DEFAULT_TIMEOUT;
    private final Object lock = new Object();
    private long count = 0;


    public int acquire(int tokenCount) {
        int nbAcquired = 0;
        for (int i = 0; i < tokenCount; i++) {
            if (acquire()) {
                nbAcquired++;
            }
        }
        return nbAcquired;
    }


    public boolean acquire() {
        synchronized (lock) {
            if (decrementCounter()) {
                return true; // acquired
            }
            try {
                lock.wait(timeout);
            }
            catch (InterruptedException e) {
                ;
            }
            return decrementCounter();
        }
    }


    public void release() {
        synchronized (lock) {
            count++;
            lock.notifyAll();
        }
    }


    public void releaseAll() {
        synchronized (lock) {
            count = Long.MAX_VALUE;
            lock.notifyAll();
        }
    }


    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }


    public long getTimeout() {
        return timeout;
    }


    private boolean decrementCounter() {
        boolean shouldDecrement = count > 0;
        if (shouldDecrement) {
            count--;
        }
        return shouldDecrement;
    }
}
