/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;
import static net.codjo.agent.DFService.service;
import net.codjo.agent.test.AgentContainerFixture;
import net.codjo.agent.test.DummyAgent;
import net.codjo.agent.test.Semaphore;
import java.util.Iterator;
import junit.framework.TestCase;
import jade.domain.FIPAAgentManagement.Property;
/**
 * Classe de test de {@link DFService}.
 */
public class DFServiceTest extends TestCase {
    private AgentContainerFixture fixture = new AgentContainerFixture();


    @Override
    protected void setUp() throws Exception {
        fixture.doSetUp();
    }


    @Override
    protected void tearDown() throws Exception {
        fixture.doTearDown();
    }


    public void test_serviceDescription() throws Exception {
        DFService.ServiceDescription service = service("job", "name");
        service.setName("workflow");
        service.addProperty("toto", "tutu");
        assertEquals("job", service.getType());
        assertEquals("workflow", service.getName());
        Property property = (Property)service.getJadeService().getAllProperties().next();
        assertEquals("toto", property.getName());
        assertEquals("tutu", property.getValue());
    }


    public void test_serviceDescription_constructor() {
        DFService.ServiceDescription service =
              new DFService.ServiceDescription("import", "boris");
        assertEquals("boris", service.getName());
        assertEquals("import", service.getType());
    }


    public void test_serviceDescription_constructorOneArgument() {
        DFService.ServiceDescription service = new DFService.ServiceDescription("import");
        assertEquals("import", service.getType());
    }


    public void test_serviceDescription_language() throws Exception {
        DFService.ServiceDescription description = new DFService.ServiceDescription();

        description.addLanguage("fipa-sl0");
        description.addLanguage("fipa-sl1");

        assertIterator("fipa-sl0, fipa-sl1", description.getAllLanguages());

        description.removeLanguage("fipa-sl1");

        assertIterator("fipa-sl0", description.getAllLanguages());
    }


    public void test_serviceDescription_ontology() throws Exception {
        DFService.ServiceDescription description = new DFService.ServiceDescription();

        description.addOntology("FIPA-Agent-Management");
        description.addOntology("product-ontology");

        assertIterator("FIPA-Agent-Management, product-ontology", description.getAllOntologies());

        description.removeOntology("product-ontology");

        assertIterator("FIPA-Agent-Management", description.getAllOntologies());
    }


    public void test_serviceDescription_protocols() throws Exception {
        DFService.ServiceDescription description = new DFService.ServiceDescription();

        description.addProtocol("fipa-request");
        description.addProtocol("fipa-subscribe");

        assertIterator("fipa-request, fipa-subscribe", description.getAllProtocols());

        description.removeProtocol("fipa-subscribe");

        assertIterator("fipa-request", description.getAllProtocols());
    }


    public void test_serviceDescription_builder() throws Exception {
        DFService.AgentDescription description = new DFService.AgentDescription();

        description.add(service("import", "mint-import-service")
              .protocol("fipa-request")
              .ontology("product-ontology")
              .language("fipa-sl0"));

        DFService.ServiceDescription service = description.getAllServices().next();
        assertEquals("import", service.getType());
        assertEquals("mint-import-service", service.getName());
        assertIterator("fipa-request", service.getAllProtocols());
        assertIterator("product-ontology", service.getAllOntologies());
        assertIterator("fipa-sl0", service.getAllLanguages());
    }


    public void test_serviceDescription_builderDraft() throws Exception {
        DFService.AgentDescription description = new DFService.AgentDescription();

        description.add(service("import", "mint-import-service")
              .protocol("fipa-request")
              .ontology("product-ontology")
              .language("fipa-sl0", "fipa-xml-sl"));

        DFService.ServiceDescription service = description.getAllServices().next();
        assertEquals("import", service.getType());
        assertEquals("mint-import-service", service.getName());
        assertIterator("fipa-request", service.getAllProtocols());
        assertIterator("product-ontology", service.getAllOntologies());
        assertIterator("fipa-sl0, fipa-xml-sl", service.getAllLanguages());
    }


    public void test_agentDescription() throws Exception {
        fixture.startContainer();

        DFService.AgentDescription description = new DFService.AgentDescription();
        description.setAID(new Aid("aid"));
        assertEquals(new Aid("aid").getLocalName(), description.getAID().getLocalName());

        description.addService(service("job", "name"));

        assertServices("service(name/job)", description.getAllServices());
    }


    public void test_agentDescription_factory() throws Exception {
        DFService.AgentDescription description = DFService.createAgentDescription("import");

        assertServices("service(import/import)", description.getAllServices());
    }


    public void test_registerAndSearch() throws Exception {
        final Semaphore semaphore = new Semaphore();

        // Enregistrement d'un service
        DFService.AgentDescription description = new DFService.AgentDescription();
        description.addService(service("pirate", "Capitaine-crochet"));
        description.addService(service("ecolier", "Le-petit-nicolas"));

        RegisterAgent registeredAgent = new RegisterAgent(description, semaphore);
        fixture.startNewAgent("KenanLePirate", registeredAgent);
        semaphore.acquire();

        // Recherche d'un agent pour le service "pirate"
        DFService.AgentDescription[] searchResult = searchUsingDF("pirate");

        // Assertion resultat
        assertEquals(1, searchResult.length);
        assertEquals("KenanLePirate", searchResult[0].getAID().getLocalName());
        assertServices("service(Capitaine-crochet/pirate), service(Le-petit-nicolas/ecolier)",
                       searchResult[0].getAllServices());
    }


    public void test_search_diedAgent() throws Exception {
        final Semaphore semaphore = new Semaphore();

        // Enregistrement d'un service
        DFService.AgentDescription description = new DFService.AgentDescription();
        description.addService(service("pirate", "Capitaine-crochet"));
        AgentController kamikaze =
              fixture.startNewAgent("kamikaze", new RegisterAgent(description, semaphore));

        semaphore.acquire();

        kamikaze.kill();

        fixture.waitForAgentDeath("kamikaze");

        // Recherche d'un agent pour le service "pirate"
        DFService.AgentDescription[] searchResult = searchUsingDF("zombie");

        // Assertion resultat
        assertEquals(0, searchResult.length);
    }


    public void test_deregister() throws Exception {
        final Semaphore semaphore = new Semaphore();

        // Enregistrement d'un service
        DFService.AgentDescription description = new DFService.AgentDescription();
        description.addService(service("flashService", "flash"));

        RegisterAgent flashAgent = new RegisterAgent(description, semaphore);
        fixture.startNewAgent("flashAgent", flashAgent);
        semaphore.acquire();

        // désenregistrement d'un service
        flashAgent.addBehaviour(new DeregisterBehaviour(semaphore));
        semaphore.acquire();

        // Recherche d'un agent pour le service "pirate"
        DFService.AgentDescription[] searchResult = searchUsingDF("flashService");
        assertEquals(0, searchResult.length);
    }


    public void test_deregister_error() throws Exception {
        Agent agent = new DummyAgent();
        fixture.startNewAgent("nicky", agent);
        // désenregistrement d'un service
        try {
            DFService.deregister(agent);
            fail();
        }
        catch (DFService.DFServiceException ex) {
            assertTrue(ex.getLocalizedMessage().contains("not-registered"));
            assertNotNull(ex.getCause());
        }
    }


    public void test_register_error() throws Exception {
        try {
            DFService.register(new DummyAgent(), new DFService.AgentDescription());
            fail();
        }
        catch (DFService.DFServiceException ex) {
            assertEquals("(missing-parameter df-agent-description name)", ex.getLocalizedMessage());
            assertNotNull(ex.getCause());
        }
    }


    public void test_searchFirstAgentWithService() throws Exception {
        Semaphore semaphore = new Semaphore();
        Agent agent = new RegisterAgent(new DFService.AgentDescription(service("import", "mint")), semaphore);
        fixture.startNewAgent("nicky", agent);
        semaphore.acquire();

        Aid aid = DFService.searchFirstAgentWithService(agent, "import");
        assertEquals("nicky", aid.getLocalName());
    }


    public void test_searchFirstAgentWithService_error() throws Exception {
        Agent agent = new DummyAgent();
        fixture.startNewAgent("nicky", agent);

        try {
            DFService.searchFirstAgentWithService(agent, "unknown");
            fail();
        }
        catch (DFService.DFServiceException e) {
            assertEquals("No service of type 'unknown' available", e.getLocalizedMessage());
        }
    }


    private void assertServices(String expected, Iterator<DFService.ServiceDescription> allServices) {
        StringBuffer buffer = new StringBuffer();
        while (allServices.hasNext()) {
            DFService.ServiceDescription description = allServices.next();
            if (buffer.length() > 0) {
                buffer.append(", ");
            }
            buffer.append("service(");
            if (description.getName() != null) {
                buffer.append(description.getName()).append("/");
            }
            buffer.append(description.getType());
            buffer.append(")");
        }
        assertEquals(expected, buffer.toString());
    }


    private void assertIterator(String expected, Iterator<String> iterator) {
        StringBuffer buffer = new StringBuffer();
        while (iterator.hasNext()) {
            String description = iterator.next();
            if (buffer.length() > 0) {
                buffer.append(", ");
            }
            buffer.append(description);
        }
        assertEquals(expected, buffer.toString());
    }


    private DFService.AgentDescription[] searchUsingDF(String type)
          throws ContainerFailureException, DFService.DFServiceException {
        Agent searcherAgent = new DummyAgent();
        fixture.startNewAgent("the Searcher", searcherAgent);

        return DFService.searchForService(searcherAgent, service(type));
    }


    private static class RegisterAgent extends DummyAgent {
        private final DFService.AgentDescription description;
        private final Semaphore semaphore;


        RegisterAgent(DFService.AgentDescription description, Semaphore semaphore) {
            this.description = description;
            this.semaphore = semaphore;
        }


        @Override
        protected void setup() {
            description.setAID(getAID());
            try {
                DFService.register(this, description);
            }
            catch (DFService.DFServiceException exception) {
                ;
            }
            semaphore.release();
        }
    }

    private static class DeregisterBehaviour extends Behaviour {
        private final Semaphore semaphore;


        DeregisterBehaviour(Semaphore semaphore) {
            this.semaphore = semaphore;
        }


        @Override
        protected void action() {
            try {
                DFService.deregister(getAgent());
            }
            catch (DFService.DFServiceException exception) {
                ;
            }
            semaphore.release();
        }


        @Override
        public boolean done() {
            return false;
        }
    }
}
