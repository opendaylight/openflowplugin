/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.translator;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.opendaylight.openflowplugin.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.openflow.md.core.sal.ModelDrivenSwitchImpl;
import org.opendaylight.openflowplugin.openflow.md.core.sal.TransactionKey;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.util.ByteUtil;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.errors.rev131116.ErrorType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.NodeErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.NodeErrorNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.NodeFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.node.error.notification.object.reference.FlowRefBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.node.error.notification.object.reference.GroupRefBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.node.error.notification.object.reference.MeterRefBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev131103.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev131103.TransactionMetadata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.NodeGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorTranslator implements IMDMessageTranslator<OfHeader, List<DataObject>> {

    protected static final Logger LOG = LoggerFactory.getLogger(ErrorTranslator.class);

    @Override
    public List<DataObject> translate(SwitchConnectionDistinguisher cookie, SessionContext sc, OfHeader msg) {
        if (msg instanceof ErrorMessage) {
            ErrorMessage message = (ErrorMessage) msg;
            List<DataObject> list = new CopyOnWriteArrayList<DataObject>();
            LOG.debug(" Error Message received: type={}[{}], code={}[{}], data=[{}] ", message.getType(),
                    message.getTypeString(), message.getCode(), message.getCodeString(),
                    ByteUtil.bytesToHexstring(message.getData(), " "));

            // create a Node Error Notification event builder
            NodeErrorNotificationBuilder nodeErrBuilder = new NodeErrorNotificationBuilder();

            // Fill in the Node Error Notification Builder object from the Error
            // Message
            Object object = ModelDrivenSwitchImpl.mapBulkTransaction.get(new TransactionKey(InventoryDataServiceUtil
                    .nodeIdFromDatapathId(sc.getFeatures().getDatapathId()).getValue().toString(), (new TransactionId(
                    BigInteger.valueOf(message.getXid()))).toString()));
            // Fill the TransactionRef & NodeRef for the nodeErrBuilder
            if (object != null) {
                nodeErrBuilder.setTransactionUri(((TransactionMetadata) object).getTransactionUri());
                FlowRefBuilder flowRef = new FlowRefBuilder();
                GroupRefBuilder groupRef = new GroupRefBuilder();
                MeterRefBuilder meterRef = new MeterRefBuilder();
                if (object instanceof NodeFlow) {
                    FlowEntityData flowEntry = new FlowEntityData();
                    flowRef = flowEntry.getBuilder(object);
                    nodeErrBuilder.setObjectReference(flowRef.build());
                } else if (object instanceof NodeGroup) {
                    GroupEntityData groupEntry = new GroupEntityData();
                    groupRef = groupEntry.getBuilder(object);
                    nodeErrBuilder.setObjectReference(groupRef.build());

                } else {
                    MeterEntityData meterEntry = new MeterEntityData();
                    meterRef = meterEntry.getBuilder(object);
                    nodeErrBuilder.setObjectReference(meterRef.build());

                }

            }
            nodeErrBuilder.setTransactionId(new TransactionId(BigInteger.valueOf(message.getXid())));

            nodeErrBuilder.setType(ErrorType.forValue(message.getType()));

            nodeErrBuilder.setCode(message.getCode());

            nodeErrBuilder.setData(new String(message.getData()));

            // TODO -- Augmentation is not handled

            // Note Error_TypeV10 is not handled.

            NodeErrorNotification nodeErrorEvent = nodeErrBuilder.build();
            list.add(nodeErrorEvent);
            return list;
        } else {
            LOG.error("Message is not of Error Message ");
            return Collections.emptyList();
        }
    }

}
