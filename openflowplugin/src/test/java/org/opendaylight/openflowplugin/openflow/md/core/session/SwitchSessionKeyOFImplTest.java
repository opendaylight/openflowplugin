/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.session;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mirehak
 */
public class SwitchSessionKeyOFImplTest {

    private static final Logger LOG = LoggerFactory
            .getLogger(SwitchSessionKeyOFImplTest.class);

    private SwitchSessionKeyOFImpl switchConnectionKey;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        switchConnectionKey = createSwitchSessionKey("a1a2a3a4a5a6");
    }

    /**
     * @param datapathId
     * @return
     */
    private static SwitchSessionKeyOFImpl createSwitchSessionKey(
            String datapathId) {
        SwitchSessionKeyOFImpl key = new SwitchSessionKeyOFImpl();
        key.setDatapathId(new BigInteger(datapathId, 16));
        return key;
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.session.SwitchSessionKeyOFImpl#getId()}
     * .
     */
    @Test
    public void testGetId() {
        switchConnectionKey.initId();
        LOG.debug("testKey.id: " + Arrays.toString(switchConnectionKey.getId()));
        byte[] expected = new byte[] { -128, 17, -128, 123, -110, 55, 126, 122,
                -81, 69, 47, -29, -70, -41, 0, -24, 60, 73, 9, 19 };
        Assert.assertArrayEquals(expected, switchConnectionKey.getId());
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.session.SwitchSessionKeyOFImpl#initUUID()}
     * .
     */
    @Test
    public void testInitId1() {
        try {
            switchConnectionKey.setDatapathId(null);
            switchConnectionKey.initId();
            Assert.fail("init should fail with no datapathId");
        } catch (Exception e) {
            // expected
        }
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.session.SwitchSessionKeyOFImpl#equals(Object)}
     * ,
     * {@link org.opendaylight.openflowplugin.openflow.md.core.session.SwitchSessionKeyOFImpl#hashCode()}
     * .
     */
    @Test
    public void testHashAndEquals() {
        // insert equal keys
        SwitchSessionKeyOFImpl key1 = createSwitchSessionKey("1234567890");
        key1.initId();

        SwitchSessionKeyOFImpl key2 = createSwitchSessionKey("1234567890");
        key2.initId();

        SwitchSessionKeyOFImpl key3 = createSwitchSessionKey("123456789");
        key3.initId();

        Map<SwitchConnectionDistinguisher, Integer> keyLot = new HashMap<>();
        keyLot.put(key1, System.identityHashCode(key1));
        Assert.assertEquals(1, keyLot.size());
        keyLot.put(key2, System.identityHashCode(key2));
        Assert.assertEquals(1, keyLot.size());
        keyLot.put(key3, System.identityHashCode(key3));
        Assert.assertEquals(2, keyLot.size());

        // lookup using inited key
        Assert.assertEquals(System.identityHashCode(key2), keyLot.get(key1)
                .intValue());
        Assert.assertEquals(System.identityHashCode(key2), keyLot.get(key2)
                .intValue());
        Assert.assertEquals(System.identityHashCode(key3), keyLot.get(key3)
                .intValue());

        // lookup using not inited key
        SwitchSessionKeyOFImpl keyWithoutInit = createSwitchSessionKey("123456789");
        Assert.assertNull(keyLot.get(keyWithoutInit));

        // creating brand new key and lookup
        SwitchSessionKeyOFImpl keyWithInit = createSwitchSessionKey("123456789");
        keyWithInit.initId();

        Assert.assertEquals(System.identityHashCode(key3),
                keyLot.get(keyWithInit).intValue());

        // lookup with key containing encoded part only
        LOG.debug("key3.id: " + Arrays.toString(key3.getId()));
        SwitchSessionKeyOFImpl keyWithoutDPID = new SwitchSessionKeyOFImpl(
                new byte[] { -106, 12, 30, 77, 23, -44, -116, -11, -49, 40,
                        -122, 5, -82, -33, 81, -65, 100, 51, 34, 76 });
        Assert.assertEquals(System.identityHashCode(key3),
                keyLot.get(keyWithoutDPID).intValue());
    }

}
