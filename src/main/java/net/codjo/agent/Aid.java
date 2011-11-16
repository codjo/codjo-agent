/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;
import jade.core.AID;
import java.util.ArrayList;
import java.util.List;
/**
 * Représente un identifiant d'Agent.
 */
public class Aid {
    private String name;
    private jade.core.AID aid;
    private List<String> addressList = new ArrayList<String>();

    public enum NameType {
        // john@host:8080/JADE
        GLOBAL,
        // john
        LOCAL
    }


    public Aid(String localName) {
        this(localName, NameType.LOCAL);
    }


    /**
     * @deprecated Use {@link #Aid(String, net.codjo.agent.Aid.NameType) }
     */
    @Deprecated
    public Aid(String globalOrLocalName, boolean isLocalName) {
        this(globalOrLocalName, isLocalName ? NameType.LOCAL : NameType.GLOBAL);
    }


    public Aid(String globalOrLocalName, NameType nameType) {
        switch (nameType) {
            case LOCAL:
                this.name = globalOrLocalName;
                break;
            case GLOBAL:
                aid = new AID(globalOrLocalName, AID.ISGUID);
                break;
        }
    }


    Aid(jade.core.AID aid) {
        this.aid = aid;
    }


    public String getLocalName() {
        if (aid == null) {
            return name;
        }
        else {
            return aid.getLocalName();
        }
    }


    @Override
    public String toString() {
        if (aid != null) {
            return aid.toString();
        }
        else {
            return "( agent-identifier :name " + getLocalName() + " )";
        }
    }


    @Override
    public int hashCode() {
        return getLocalName().hashCode();
    }


    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Aid)) {
            return false;
        }

        Aid other = (Aid)object;

        if (other.aid != null && aid != null) {
            return other.aid.equals(aid);
        }
        else if (other.aid != null || aid != null) {
            return other.getJadeAID().equals(getJadeAID());
        }
        else {
            return other.getLocalName().equals(getLocalName());
        }
    }


    public String getName() {
        return getJadeAID().getName();
    }


    public String getContainerName() {
        String containerAdresse = getJadeAID().getHap();
        if (containerAdresse == null) {
            return null;
        }
        int toIndex = containerAdresse.indexOf(":");
        if (toIndex == -1) {
            toIndex = containerAdresse.indexOf("/");
        }
        if (toIndex == -1) {
            toIndex = containerAdresse.length();
        }
        return containerAdresse.substring(0, toIndex);
    }


    public void addAddresses(String url) {
        if (aid != null) {
            aid.addAddresses(url);
        }
        else {
            addressList.add(url);
        }
    }


    jade.core.AID getJadeAID() {
        if (aid == null) {
            aid = new jade.core.AID(name, AID.ISLOCALNAME);
            for (String theUrl : addressList) {
                aid.addAddresses(theUrl);
            }
        }
        return aid;
    }
}
