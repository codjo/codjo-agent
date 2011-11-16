package net.codjo.agent.imtp;
import jade.core.BaseNode;
import jade.core.HorizontalCommand;
import jade.core.IMTPException;
/**
 *
 */
public class LocalNode extends BaseNode {
    public LocalNode(String name, boolean hasPM) {
        super(name, hasPM);
    }


    public Object accept(HorizontalCommand cmd) throws IMTPException {
        try {
            return serveHorizontalCommand(cmd);
        }
        catch (jade.core.ServiceException e) {
            throw new IMTPException("Marche pas :)", e);
        }
    }


    public boolean ping(boolean hang) throws IMTPException {
        return false;
    }


    public void interrupt() throws IMTPException {
    }


    public void exit() throws IMTPException {
    }
}
