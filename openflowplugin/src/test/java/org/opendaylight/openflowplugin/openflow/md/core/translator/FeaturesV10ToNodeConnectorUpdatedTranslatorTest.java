/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.translator;

import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ActionTypeV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Capabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.CapabilitiesV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.features.reply.PhyPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.features.reply.PhyPortBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;

@RunWith(MockitoJUnitRunner.class)
public class FeaturesV10ToNodeConnectorUpdatedTranslatorTest extends TestCase {

    private static final FeaturesV10ToNodeConnectorUpdatedTranslator FEATURES_V_10_TO_NODE_CONNECTOR_UPDATED_TRANSLATOR = new FeaturesV10ToNodeConnectorUpdatedTranslator();
    private static final PortFeatures PORT_FEATURES = new PortFeatures(true, false, false, false, false, false, false, false, false, false, true, true, false, false, false, false);

    @Mock
    SwitchConnectionDistinguisher switchConnectionDistinguisher;
    private static final PortConfig PORT_CONFIG = new PortConfig(true, false, false, false);
    @Mock
    SessionContext sessionContext;
    @Mock
    GetFeaturesOutput featuresOutput;

    @Test
    /**
     * Test method for basic functionality of {@link FeaturesV10ToNodeConnectorUpdatedTranslator#translate(org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher, org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader)}
     */
    public void testTranslate() throws Exception {

        when(sessionContext.getFeatures()).thenReturn(featuresOutput);
        when(featuresOutput.getDatapathId()).thenReturn(BigInteger.valueOf(42));
        List<DataObject> data = FEATURES_V_10_TO_NODE_CONNECTOR_UPDATED_TRANSLATOR.translate(switchConnectionDistinguisher, sessionContext, new MockOfHeader());
        assertNotNull(data);
        assertEquals(10, data.size());
    }

    private final class MockOfHeader implements OfHeader, GetFeaturesOutput {

        @Override
        public Short getVersion() {
            return OFConstants.OFP_VERSION_1_3;
        }

        @Override
        public Long getXid() {
            return new Long(1);
        }

        @Override
        public Class<? extends DataContainer> getImplementedInterface() {
            return GetFeaturesOutput.class;
        }

        @Override
        public <E extends Augmentation<GetFeaturesOutput>> E getAugmentation(final Class<E> eClass) {
            return null;
        }

        @Override
        public BigInteger getDatapathId() {
            return null;
        }

        @Override
        public Long getBuffers() {
            return null;
        }

        @Override
        public Short getTables() {
            return null;
        }

        @Override
        public Short getAuxiliaryId() {
            return null;
        }

        @Override
        public Capabilities getCapabilities() {
            return null;
        }

        @Override
        public Long getReserved() {
            return null;
        }

        @Override
        public CapabilitiesV10 getCapabilitiesV10() {
            return null;
        }

        @Override
        public ActionTypeV10 getActionsV10() {
            return null;
        }

        @Override
        public List<PhyPort> getPhyPort() {
            List<PhyPort> phyPorts = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                PhyPortBuilder phyPortBuilder = new PhyPortBuilder();
                phyPortBuilder.setAdvertisedFeatures(PORT_FEATURES);
                phyPortBuilder.setConfig(PORT_CONFIG);
                phyPortBuilder.setCurrentFeatures(PORT_FEATURES);
                phyPortBuilder.setPeerFeatures(PORT_FEATURES);
                phyPortBuilder.setState(PortState.getDefaultInstance("live"));
                phyPortBuilder.setSupportedFeatures(PORT_FEATURES);
                phyPortBuilder.setPortNo(new Long(42));
                phyPorts.add(phyPortBuilder.build());
            }
            return phyPorts;
        }
    }

}