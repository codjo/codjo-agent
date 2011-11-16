/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.test;
import net.codjo.agent.AclMessage;
import net.codjo.test.common.LogString;
import junit.framework.TestCase;
/**
 * Classe de test de {@link LogMessageSubStep}.
 */
public class LogMessageSubStepTest extends TestCase {
    private LogString log = new LogString();


    public void test_logFipaMessage() throws Exception {
        LogMessageSubStep action = new LogMessageSubStep(log);

        AclMessage message = new AclMessage(AclMessage.Performative.AGREE);
        action.run(null, message);

        log.assertContent(message.toString());
    }


    public void test_logPerformative() throws Exception {
        LogMessageSubStep action =
              new LogMessageSubStep(log, new MessageField[]{MessageField.PERFORMATIVE});

        action.run(null, new AclMessage(AclMessage.Performative.AGREE));

        log.assertContent("performative(AGREE)");
    }


    public void test_logSeveralFields() throws Exception {
        LogMessageSubStep action =
              new LogMessageSubStep(log,
                                    new MessageField[]{
                                          MessageField.PERFORMATIVE, MessageField.PROTOCOL,
                                          MessageField.CONTENT
                                    });

        AclMessage message = new AclMessage(AclMessage.Performative.AGREE);
        message.setProtocol("c'est mon protocole");
        message.setContentObject("c'est mon contenu");
        action.run(null, message);

        log.assertContent(
              "performative(AGREE), protocol(c'est mon protocole), content(c'est mon contenu)");
    }


    public void test_logNullContent() throws Exception {
        LogMessageSubStep action =
              new LogMessageSubStep(log, new MessageField[]{MessageField.CONTENT});

        action.run(null, new AclMessage(AclMessage.Performative.AGREE));

        log.assertContent("content(null)");
    }
}
