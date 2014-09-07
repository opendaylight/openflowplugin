/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.session;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mirehak
 */
public class SwitchConnectionCookieOFImplTest {

    private static final Logger LOG = LoggerFactory
            .getLogger(SwitchConnectionCookieOFImplTest.class);

    private SwitchConnectionCookieOFImpl switchConnectionKey;

    private int seed;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        seed = 4242;
        switchConnectionKey = createSwitchSessionKey((short) 42);
    }

    /**
     * @param datapathId
     * @return
     */
    private static SwitchConnectionCookieOFImpl createSwitchSessionKey(short auxiliary) {
        SwitchConnectionCookieOFImpl key = new SwitchConnectionCookieOFImpl();
        key.setAuxiliaryId(auxiliary);
        return key;
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.session.SwitchConnectionCookieOFImpl#getId()}
     * .
     */
    @Test
    public void testGetId() {
        switchConnectionKey.init(seed);
        LOG.debug("testKey.id: " + Long.toHexString(switchConnectionKey.getCookie()));
        long expected = 710033450L;
        Assert.assertEquals(expected, switchConnectionKey.getCookie());
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.session.SwitchConnectionCookieOFImpl#initUUID()}
     * .
     */
    @Test
    public void testInitId1() {
        try {
            switchConnectionKey.setAuxiliaryId((short) 0);
            switchConnectionKey.init(seed);
            Assert.fail("init should fail with no datapathId");
        } catch (Exception e) {
            // expected
        }
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.session.SwitchConnectionCookieOFImpl#equals(Object)}
     * ,
     * {@link org.opendaylight.openflowplugin.openflow.md.core.session.SwitchConnectionCookieOFImpl#hashCode()}
     * .
     */
    @Test
    public void testHashAndEquals() {
        // insert equal keys
        SwitchConnectionCookieOFImpl key1 = createSwitchSessionKey((short) 42);
        key1.init(seed);

        SwitchConnectionCookieOFImpl key2 = createSwitchSessionKey((short) 42);
        key2.init(seed);

        SwitchConnectionCookieOFImpl key3 = createSwitchSessionKey((short) 43);
        key3.init(seed);
        SwitchConnectionCookieOFImpl key4 = createSwitchSessionKey((short) 21);
        key4.init(seed);

        Map<SwitchConnectionDistinguisher, Integer> keyLot = new HashMap<>();
        keyLot.put(key1, System.identityHashCode(key1));
        Assert.assertEquals(1, keyLot.size());
        keyLot.put(key2, System.identityHashCode(key2));
        Assert.assertEquals(1, keyLot.size());
        keyLot.put(key3, System.identityHashCode(key3));
        Assert.assertEquals(2, keyLot.size());
        keyLot.put(key4, System.identityHashCode(key4));
        Assert.assertEquals(3, keyLot.size());

        // lookup using inited key
        Assert.assertEquals(System.identityHashCode(key2), keyLot.get(key1)
                .intValue());
        Assert.assertEquals(System.identityHashCode(key2), keyLot.get(key2)
                .intValue());
        Assert.assertEquals(System.identityHashCode(key3), keyLot.get(key3)
                .intValue());
        Assert.assertEquals(System.identityHashCode(key4), keyLot.get(key4)
                .intValue());

        // lookup using not inited key
        SwitchConnectionCookieOFImpl keyWithoutInit = createSwitchSessionKey((short) 42);
        Assert.assertNull(keyLot.get(keyWithoutInit));

        // creating brand new key and lookup
        SwitchConnectionCookieOFImpl keyWithInit = createSwitchSessionKey((short) 43);
        keyWithInit.init(seed);
        Assert.assertEquals(System.identityHashCode(key3),
                keyLot.get(keyWithInit).intValue());

        // lookup with key containing encoded part only
        LOG.debug("key3.id: " + Long.toHexString(key3.getCookie()));
        SwitchConnectionCookieOFImpl keyWithoutDPID = new SwitchConnectionCookieOFImpl(734546075L);
        Assert.assertEquals(System.identityHashCode(key3),
                keyLot.get(keyWithoutDPID).intValue());
    }

}
