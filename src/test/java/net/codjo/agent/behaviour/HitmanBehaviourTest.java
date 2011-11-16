/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.behaviour;
import net.codjo.agent.Agent;
import net.codjo.agent.Aid;
import net.codjo.agent.test.BehaviourTestCase;
import net.codjo.agent.test.DummyAgent;
/**
 * Classe de test de {@link net.codjo.agent.behaviour.HitmanBehaviour}.
 */
public class HitmanBehaviourTest extends BehaviourTestCase {
    public void test_action() throws Exception {
        Agent agentToKill = new DummyAgent();
        containerFixture.startNewAgent("John-Kennedy", agentToKill);
        containerFixture.assertContainsAgent("John-Kennedy");

        KillerAgent killer = new KillerAgent(agentToKill.getAID());
        containerFixture.startNewAgent("Lee-Harvey-Oswald", killer);

        containerFixture.assertContainsAgent("Lee-Harvey-Oswald");
        containerFixture.assertNotContainsAgent("John-Kennedy");

        containerFixture.assertBehaviourDone(killer.getHitmanBehaviour());
    }


    public void test_action_failure() throws Exception {
        KillerAgent killer = new KillerAgent(new Aid("John-Kennedy"));
        containerFixture.startNewAgent("Lee-Harvey-Oswald", killer);

        containerFixture.assertContainsAgent("Lee-Harvey-Oswald");

        containerFixture.assertBehaviourDone(killer.getHitmanBehaviour());
    }


    private static class KillerAgent extends DummyAgent {
        private Aid aid;
        private HitmanBehaviour hitmanBehaviour;


        KillerAgent(Aid aid) {
            this.aid = aid;
        }


        @Override
        protected void setup() {
            hitmanBehaviour = new HitmanBehaviour(this, aid);
            addBehaviour(hitmanBehaviour);
        }


        public HitmanBehaviour getHitmanBehaviour() {
            return hitmanBehaviour;
        }
    }
}
