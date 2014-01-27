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

import org.opendaylight.openflowplugin.openflow.md.core.sal.TransactionKey;
import org.opendaylight.openflowplugin.openflow.md.core.sal.ModelDrivenSwitchImpl;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.openflowplugin.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.util.ByteUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.errors.rev131116.ErrorType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.NodeErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.NodeErrorNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev131103.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorTranslator implements
		IMDMessageTranslator<OfHeader, List<DataObject>> {

	protected static final Logger LOG = LoggerFactory
			.getLogger(ErrorTranslator.class);

	@Override
	public List<DataObject> translate(SwitchConnectionDistinguisher cookie,
			SessionContext sc, OfHeader msg) {
		if (msg instanceof ErrorMessage) {
			ErrorMessage message = (ErrorMessage) msg;
			List<DataObject> list = new CopyOnWriteArrayList<DataObject>();
			LOG.error(
					" Error Message received: type={}[{}], code={}[{}], data=[{}] ",
					message.getType(), message.getTypeString(),
					message.getCode(), message.getCodeString(),
					ByteUtil.bytesToHexstring(message.getData(), " "));

			// create a Node Error Notification event builder
			NodeErrorNotificationBuilder nodeErrBuilder = new NodeErrorNotificationBuilder();

			// Fill in the Node Error Notification Builder object from the Error
			// Message

			nodeErrBuilder.setTransactionId(new TransactionId(BigInteger
					.valueOf(message.getXid())));

			Object object = ModelDrivenSwitchImpl.mapBulkTransaction
					.get(new TransactionKey(InventoryDataServiceUtil
							.nodeIdFromDatapathId(
									sc.getFeatures().getDatapathId())
							.getValue().toString(), (new TransactionId(
							BigInteger.valueOf(message.getXid()))).toString()));
			// Fill the TransactionRef & NodeRef for the nodeErrBuilder
			if (object != null) {
				if (object instanceof AddFlowInput) {
					AddFlowInput addFlowinput = ((AddFlowInput) object);
					nodeErrBuilder.setTransactionUri(addFlowinput
							.getTransactionUri());
					nodeErrBuilder.setNode(addFlowinput.getNode());
				} else if (object instanceof UpdateFlowInput) {
					UpdateFlowInput updateFlowinput = ((UpdateFlowInput) object);
					nodeErrBuilder.setTransactionUri(updateFlowinput
							.getTransactionUri());
					nodeErrBuilder.setNode(updateFlowinput.getNode());
				} else if (object instanceof RemoveFlowInput) {
					RemoveFlowInput removeFlowinput = ((RemoveFlowInput) object);
					nodeErrBuilder.setTransactionUri(removeFlowinput
							.getTransactionUri());
					nodeErrBuilder.setNode(removeFlowinput.getNode());
				} else if (object instanceof AddGroupInput) {
					AddGroupInput addGroupinput = ((AddGroupInput) object);
					nodeErrBuilder.setTransactionUri(addGroupinput
							.getTransactionUri());
					nodeErrBuilder.setNode(addGroupinput.getNode());
				} else if (object instanceof UpdateGroupInput) {
					UpdateGroupInput updateGroupinput = ((UpdateGroupInput) object);
					nodeErrBuilder.setTransactionUri(updateGroupinput
							.getTransactionUri());
					nodeErrBuilder.setNode(updateGroupinput.getNode());
				} else if (object instanceof RemoveGroupInput) {
					RemoveGroupInput removeGroupinput = ((RemoveGroupInput) object);
					nodeErrBuilder.setTransactionUri(removeGroupinput
							.getTransactionUri());
					nodeErrBuilder.setNode(removeGroupinput.getNode());

				}
			}

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
