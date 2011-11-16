package net.codjo.agent.behaviour;
import static net.codjo.agent.test.AgentAssert.behaviourDone;
import net.codjo.agent.test.BehaviourTestCase;
import net.codjo.agent.test.TesterAgent;
import net.codjo.test.common.LogString;
/**
 * Classe de test de {@link net.codjo.agent.behaviour.SequentialBehaviour}.
 */
public class SequentialBehaviourTest extends BehaviourTestCase {
    private LogString log = new LogString();


    public void test_essai() throws Exception {
        TesterAgent agent = new TesterAgent();
        containerFixture.startNewAgent("Séquenseur", agent);
        containerFixture.assertContainsAgent(agent.getAID().getLocalName());

        SequentialBehaviour sequentialBehaviour = SequentialBehaviour.wichStartsWith(doLog("step one"))
              .andThen(doLog("step two"));
        agent.addBehaviour(sequentialBehaviour);

        containerFixture.assertUntilOk(behaviourDone(sequentialBehaviour));

        log.assertContent("step one, step two");
    }


    private SequentialBehaviourTest.DoLogBehaviour doLog(String message) {
        return new DoLogBehaviour(message);
    }


    public void test_done() throws Exception {
        TesterAgent sequentialAgent = new TesterAgent();
        containerFixture.startNewAgent("Séquenseur", sequentialAgent);
        containerFixture.assertContainsAgent(sequentialAgent.getAID().getLocalName());

        final SequentialBehaviour sequentialBehaviour = new SequentialBehaviour();

        sequentialBehaviour.addSubBehaviour(new DoLogBehaviour("step one"));
        sequentialBehaviour.addSubBehaviour(new DoLogBehaviour("step two"));

        sequentialAgent.addBehaviour(sequentialBehaviour);

        containerFixture.assertUntilOk(behaviourDone(sequentialBehaviour));

        log.assertContent("step one, step two");
    }


    public void test_skipNext() throws Exception {
        TesterAgent sequentialAgent = new TesterAgent();
        containerFixture.startNewAgent("Séquenseur", sequentialAgent);
        containerFixture.assertContainsAgent(sequentialAgent.getAID().getLocalName());

        final SequentialBehaviour sequentialBehaviour = new SequentialBehaviour();

        sequentialBehaviour.addSubBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                log.info("step one");
                sequentialBehaviour.skipNext();
            }
        });
        sequentialBehaviour.addSubBehaviour(new DoLogBehaviour("step two"));

        sequentialAgent.addBehaviour(sequentialBehaviour);

        containerFixture.assertUntilOk(behaviourDone(sequentialBehaviour));

        log.assertContent("step one");
    }


    public void test_getAgent() throws Exception {
        TesterAgent sequentialAgent = new TesterAgent();

        SequentialBehaviour sequentialBehaviour = new SequentialBehaviour();
        sequentialAgent.addBehaviour(sequentialBehaviour);

        sequentialBehaviour.addSubBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                log.info("1 - played by " + getAgent().getAID().getLocalName());
            }
        });

        containerFixture.startNewAgent("ohno", sequentialAgent);
        containerFixture.assertContainsAgent(sequentialAgent.getAID().getLocalName());
        containerFixture.assertUntilOk(behaviourDone(sequentialBehaviour));

        log.assertContent("1 - played by ohno");
    }


    private class DoLogBehaviour extends OneShotBehaviour {
        protected String message;


        DoLogBehaviour(String message) {
            this.message = message;
        }


        @Override
        public void action() {
            log.info(message);
        }
    }
}
