package net.codjo.agent.test;
import net.codjo.agent.AclMessage;
import net.codjo.agent.MessageTemplate;
/**
 * Classe utilitaire pour mettre en place des assert explicite via les {@link TesterAgent} et {@link Story}.
 *
 * @see ReceiveMessageStep#assertReceivedMessage(net.codjo.agent.MessageTemplate)
 */
public abstract class AssertMatchExpression implements MessageTemplate.MatchExpression {
    private Object actual;
    private final Object expected;
    private final String label;


    protected AssertMatchExpression(String label, Object expected) {
        this.expected = expected;
        this.label = label;
    }


    public final boolean match(AclMessage message) {
        actual = extractActual(message);
        if (expected == null || actual == null) {
            return expected == actual;
        }
        return expected.equals(actual);
    }


    protected abstract Object extractActual(AclMessage message);


    @Override
    public final String toString() {
        return label + ": expected(" + expected + ") actual(" + actual + ")";
    }
}
