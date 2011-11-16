/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;
import jade.domain.FIPAAgentManagement.Property;
import java.util.Iterator;
/**
 * Classe permettant d'accéder au service FIPA des pages jaunes. Le service des pages jaunes est assuré par un
 * agent ('DF').
 */
public class DFService {
    private DFService() {
    }


    /**
     * Enregistre un agent auprès du DF. Cette méthode bloque le thread appelant.
     *
     * @param agent       Agent à inscrire
     * @param description Description des services de l'agent
     *
     * @throws DFServiceException Erreur lors de l'inscription
     */
    public static void register(Agent agent, AgentDescription description)
          throws DFServiceException {
        try {
            jade.domain.DFService.register(JadeWrapper.unwrapp(agent),
                                           description.getJadeDescription());
        }
        catch (jade.domain.FIPAException e) {
            throw new DFServiceException(e.getLocalizedMessage(), e);
        }
    }


    /**
     * Désenregistre un agent auprès du DF. Cette méthode bloque le thread appelant.
     *
     * @param agent Agent à désinscrire
     *
     * @throws DFServiceException Erreur lors de désinscription
     */
    public static void deregister(Agent agent) throws DFServiceException {
        try {
            jade.domain.DFService.deregister(JadeWrapper.unwrapp(agent));
        }
        catch (jade.domain.FIPAException e) {
            throw new DFServiceException(e.getLocalizedMessage(), e);
        }
    }


    /**
     * Cherche les agents permettant de répondre au service(s) demandé(s).
     *
     * @param agent       Agent lançant la recherche
     * @param description Description des services recherché(s)
     *
     * @return La liste des agents répondant à la recherche
     *
     * @throws DFServiceException Erreur lors de la recherche
     */
    public static AgentDescription[] search(Agent agent, AgentDescription description)
          throws DFServiceException {
        try {
            jade.domain.FIPAAgentManagement.DFAgentDescription[] jadeResult =
                  jade.domain.DFService.search(JadeWrapper.unwrapp(agent),
                                               description.getJadeDescription());

            AgentDescription[] result = new AgentDescription[jadeResult.length];
            for (int i = 0; i < jadeResult.length; i++) {
                result[i] = new AgentDescription(jadeResult[i]);
            }
            return result;
        }
        catch (jade.domain.FIPAException e) {
            throw new DFServiceException(e.getLocalizedMessage(), e);
        }
    }


    public static AgentDescription[] searchForService(Agent agent, String service) throws DFServiceException {
        return search(agent, new AgentDescription(new ServiceDescription(service)));
    }


    public static AgentDescription[] searchForService(Agent agent, ServiceDescription serviceDescription)
          throws DFServiceException {
        return search(agent, new AgentDescription(serviceDescription));
    }


    public static Aid searchFirstAgentWithService(Agent agent, String serviceType) throws DFServiceException {
        AgentDescription[] descriptions = search(agent, new AgentDescription(service(serviceType)));
        if (descriptions.length == 0) {
            throw new DFServiceException("No service of type '" + serviceType + "' available");
        }
        return descriptions[0].getAID();
    }


    public static AgentDescription createAgentDescription(String nameAndType) {
        return new AgentDescription(service(nameAndType, nameAndType));
    }


    public static ServiceDescription service(String type) {
        return new ServiceDescription(type);
    }


    public static ServiceDescription service(String type, String name) {
        return new ServiceDescription(type, name);
    }


    /**
     * Description d'un service fourni par un agent (FIPA compliant).
     */
    public static class ServiceDescription {
        private final jade.domain.FIPAAgentManagement.ServiceDescription jadeService;


        public ServiceDescription() {
            this.jadeService = new jade.domain.FIPAAgentManagement.ServiceDescription();
        }


        ServiceDescription(jade.domain.FIPAAgentManagement.ServiceDescription jadeDescription) {
            this.jadeService = jadeDescription;
        }


        public ServiceDescription(String type, String name) {
            this();
            setName(name);
            setType(type);
        }


        public ServiceDescription(String type) {
            this();
            setType(type);
        }


        public void setName(String name) {
            jadeService.setName(name);
        }


        public String getName() {
            return jadeService.getName();
        }


        public void setType(String type) {
            jadeService.setType(type);
        }


        public String getType() {
            return jadeService.getType();
        }


        jade.domain.FIPAAgentManagement.ServiceDescription getJadeService() {
            return jadeService;
        }


        public void addProtocol(String ip) {
            jadeService.addProtocols(ip);
        }


        public boolean removeProtocol(String ip) {
            return jadeService.removeProtocols(ip);
        }


        public Iterator<String> getAllProtocols() {
            //noinspection unchecked
            return jadeService.getAllProtocols();
        }


        public void addOntology(String ontology) {
            jadeService.addOntologies(ontology);
        }


        public boolean removeOntology(String ontology) {
            return jadeService.removeOntologies(ontology);
        }


        public Iterator<String> getAllOntologies() {
            //noinspection unchecked
            return jadeService.getAllOntologies();
        }


        public void addLanguage(String language) {
            jadeService.addLanguages(language);
        }


        public boolean removeLanguage(String language) {
            return jadeService.removeLanguages(language);
        }


        public Iterator<String> getAllLanguages() {
            //noinspection unchecked
            return jadeService.getAllLanguages();
        }


        public void addProperty(String name, Object value) {
            jadeService.addProperties(new Property(name, value));
        }


        public ServiceDescription protocol(String... protocols) {
            for (String protocol : protocols) {
                addProtocol(protocol);
            }
            return this;
        }


        public ServiceDescription ontology(String... ontologies) {
            for (String ontology : ontologies) {
                addOntology(ontology);
            }
            return this;
        }


        public ServiceDescription language(String... languages) {
            for (String language : languages) {
                addLanguage(language);
            }

            return this;
        }
    }
    /**
     * Description de l'ensemble des services rendus par un agent.
     */
    public static class AgentDescription {
        private final jade.domain.FIPAAgentManagement.DFAgentDescription jadeDescription;


        public AgentDescription() {
            this.jadeDescription =
                  new jade.domain.FIPAAgentManagement.DFAgentDescription();
        }


        AgentDescription(jade.domain.FIPAAgentManagement.DFAgentDescription jadeDescription) {
            this.jadeDescription = jadeDescription;
        }


        public AgentDescription(ServiceDescription service) {
            this();
            addService(service);
        }


        public void setAID(Aid aid) {
            jadeDescription.setName(JadeWrapper.unwrapp(aid));
        }


        public Aid getAID() {
            return JadeWrapper.wrapp(jadeDescription.getName());
        }


        public void addService(ServiceDescription serviceDescription) {
            jadeDescription.addServices(serviceDescription.getJadeService());
        }


        public void add(ServiceDescription serviceDescription) {
            addService(serviceDescription);
        }


        public Iterator<ServiceDescription> getAllServices() {
            return new IteratorAdatper(jadeDescription.getAllServices());
        }


        jade.domain.FIPAAgentManagement.DFAgentDescription getJadeDescription() {
            return jadeDescription;
        }
    }

    private static class IteratorAdatper implements Iterator<ServiceDescription> {
        private jade.util.leap.Iterator subIterator;


        IteratorAdatper(jade.util.leap.Iterator allServices) {
            subIterator = allServices;
        }


        public boolean hasNext() {
            return subIterator.hasNext();
        }


        public ServiceDescription next() {
            return new ServiceDescription((jade.domain.FIPAAgentManagement.ServiceDescription)
                  subIterator.next());
        }


        public void remove() {
            subIterator.remove();
        }
    }

    /**
     * Exception lancé lors d'une conversation avec le DF.
     */
    public static class DFServiceException extends Exception {
        public DFServiceException(String message) {
            super(message);
        }


        public DFServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
