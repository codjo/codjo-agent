package net.codjo.agent.protocol;
import net.codjo.agent.protocol.SubscribeParticipant.Subscription;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractSubscribeParticipantHandler implements SubscribeParticipantHandler {
    private final List<Subscription> subscribers = new ArrayList<Subscription>();


    public List<Subscription> getSubscribers() {
        return subscribers;
    }


    public void handleSubscribe(Subscription subscription)
          throws RefuseException, NotUnderstoodException {
        subscribers.add(subscription);
    }


    public void handleCancel(Subscription subscription)
          throws FailureException {
        subscribers.remove(subscription);
    }


    public void removeSubscription(String conversationId) {
        for (Iterator iterator = subscribers.iterator(); iterator.hasNext();) {
            Subscription subscription =
                  (Subscription)iterator.next();
            if (conversationId.equals(subscription.getMessage().getConversationId())) {
                iterator.remove();
                subscription.close();
            }
        }
    }
}