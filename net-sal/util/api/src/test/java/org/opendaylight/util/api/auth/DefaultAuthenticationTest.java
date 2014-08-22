/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.api.auth;

import static org.junit.Assert.*;

import org.junit.Test;

import org.opendaylight.util.junit.EqualityTester;

/**
 * Test of the default authentication data implementation.
 *
 * @author Thomas Vachuska
 */
public class DefaultAuthenticationTest {

    @Test
    public void test() {
        Authentication a = 
                new DefaultAuthentication("t", 987654321L, "uid", "user", 
                                          "r1, r2, r3", "did", "domain");
        assertEquals("incorrect token", "t", a.token());
        assertEquals("incorrect expiration", 987654321L, a.expiration());
        assertEquals("incorrect user id", "uid", a.userId());
        assertEquals("incorrect user name", "user", a.userName());
        assertEquals("incorrect domain id", "did", a.domainId());
        assertEquals("incorrect domain name", "domain", a.domainName());
        
        assertEquals("incorrect roles size", 3, a.roles().size());
        assertTrue("missing role r1", a.roles().contains("r1"));
        assertTrue("missing role r2", a.roles().contains("r2"));
        assertTrue("missing role r3", a.roles().contains("r3"));
    }
    
    @Test
    public void equalsAndHashCode() {
        Authentication baseObjToTest = 
                new DefaultAuthentication("t", 987654321L, "uid", "user", 
                                          "r1, r2, r3", "did", "domain");
        Authentication equalsToBase1 = 
                new DefaultAuthentication("t", 987654321L, "uid", "user", 
                                          "r1, r2, r3", "did", "domain");
        Authentication equalsToBase2 = 
                new DefaultAuthentication("t", 987654321L, "uid", "user", 
                                          "r1, r2, r3", "did", "domain");
        Authentication unequalToBase1 = 
                new DefaultAuthentication("ot", 987654321L, "uid", "user", 
                                          "r1, r2, r3", "did", "domain");
        Authentication unequalToBase2 = 
                new DefaultAuthentication(null, 987654321L, "uid", "user", 
                                          "r1, r2, r3", "did", "domain");
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase1);
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase2);
        
        baseObjToTest = 
                new DefaultAuthentication(null, 987654321L, "uid", "user", 
                                          "r1, r2, r3", "did", "domain");
        equalsToBase1 = 
                new DefaultAuthentication(null, 987654321L, "uid", "user", 
                                          "r1, r2, r3", "did", "domain");
        equalsToBase2 = 
                new DefaultAuthentication(null, 987654321L, "uid", "user", 
                                          "r1, r2, r3", "did", "domain");
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase1);
    }
}
