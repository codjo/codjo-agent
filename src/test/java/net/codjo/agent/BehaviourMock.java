/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;
import net.codjo.test.common.LogString;
/**
 * Mock de {@link Behaviour}.
 */
class BehaviourMock extends Behaviour {
    LogString log = new LogString();
    private String errorMessage;


    BehaviourMock() {
    }


    private BehaviourMock(String errorMessage) {
        this.errorMessage = errorMessage;
    }


    @Override
    protected void action() {
        log.call("action");
        if (errorMessage != null) {
            throw new NullPointerException(errorMessage);
        }
    }


    @Override
    public boolean done() {
        log.call("done");
        return true;
    }


    static BehaviourMock thatNeverFails(String errorMessage) {
        return new BehaviourMock(errorMessage);
    }
}
