/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.test;
import java.util.ArrayList;
import java.util.List;
import junit.framework.AssertionFailedError;
/**
 *
 */
public class StoryErrorManager {
    private List<ErrorStructure> errorList;
    private Semaphore semaphore;


    public synchronized boolean hasError() {
        return errorList != null;
    }


    public synchronized String getFirstErrorDescription() {
        if (!hasError()) {
            return null;
        }
        return errorList.get(0).getDescription();
    }


    public synchronized void addError(String agentNickName, Throwable error) {
        if (errorList == null) {
            errorList = new ArrayList<ErrorStructure>(1);
        }
        errorList.add(new ErrorStructure(agentNickName, error));
        if (semaphore != null) {
            semaphore.releaseAll();
        }
    }


    public synchronized void assertNoError() {
        if (hasError()) {
            AssertionFailedError assertionFailedError =
                  new AssertionFailedError(createMessage());
            ErrorStructure first = errorList.get(0);
            if (!(first.getThrowable() instanceof AssertionFailedError)) {
                assertionFailedError.initCause(first.getThrowable());
            }
            throw assertionFailedError;
        }
    }


    private String createMessage() {
        StringBuilder message = new StringBuilder();
        for (Object anErrorList : errorList) {
            ErrorStructure structure = (ErrorStructure)anErrorList;
            if (message.length() > 0) {
                message.append("\n");
            }
            message.append(structure.getDescription());
        }
        return message.toString();
    }


    public void setSemaphore(Semaphore semaphore) {
        this.semaphore = semaphore;
    }


    private static class ErrorStructure {
        private String agentName;
        private Throwable throwable;


        ErrorStructure(String agentNickName, Throwable error) {
            this.agentName = agentNickName;
            this.throwable = error;
        }


        public Throwable getThrowable() {
            return throwable;
        }


        public String getAgentName() {
            return agentName;
        }


        private String getErrorMessage() {
            if (throwable.getMessage() == null) {
                return throwable.getClass().getName();
            }
            return throwable.getMessage();
        }


        public String getDescription() {
            return "agent '" + agentName + "' : " + getErrorMessage();
        }
    }
}
