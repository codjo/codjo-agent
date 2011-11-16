/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.test;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Agent;
import net.codjo.test.common.LogString;
/**
 *
 */
class LogMessageSubStep implements SubStep {
    private LogString log;
    private MessageField[] fields;


    LogMessageSubStep(LogString log) {
        this(log, null);
    }


    LogMessageSubStep(LogString log, MessageField[] fields) {
        this.log = log;
        this.fields = fields;
    }


    public void run(Agent agent, AclMessage message) {
        if (fields != null) {
            for (MessageField field : fields) {
                log.call(field.getName(), field.retrieveValue(message));
            }
        }
        else {
            log.info(message.toString());
        }
    }
}
