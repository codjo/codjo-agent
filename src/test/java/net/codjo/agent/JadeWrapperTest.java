/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;
import net.codjo.agent.test.AgentContainerFixture;
import net.codjo.agent.test.DummyAgent;
import junit.framework.TestCase;
import org.picocontainer.defaults.DefaultPicoContainer;
/**
 * Classe de test de {@link JadeWrapper}.
 */
public class JadeWrapperTest extends TestCase {
    private AgentContainerFixture fixture = new AgentContainerFixture();


    public void test_unwrapp_agent() throws Exception {
        Agent agent = new DummyAgent();
        jade.core.Agent jadeAgent = JadeWrapper.unwrapp(agent);
        assertSame(agent.getJadeAgent(), jadeAgent);
    }


    public void test_unwrapp_aid() throws Exception {
        fixture.startContainer();
        Aid aid = new Aid("aid");
        jade.core.AID jade = JadeWrapper.unwrapp(aid);
        assertEquals(aid.getJadeAID(), jade);
    }


    public void test_wrapp_aid() throws Exception {
        fixture.startContainer();
        jade.core.AID jadeAid = new jade.core.AID("aid", jade.core.AID.ISLOCALNAME);
        Aid aid = JadeWrapper.wrapp(jadeAid);
        assertEquals(jadeAid, aid.getJadeAID());
    }


    public void test_wrapp_message() throws Exception {
        jade.lang.acl.ACLMessage jadeMessage =
              new jade.lang.acl.ACLMessage(jade.lang.acl.ACLMessage.INFORM);
        AclMessage message = JadeWrapper.wrapp(jadeMessage);
        assertSame(jadeMessage, message.getJadeMessage());
    }


    public void test_unwrapp_message() throws Exception {
        AclMessage message = new AclMessage(AclMessage.Performative.INFORM);
        jade.lang.acl.ACLMessage jadeMessage = JadeWrapper.unwrapp(message);
        assertSame(message.getJadeMessage(), jadeMessage);
    }


    public void test_wrapp_behaviour() throws Exception {
        BehaviourMock behaviour = new BehaviourMock();
        jade.core.behaviours.Behaviour jadeBehaviour = new SimpleJadeBehaviour();

        JadeWrapper.wrapp(behaviour, jadeBehaviour);

        assertSame(jadeBehaviour, behaviour.getJadeBehaviour());
    }


    public void test_unwrapp_behaviour() throws Exception {
        BehaviourMock behaviour = new BehaviourMock();
        jade.core.behaviours.Behaviour jadeBehaviour = JadeWrapper.unwrapp(behaviour);
        assertSame(behaviour.getJadeBehaviour(), jadeBehaviour);
    }


    public void test_wrapp_messageTemplate() throws Exception {
        jade.lang.acl.MessageTemplate jadetemplate =
              jade.lang.acl.MessageTemplate.MatchAll();
        MessageTemplate template = JadeWrapper.wrapp(jadetemplate);
        assertSame(jadetemplate, template.getJadeTemplate());
    }


    public void test_unwrapp_messageTemplate() throws Exception {
        MessageTemplate template = MessageTemplate.matchAll();
        jade.lang.acl.MessageTemplate jadeTemplate = JadeWrapper.unwrapp(template);
        assertSame(template.getJadeTemplate(), jadeTemplate);
    }


    public void test_wrapp_null() throws Exception {
        assertNull(JadeWrapper.wrapp((jade.lang.acl.ACLMessage)null));
        assertNull(JadeWrapper.wrapp((jade.core.AID)null));
        assertNull(JadeWrapper.wrapp((jade.lang.acl.MessageTemplate)null));
    }


    public void test_unwrapp_null() throws Exception {
        assertNull(JadeWrapper.unwrapp((AclMessage)null));
        assertNull(JadeWrapper.unwrapp((Behaviour)null));
        assertNull(JadeWrapper.unwrapp((Agent)null));
        assertNull(JadeWrapper.unwrapp((Aid)null));
        assertNull(JadeWrapper.unwrapp((MessageTemplate)null));
    }


    public void test_serviceBootstraper() throws Exception {
        ContainerConfiguration configuration = new ContainerConfiguration();
        JadeWrapper.setPicoContainer(configuration, new DefaultPicoContainer());
        assertNotNull(JadeWrapper.getPicoContainer(configuration));
        JadeWrapper.removePicoContainer(configuration);
        assertNull(JadeWrapper.getPicoContainer(configuration));
    }


    public void test_wrapp_datastore() throws Exception {
        DataStore store = new DataStore();
        assertSame(store.getJadeDataStore(), JadeWrapper.unwrapp(store));

        jade.core.behaviours.DataStore jadeStore = store.getJadeDataStore();
        assertEquals(jadeStore, JadeWrapper.wrapp(jadeStore).getJadeDataStore());
    }


    @Override
    protected void setUp() throws Exception {
        fixture.doSetUp();
    }


    @Override
    protected void tearDown() throws Exception {
        fixture.doTearDown();
    }


    private static class SimpleJadeBehaviour extends jade.core.behaviours.Behaviour {
        @Override
        public void action() {
        }


        @Override
        public boolean done() {
            return false;
        }
    }
}
