/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;
import net.codjo.agent.Aid.NameType;
import net.codjo.agent.test.AgentContainerFixture;
import net.codjo.agent.test.Story.ConnectionType;
import net.codjo.test.common.AssertUtil;
import jade.core.AID;
import junit.framework.TestCase;
/**
 * Classe de test de {@link Aid}.
 */
public class AidTest extends TestCase {
    private AgentContainerFixture fixture = new AgentContainerFixture();


    public void test_constructor() throws Exception {
        Aid aid = new Aid("myAIDName");
        assertEquals("myAIDName", aid.getLocalName());
    }


    public void test_constructor_localUID() throws Exception {
        fixture.startContainer(ConnectionType.NO_CONNECTION);

        Aid aid = JadeWrapper.wrapp(new jade.core.AID("john", jade.core.AID.ISLOCALNAME));

        assertEquals("john", JadeWrapper.unwrapp(aid).getLocalName());
        assertEquals("john@localhost:-1/JADE", JadeWrapper.unwrapp(aid).getName());
        assertEquals("localhost:-1/JADE", JadeWrapper.unwrapp(aid).getHap());

        assertEquals("john", aid.getLocalName());
        assertEquals("john@localhost:-1/JADE", aid.getName());
        assertEquals("localhost", aid.getContainerName());
    }


    public void test_constructor_globalUID() throws Exception {
        Aid aid = JadeWrapper.wrapp(new jade.core.AID("john@A7WX063:35714/JADE", jade.core.AID.ISGUID));

        assertEquals("john", JadeWrapper.unwrapp(aid).getLocalName());
        assertEquals("john@A7WX063:35714/JADE", JadeWrapper.unwrapp(aid).getName());
        assertEquals("A7WX063:35714/JADE", JadeWrapper.unwrapp(aid).getHap());

        assertEquals("john", aid.getLocalName());
        assertEquals("john@A7WX063:35714/JADE", aid.getName());
        assertEquals("A7WX063", aid.getContainerName());
    }


    public void test_constructor_addresses() throws Exception {
        jade.core.AID jadeOne = new jade.core.AID("john@A7WX063:35714/JADE", AID.ISGUID);
        jadeOne.addAddresses("http://google.com");

        Aid copy = JadeWrapper.wrapp(jadeOne);

        AssertUtil.assertEquals(JadeWrapper.unwrapp(copy).getAddressesArray(), jadeOne.getAddressesArray());
    }


    public void test_toString() throws Exception {
        Aid aid = new Aid("myAIDName");

        assertEquals("( agent-identifier :name myAIDName )", aid.toString());
    }


    public void test_getJadeAID() throws Exception {
        Aid aid = new Aid("myAIDName");

        fixture.startContainer(ConnectionType.NO_CONNECTION);

        assertEquals("myAIDName", aid.getJadeAID().getLocalName());
    }


    public void test_getName() throws Exception {
        fixture.startContainer(ConnectionType.NO_CONNECTION);

        Aid aid = new Aid("myAID");

        assertEquals(JadeWrapper.unwrapp(aid).getName(), aid.getName());
    }


    public void test_getContainerName() throws Exception {
        fixture.startContainer(ConnectionType.NO_CONNECTION);

        Aid aid = new Aid("myAID");
        String containerAdresse = aid.getJadeAID().getHap();
        String containerName =
              containerAdresse.substring(0, containerAdresse.indexOf(":"));

        assertEquals(containerName, aid.getContainerName());
    }


    public void test_getContainerName_forGlobalUid() throws Exception {
        Aid aid = new Aid("john@google.com:-1/JADE", NameType.GLOBAL);
        assertEquals("google.com", aid.getContainerName());
    }


    public void test_getContainerName_unspecifiedPort() throws Exception {
        Aid aid = new Aid("john@google.com/JADE", NameType.GLOBAL);
        assertEquals("google.com", aid.getContainerName());
    }


    public void test_getContainerName_weirdHap() throws Exception {
        Aid aid = new Aid("john@google.com", NameType.GLOBAL);
        assertEquals("google.com", aid.getContainerName());
    }


    public void test_getContainerName_unspecifiedHap() throws Exception {
        Aid aid = new Aid("john", NameType.GLOBAL);
        assertNull(aid.getContainerName());
    }


    public void test_getHapForLocalhost() throws Exception {
        fixture.startContainer(ConnectionType.NO_CONNECTION);
        jade.core.AID johnAid = new jade.core.AID("john", jade.core.AID.ISLOCALNAME);
        assertEquals("localhost:-1/JADE", johnAid.getHap());
    }


    public void test_equals() throws Exception {
        final Aid oneAid = new Aid("an agent");
        final Aid theSameAid = new Aid("an agent");
        final Aid anotherAid = new Aid("another agent");

        assertTrue(oneAid.equals(theSameAid));
        assertEquals(oneAid.hashCode(), theSameAid.hashCode());
        assertFalse(oneAid.equals(anotherAid));
        assertFalse(theSameAid.equals(anotherAid));
    }


    public void test_equals_guid() throws Exception {
        final Aid oneAid = new Aid("an agent@host:3030/JADE", NameType.GLOBAL);
        final Aid theSameAid = new Aid("an agent@host:3030/JADE", NameType.GLOBAL);

        assertTrue(oneAid.equals(theSameAid));
        assertEquals(oneAid.hashCode(), theSameAid.hashCode());
    }


    public void test_equals_guidAndLocal() throws Exception {
        fixture.startContainer(ConnectionType.NO_CONNECTION);
        final Aid oneAid = new Aid("john@localhost:-1/JADE", NameType.GLOBAL);
        final Aid theSameAid = new Aid("john");

        assertTrue(oneAid.equals(theSameAid));
        assertEquals(oneAid.hashCode(), theSameAid.hashCode());

        final Aid anotherAid = new Aid("an agent");
        assertFalse(oneAid.equals(anotherAid));
        assertFalse(theSameAid.equals(anotherAid));
    }


    @Override
    protected void tearDown() throws Exception {
        fixture.doTearDown();
    }


    @Override
    protected void setUp() throws Exception {
        fixture.doSetUp();
    }
}
