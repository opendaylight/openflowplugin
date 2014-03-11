/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.translator;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.errors.rev131116.ErrorType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.NodeErrorNotificationBuilder;

public class ErrorV10Translator extends AbstractErrorTranslator {

    @Override
    public void decodeErrorType(NodeErrorNotificationBuilder nodeErrBuilder, int typeArg) {
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
        nodeErrBuilder.setType(type);
    }

}
