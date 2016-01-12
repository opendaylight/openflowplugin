/**
 * Copyright (c) 2013, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.translator;

import java.math.BigInteger;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.errors.rev131116.ErrorType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.NodeErrorNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;

/**
 * OF-1.0 errorMessage support
 */
public class ErrorV10Translator extends AbstractErrorTranslator {

    @Override
    public ErrorType decodeErrorType(int typeArg) {
        ErrorType type = ErrorType.forValue(typeArg);
        switch (type.ordinal()) {
            case 3:
                type = ErrorType.FlowModFailed;
                break;
            case 4:
                type = ErrorType.PortModFailed;
                break;
            case 5:
                type = ErrorType.QueueOpFailed;
                break;
        }
        return type;
    }

    /**
     * @param message error message
     * @param errorType error type
     * @param node reference to node, that sent the error message
     * @return translated error message of general type (OF-1.0)
     */
    @Override
    public org.opendaylight.yang.gen.v1.urn.opendaylight.flow.errors.rev131116.ErrorMessage getGranularNodeErrors(ErrorMessage message, ErrorType errorType, NodeRef node){
        NodeErrorNotificationBuilder nodeErrBuilder = new NodeErrorNotificationBuilder();
        nodeErrBuilder.setTransactionId(new TransactionId(BigInteger.valueOf(message.getXid())));
        nodeErrBuilder.setType(errorType);
        nodeErrBuilder.setCode(message.getCode());
        nodeErrBuilder.setNode(node);

        if (message.getData() != null) {
            nodeErrBuilder.setData(new String(message.getData()));
        }
        return nodeErrBuilder.build();
    }

}
