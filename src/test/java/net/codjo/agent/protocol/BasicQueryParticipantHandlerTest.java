package net.codjo.agent.protocol;
import net.codjo.agent.AclMessage;
import java.io.Serializable;
import junit.framework.TestCase;
/**
 * Classe de test de {@link net.codjo.agent.protocol.BasicQueryParticipantHandler}.
 */
public class BasicQueryParticipantHandlerTest extends TestCase {
    private BasicQueryParticipantHandler handler;
    private SimpleJavaBean simpleJavaBean;


    public void test_handleRequest() throws Exception {
        AclMessage response = handler.handleRequest(createQuery("name"));

        assertNull(response);
    }


    public void test_handleRequest_failure() throws Exception {
        try {
            handler.handleRequest(createQuery("badProperty"));
            fail();
        }
        catch (NotUnderstoodException ex) {
            assertStartsWith("Property badProperty unknown ", ex.getMessage());
        }
    }


    public void test_executeRequest() throws Exception {
        simpleJavaBean.setName("bobo");
        assertQueryExecution("name", "bobo");

        simpleJavaBean.setName("bibi");
        assertQueryExecution("name", "bibi");

        simpleJavaBean.setAge(25);
        assertBinaryQueryExecution("age", 25);
    }


    public void test_executeRequest_failure() throws Exception {
        try {
            assertQueryExecution("badProperty", "bobo");
            fail();
        }
        catch (FailureException ex) {
            assertStartsWith("Property badProperty unknown ", ex.getMessage());
        }
    }


    private void assertStartsWith(String expected, String actual) {
        assertTrue(actual.startsWith(expected));
    }


    private void assertQueryExecution(String fieldName, String expected)
          throws FailureException {
        AclMessage result = handler.executeRequest(createQuery(fieldName), null);

        assertEquals(AclMessage.Performative.INFORM, result.getPerformative());
        assertEquals("string", result.getLanguage());
        assertEquals(expected, result.getContent());
    }


    private void assertBinaryQueryExecution(String fieldName, Serializable expected)
          throws FailureException {
        AclMessage result = handler.executeRequest(createQuery(fieldName), null);

        assertEquals(AclMessage.Performative.INFORM, result.getPerformative());
        assertEquals(AclMessage.OBJECT_LANGUAGE, result.getLanguage());
        assertEquals(expected, result.getContentObject());
    }


    private AclMessage createQuery(String field) {
        AclMessage request = new AclMessage(AclMessage.Performative.QUERY);
        request.setContent(field);
        return request;
    }


    @Override
    protected void setUp() throws Exception {
        simpleJavaBean = new SimpleJavaBean();
        handler = new BasicQueryParticipantHandler(simpleJavaBean);
    }


    public static class SimpleJavaBean {
        private String name;
        private Integer age;


        public String getName() {
            return name;
        }


        public void setName(String name) {
            this.name = name;
        }


        public Integer getAge() {
            return age;
        }


        public void setAge(Integer age) {
            this.age = age;
        }
    }
}
