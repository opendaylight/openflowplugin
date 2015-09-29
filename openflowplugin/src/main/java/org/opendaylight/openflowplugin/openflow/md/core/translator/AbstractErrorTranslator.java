/**
 * Copyright (c) 2013, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.translator;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.opendaylight.openflowplugin.api.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.util.ByteUtil;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.errors.rev131116.ErrorType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * general support for errorMessage OF-API to MD-SAL translation
 */
public abstract class AbstractErrorTranslator implements IMDMessageTranslator<OfHeader, List<DataObject>> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractErrorTranslator.class);

    @Override
    public List<DataObject> translate(SwitchConnectionDistinguisher cookie, SessionContext sc, OfHeader msg) {
        if (msg instanceof ErrorMessage) {
            ErrorMessage message = (ErrorMessage) msg;
            List<DataObject> list = new CopyOnWriteArrayList<DataObject>();
            if (LOG.isDebugEnabled()) {
                String hexData = "n/a";
                if (message.getData() != null) {
                    hexData = ByteUtil.bytesToHexstring(message.getData(), " ");
                }
                LOG.debug(" Error Message received: type={}[{}], code={}[{}], data=[{}] ", message.getType(),
                        message.getTypeString(), message.getCode(), message.getCodeString(),
                        hexData);

            }

            // TODO -- Augmentation is not handled
            ErrorType type = decodeErrorType(message.getType());
            NodeRef node = new NodeRef(
                InventoryDataServiceUtil.identifierFromDatapathId(
                    sc.getFeatures().getDatapathId()));
            list.add(getGranularNodeErrors(message, type, node));
            return list;
        } else {
            LOG.error("Message is not of Error Message ");
            return Collections.emptyList();
        }
    }

    /**
     * @param message error message
     * @param errorType error type
     * @param node node ref
     * @return error message
     */
    protected abstract org.opendaylight.yang.gen.v1.urn.opendaylight.flow.errors.rev131116.ErrorMessage getGranularNodeErrors(ErrorMessage message, ErrorType errorType, NodeRef node);

    /**
     * @param type error type in source message
     * @return enum for errorType
     */
    public abstract ErrorType decodeErrorType(int type);

}
