package net.codjo.agent.test;
import net.codjo.agent.Agent;
import net.codjo.test.common.LogString;
/**
 *
 */
public class AgentStep {
    private AgentStep() {
    }


    public static OneShotStep logInfo(final LogString log, final String message) {
        return new OneShotStep() {
            public void run(Agent agent) throws Exception {
                log.info(message);
            }
        };
    }


    public static OneShotStep release(final Semaphore semaphore) {
        return new OneShotStep() {
            public void run(Agent agent) throws Exception {
                semaphore.release();
            }
        };
    }


    public static OneShotStep acquire(final Semaphore semaphore) {
        return new OneShotStep() {
            public void run(Agent agent) throws Exception {
                semaphore.acquire();
            }
        };
    }
}
