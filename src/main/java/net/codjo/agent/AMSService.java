package net.codjo.agent;
import static net.codjo.agent.JadeWrapper.unwrapp;
import static net.codjo.agent.JadeWrapper.wrapp;
/**
 *
 */
public class AMSService {
    public static final String AMS = jade.domain.FIPANames.AMS;


    private AMSService() {
    }


    public static Aid getFailedReceiver(Agent agent, AclMessage amsFailure) throws AMSServiceException {
        try {
            return wrapp(jade.domain.AMSService.getFailedReceiver(unwrapp(agent), unwrapp(amsFailure)));
        }
        catch (jade.domain.FIPAException error) {
            throw new AMSServiceException(error.getMessage(), error);
        }
    }


    public static class AMSServiceException extends Exception {
        public AMSServiceException(String message) {
            super(message);
        }


        public AMSServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
