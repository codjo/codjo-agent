/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.protocol;
import net.codjo.agent.AclMessage;
import java.io.Serializable;
import java.lang.reflect.Method;
/**
 * {@link RequestParticipantHandler} permettant de recevoir des query et de les traiter.
 */
public class BasicQueryParticipantHandler implements RequestParticipantHandler {
    private final Object javaBean;


    public BasicQueryParticipantHandler(Object javaBean) {
        this.javaBean = javaBean;
    }


    public AclMessage handleRequest(AclMessage request) throws RefuseException, NotUnderstoodException {
        try {
            findGetter(request);
        }
        catch (NoSuchMethodException e) {
            throw new NotUnderstoodException("Property " + request.getContent()
                                             + " unknown (" + e.getMessage() + ")");
        }
        return null;
    }


    public AclMessage executeRequest(AclMessage request, AclMessage response) throws FailureException {
        AclMessage reply = request.createReply(AclMessage.Performative.INFORM);

        try {
            Method getMethod = findGetter(request);
            getMethod.setAccessible(true);
            Object result = getMethod.invoke(javaBean);
            if (getMethod.getReturnType() == String.class) {
                reply.setContent((String)result);
                reply.setLanguage("string");
            }
            else {
                reply.setContentObject((Serializable)result);
                reply.setLanguage(AclMessage.OBJECT_LANGUAGE);
            }
        }
        catch (Exception e) {
            throw new FailureException(
                  "Property " + request.getContent() + " unknown (" + e.getMessage() + ")");
        }

        return reply;
    }


    private Method findGetter(AclMessage request) throws NoSuchMethodException {
        String fieldName = request.getContent();
        String firstChar = fieldName.substring(0, 1);
        Class aClass = javaBean.getClass();
        return aClass.getMethod("get" + firstChar.toUpperCase() + fieldName.substring(1));
    }
}
