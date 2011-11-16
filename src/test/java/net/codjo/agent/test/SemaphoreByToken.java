/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.test;
/**
 * Implantation d'un semaphore base sur des step.
 */
public class SemaphoreByToken {
    private long timeout = Semaphore.DEFAULT_TIMEOUT;
    private final Object lock = new Object();
    private Token currentToken = new Token("n/a");


    public void release(Token token) {
        synchronized (lock) {
            currentToken = token;
            lock.notifyAll();
        }
    }


    public void waitFor(Token token) {
        synchronized (lock) {
            long start = System.currentTimeMillis();

            while (currentToken != token) {
                try {
                    lock.wait(timeout);
                }
                catch (InterruptedException e) {
                    throw new IllegalStateException(e.toString());
                }

                if (System.currentTimeMillis() - start > timeout) {
                    if (currentToken != token) {
                        throw new IllegalStateException(
                              "Timeout - Le " + token + " n'a pas été relâché (i.e. release(.))");
                    }
                    return;
                }
            }
        }
    }


    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }


    public static class Token {
        private final String name;


        public Token() {
            this.name = "no-name";
        }


        public Token(String name) {
            this.name = name;
        }


        @Override
        public String toString() {
            return "Token(" + name + ')';
        }
    }
}
