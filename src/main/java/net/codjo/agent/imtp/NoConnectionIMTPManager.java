package net.codjo.agent.imtp;
import jade.core.IMTPException;
import jade.core.IMTPManager;
import jade.core.Node;
import jade.core.PlatformManager;
import jade.core.Profile;
import jade.core.Service;
import jade.core.SliceProxy;
import jade.mtp.TransportAddress;
import jade.util.leap.ArrayList;
import jade.util.leap.List;
import org.apache.log4j.Logger;
/**
 * Ce IMTP permet de bloquer la communication intra-platform.
 *
 * <p/> Pour l'activer il suffit de faire :
 *
 * <pre>
 *   ContainerConfiguration configuration = ...;
 *   configuration.setParameter("mtps", null);
 *   configuration.setParameter("imtp", NoConnectionIMTPManager.class.getName());
 *   ...
 *   mainContainer = AgentContainer.createMainContainer(configuration);
 * </pre>
 */
public class NoConnectionIMTPManager implements IMTPManager {
    private final Logger logger = Logger.getLogger(NoConnectionIMTPManager.class);
    private LocalNode localNode = new LocalNode("local-node", true);


    public void initialize(Profile profile) throws IMTPException {
        logger.info("Demarrage du 'NoConnectionIMTPManager' : Communication intra-platform bloquee");
    }


    public void shutDown() {
    }


    public Node getLocalNode() throws IMTPException {
        return localNode;
    }


    public void exportPlatformManager(PlatformManager mgr) throws IMTPException {
    }


    public void unexportPlatformManager(PlatformManager sm) throws IMTPException {
    }


    public PlatformManager getPlatformManagerProxy() throws IMTPException {
        throw new IMTPException("Pas le droit de communiquer a l'exterieur");
    }


    public PlatformManager getPlatformManagerProxy(String addr) throws IMTPException {
        throw new IMTPException("Pas le droit de communiquer a l'exterieur");
    }


    public void reconnected(PlatformManager pm) {
    }


    public Service.Slice createSliceProxy(String serviceName, Class itf, Node where) throws IMTPException {
        try {
            Class proxyClass = Class.forName(serviceName + "Proxy");
            Service.Slice proxy = (Service.Slice)proxyClass.newInstance();
            if (proxy instanceof SliceProxy) {
                ((SliceProxy)proxy).setNode(where);
            }
            else {
                throw new IMTPException("Class " + proxyClass.getName() + " is not a slice proxy.");
            }
            return proxy;
        }
        catch (Exception e) {
            throw new IMTPException("Error creating a slice proxy", e);
        }
    }


    public List getLocalAddresses() throws IMTPException {
        TransportAddress address = new MyTransportAddress();
        ArrayList list = new ArrayList();
        list.add(address);
        return list;
    }


    public TransportAddress stringToAddr(String addr) throws IMTPException {
        throw new IMTPException("Inutile de faire l'implementation !");
    }


    private static class MyTransportAddress implements TransportAddress {
        public String getProto() {
            return "proto";
        }


        public String getHost() {
            return "localhost";
        }


        public String getPort() {
            return "-1";
        }


        public String getFile() {
            return "";
        }


        public String getAnchor() {
            return "";
        }
    }
}
