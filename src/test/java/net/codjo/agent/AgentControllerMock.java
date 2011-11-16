/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;
import net.codjo.test.common.LogString;
/**
 * Classe de mock pour {@link AgentController}.
 */
public class AgentControllerMock extends AgentController {
    private LogString log = new LogString();


    public AgentControllerMock() {
        super(null);
    }


    @Override
    public void putO2AObject(Object object) throws BadControllerException {
        log.call("putO2AObject", object);
    }


    public LogString getLog() {
        return log;
    }
}
