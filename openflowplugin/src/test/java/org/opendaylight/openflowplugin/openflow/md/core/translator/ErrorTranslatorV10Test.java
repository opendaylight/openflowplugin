/**
 * Copyright (c) 2013-2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.translator;

import java.math.BigInteger;
import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.errors.rev131116.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.errors.rev131116.ErrorType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev131103.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class ErrorTranslatorV10Test {

    private ErrorV10Translator errorTranslator;
    private static Logger LOG = LoggerFactory
            .getLogger(ErrorTranslatorV10Test.class);

    /**
     * startup method
     */
    @Before
    public void setUp() {
        errorTranslator = new ErrorV10Translator();
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.openflow.md.core.translator.ErrorTranslator#decodeErrorType(int)}.
     */
    @Test
    public void testDecodeErrorType() {
        for (ErrorType eType : ErrorType.values()) {
            ErrorType result = errorTranslator.decodeErrorType(eType.getIntValue());
            int expectedType = -1;
            switch (eType.getIntValue()) {
            case 3:
                expectedType = 5;
                break;
            case 4:
                expectedType = 7;
                break;
            case 5:
                expectedType = 9;
                break;
            default:
                expectedType = eType.getIntValue();
            }

            Assert.assertEquals(expectedType, result.getIntValue());
        }
    }
}
