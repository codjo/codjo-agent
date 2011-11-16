/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;
import org.junit.Test;
/**
 * Classe de test de {@link UserId}.
 */
public class UserIdTest {

    private static final String ENCRYPTED_PASSWORD = "752b674a637367314576527243554f512b76656266673d3d";


    @Test
    public void createId() throws Exception {
        UserId userId = UserId.createId("login", "password");
        assertEquals("login", userId.getLogin());
        assertEquals("password", userId.getPassword());

        long now = System.currentTimeMillis();
        assertEquals((double)now, (double)userId.getLoginTime(), 500.);

        assertEquals("login/" + ENCRYPTED_PASSWORD + "/" + userId.getLoginTime() + "/" + userId.getObjectId(),
                     userId.encode());
    }


    @Test
    public void createId_withBadParameters()
          throws Exception {
        try {
            UserId.createId("lo/gin", "password");
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("login incorrecte : contient le caractère '/'", ex.getMessage());
        }

        UserId userId = null;
        try {
            userId = UserId.createId("login", "pa/ssword");
        }
        catch (IllegalArgumentException ex) {
            fail("Exception inattendue : " + ex.getLocalizedMessage());
        }

        assertNotNull(userId);
    }

    @Test
    public void decodeUserId() throws Exception {
        UserId userId = UserId.decodeUserId("login/" + ENCRYPTED_PASSWORD + "/1143463916134/23334");

        assertEquals("login", userId.getLogin());
        assertEquals("password", userId.getPassword());
        assertEquals(1143463916134L, userId.getLoginTime());
        assertEquals("login/" + ENCRYPTED_PASSWORD + "/1143463916134/23334", userId.encode());
    }


    @Test
    public void decodeUserId_fromBadValue() throws Exception {
        try {
            UserId.decodeUserId("login/1143463916134");
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("Impossible de decoder un UserId : Mauvais format (L/P/time/objectId) !",
                         ex.getMessage());
        }
        try {
            UserId.decodeUserId("login/1/3/4/6");
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("Impossible de decoder un UserId : Mauvais format (L/P/time/objectId) !",
                         ex.getMessage());
        }
    }


    @Test
    public void equalsAndHashCode() throws Exception {
        UserId userId = UserId.decodeUserId("log/" + ENCRYPTED_PASSWORD + "/5/1");
        assertNotNull(userId);

        UserId userIdBis = UserId.decodeUserId("log/" + ENCRYPTED_PASSWORD + "/5/1");

        assertNotSame(userId, userIdBis);
        assertEquals(userId, userIdBis);
        assertEquals(userId.hashCode(), userIdBis.hashCode());

        assertFalse(userId.equals(UserId.decodeUserId("other/" + ENCRYPTED_PASSWORD + "/5/1")));
        assertFalse(
              userId.hashCode() == UserId.decodeUserId("other/" + ENCRYPTED_PASSWORD + "/5/1").hashCode());

        //noinspection LiteralAsArgToStringEquals
        assertFalse(userId.equals("nimportKoa"));
        //noinspection ObjectEqualsNull
        assertFalse(userId.equals(null));
    }
}
