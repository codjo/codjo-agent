package net.codjo.agent.protocol;
import net.codjo.agent.AclMessage;
import static net.codjo.agent.AclMessage.Performative;
/**
 *
 */
public class DefaultSubscribeParticipantHandler extends AbstractSubscribeParticipantHandler {

    public void sendInform(String content) {
        AclMessage notification = new AclMessage(Performative.INFORM);
        notification.setContent(content);

        for (SubscribeParticipant.Subscription subscriber : getSubscribers()) {
            subscriber.reply(notification);
        }
    }
}
