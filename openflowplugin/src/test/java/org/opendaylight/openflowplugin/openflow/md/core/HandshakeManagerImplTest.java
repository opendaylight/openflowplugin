/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowplugin.openflow.md.core.plan.ConnectionAdapterStackImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.hello.Elements;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.hello.ElementsBuilder;

import com.google.common.collect.Lists;

/**
 * @author mirehak
 *
 */
public class HandshakeManagerImplTest {
    
    private HandshakeManagerImpl handshakeManager;
    protected ConnectionAdapterStackImpl adapter;
    
    /**
     * invoked before every test method
     */
    @Before
    public void setUp() {
        adapter = new ConnectionAdapterStackImpl();
        handshakeManager = new HandshakeManagerImpl(adapter, (short) 4, ConnectionConductor.versionOrder);
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.openflow.md.core.HandshakeManagerImpl#proposeCommonBitmapVersion(java.util.List)}.
     */
    @Test
    public void testProposeCommonBitmapVersion() {
        Boolean[][] versions = new Boolean[][] {
                {true, true, true, false, false, false},
                {true, true, true, false, false}
        };
        
        for (Boolean[] verasionList : versions) {
            ElementsBuilder elementsBuilder = new ElementsBuilder();
            elementsBuilder.setVersionBitmap(Lists.newArrayList(verasionList));
            Elements element = elementsBuilder.build();
            List<Elements> elements = Lists.newArrayList(element );
            Short proposal = handshakeManager.proposeCommonBitmapVersion(elements);
            Assert.assertEquals(Short.valueOf((short)1), proposal);
        }
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.openflow.md.core.HandshakeManagerImpl#proposeNextVersion(short)}.
     */
    @Test
    public void testProposeNextVersion() {
        short[] remoteVer = new short[] { 0x05, 0x04, 0x03, 0x02, 0x01, 0x8f,
                0xff };
        short[] expectedProposal = new short[] { 0x04, 0x04, 0x01, 0x01, 0x01,
                0x04, 0x04 };

        for (int i = 0; i < remoteVer.length; i++) {
            short actualProposal = handshakeManager
                    .proposeNextVersion(remoteVer[i]);
            Assert.assertEquals(
                    String.format("proposing for version: %04x", remoteVer[i]),
                    expectedProposal[i], actualProposal);
        }

        try {
            handshakeManager.proposeNextVersion((short) 0);
            Assert.fail("there should be no proposition for this version");
        } catch (Exception e) {
            // expected
        }
    }

}
