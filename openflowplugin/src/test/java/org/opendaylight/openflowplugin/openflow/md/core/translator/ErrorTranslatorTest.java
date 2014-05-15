/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.translator;

import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.errors.rev131116.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.errors.rev131116.ErrorType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev131103.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class ErrorTranslatorTest {
    
    private ErrorTranslator errorTranslator;
    private static Logger LOG = LoggerFactory
            .getLogger(ErrorTranslatorTest.class);
    
    /**
     * startup method
     */
    @Before
    public void setUp() {
        errorTranslator = new ErrorTranslator();
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.openflow.md.core.translator.ErrorTranslator#getGranularNodeErrors(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage, org.opendaylight.yang.gen.v1.urn.opendaylight.flow.errors.rev131116.ErrorType)}.
     * @throws Exception 
     */
    @Test
    public void testGetGranularNodeErrors() throws Exception {
        for (ErrorType eType : ErrorType.values()) {
            ErrorMessageBuilder builder = new ErrorMessageBuilder();
            builder.setType(eType.getIntValue());
            builder.setCode(21);
            builder.setXid(42L);
            builder.setData(new byte[]{42});

            ErrorMessage errorMessage = errorTranslator.getGranularNodeErrors(builder.build(), eType);
            LOG.debug("translating errorMessage of type {}", eType);
            Assert.assertNotNull("translated error is null", errorMessage);
            Assert.assertEquals(21, errorMessage.getCode().intValue());
            Assert.assertEquals(eType, errorMessage.getType());
            Method getXid = errorMessage.getClass().getMethod("getTransactionId", new Class[0]);
            getXid.setAccessible(true);
            TransactionId xid = (TransactionId) getXid.invoke(errorMessage, new Object[0]);
            Assert.assertEquals(42L, xid.getValue().longValue());
            Assert.assertNotNull("data is null", errorMessage.getData());
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
