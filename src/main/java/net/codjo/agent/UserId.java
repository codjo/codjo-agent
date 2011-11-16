/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;
import net.codjo.crypto.common.StringEncrypter;
import java.io.Serializable;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
/**
 * Identifiant d'utilisateur.
 */
public class UserId implements Serializable {
    private String value;
    private String login;
    private String password;
    private long loginTime;
    private int objectId;


    private UserId(String login, String password) {
        this(login, password, System.currentTimeMillis());
    }


    private UserId(String login, String password, long loginTime) {
        checkParameter("login", login);
        this.login = login;
        this.password = password;
        this.loginTime = loginTime;
        this.objectId = System.identityHashCode(this);
        initValue(objectId);
    }


    private UserId(String login, String password, long loginTime, int id) {
        checkParameter("login", login);
        this.login = login;
        this.password = password;
        this.loginTime = loginTime;
        this.objectId = id;
        initValue(id);
    }


    public static UserId createId(String login, String password) {
        return new UserId(login, password);
    }


    public static UserId decodeUserId(String value) {
        String[] items = value.split("/");
        checkValueFormat(items);
        return new UserId(items[0], decodePassword(items[1]),
                          Long.parseLong(items[2]), Integer.parseInt(items[3]));
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof UserId)) {
            return false;
        }
        UserId otherUserId = (UserId)obj;
        return this.value.equals(otherUserId.value);
    }


    @Override
    public int hashCode() {
        return value.hashCode();
    }


    public String encode() {
        return value;
    }


    public String getLogin() {
        return login;
    }


    public String getPassword() {
        return password;
    }


    public long getLoginTime() {
        return loginTime;
    }


    public int getObjectId() {
        return objectId;
    }


    private void checkParameter(String parameterType, String parameter) {
        if (parameter.indexOf('/') != -1) {
            throw new IllegalArgumentException(parameterType
                                               + " incorrecte : contient le caractère '/'");
        }
    }


    private static void checkValueFormat(String[] items) {
        if (items.length != 4) {
            throw new IllegalArgumentException(
                  "Impossible de decoder un UserId : Mauvais format (L/P/time/objectId) !");
        }
    }


    private void initValue(int id) {
        value = login + "/" + encodePassword(password) + "/" + loginTime + "/" + id;
    }


    private String encodePassword(String passwd) {
        return new String(Hex.encodeHex((new StringEncrypter("cle user id")).encrypt(passwd).getBytes()));
    }


    private static String decodePassword(String passwd) {
        try {
            return (new StringEncrypter("cle user id")).decrypt(new String(Hex.decodeHex(passwd.toCharArray())));
        }
        catch (DecoderException e) {
            throw new IllegalArgumentException("Cannot decode the password: " + passwd, e);
        }
    }
}
