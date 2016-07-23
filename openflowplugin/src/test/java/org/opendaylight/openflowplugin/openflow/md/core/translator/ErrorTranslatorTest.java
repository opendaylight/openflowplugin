/**
 * Copyright (c) 2013, 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.translator;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.errors.rev131116.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.errors.rev131116.ErrorType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.BaseNodeErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class ErrorTranslatorTest {

    private final ErrorTranslator errorTranslator = new ErrorTranslator();
    private final ErrorMessageBuilder builder = new ErrorMessageBuilder();;
    private static final BigInteger  DATAPATH_ID = BigInteger.valueOf(0x7777L);
    private static Logger LOG = LoggerFactory
            .getLogger(ErrorTranslatorTest.class);

    @Mock
    SwitchConnectionDistinguisher cookie;
    @Mock
    SessionContext sc;
    @Mock
    GetFeaturesOutput features;

    /**
     * startup method
     */
    @Before
    public void setUp() {
        builder.setCode(21);
        builder.setXid(42L);
        builder.setData(new byte[]{42});

        MockitoAnnotations.initMocks(this);
        when(sc.getFeatures()).thenReturn(features);
        when(features.getDatapathId()).thenReturn(DATAPATH_ID);
    }


    @Test
    public void testTranslate() {
        builder.setType(1);
        List<DataObject> data = errorTranslator.translate(cookie, sc, builder.build());
        assertNotNull(data);
        Assert.assertEquals(1, data.size());
        DataObject obj = data.get(0);
        Assert.assertTrue(obj instanceof BaseNodeErrorNotification);
        BaseNodeErrorNotification nodeError = (BaseNodeErrorNotification)obj;
        NodeRef expectedNode = new NodeRef(
            InventoryDataServiceUtil.identifierFromDatapathId(DATAPATH_ID));
        Assert.assertEquals(expectedNode, nodeError.getNode());
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.openflow.md.core.translator.ErrorTranslator#getGranularNodeErrors(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage, org.opendaylight.yang.gen.v1.urn.opendaylight.flow.errors.rev131116.ErrorType, NodeRef)}.
     *
     * @throws Exception
     */
    @Test
    public void testGetGranularNodeErrors() throws Exception {
        BigInteger dpid = BigInteger.valueOf(0x1122334455667788L);
        NodeRef node = new NodeRef(
            InventoryDataServiceUtil.identifierFromDatapathId(dpid));
        for (ErrorType eType : ErrorType.values()) {
            builder.setType(eType.getIntValue());
            ErrorMessage errorMessage = errorTranslator.getGranularNodeErrors(builder.build(), eType, node);
            LOG.debug("translating errorMessage of type {}", eType);
            assertNotNull("translated error is null", errorMessage);
            Assert.assertEquals(21, errorMessage.getCode().intValue());
            Assert.assertEquals(eType, errorMessage.getType());
            Method getNode = errorMessage.getClass().getMethod("getNode");
            getNode.setAccessible(true);
            Assert.assertEquals(node, getNode.invoke(errorMessage));
            Method getXid = errorMessage.getClass().getMethod("getTransactionId", new Class[0]);
            getXid.setAccessible(true);
            TransactionId xid = (TransactionId) getXid.invoke(errorMessage, new Object[0]);
            Assert.assertEquals(42L, xid.getValue().longValue());
            assertNotNull("data is null", errorMessage.getData());
        }
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.openflow.md.core.translator.ErrorTranslator#decodeErrorType(int)}.
     */
    @Test
    public void testDecodeErrorType() {
        for (ErrorType eType : ErrorType.values()) {
            ErrorType result = errorTranslator.decodeErrorType(eType.getIntValue());
            Assert.assertEquals(eType, result);
        }
    }

}
