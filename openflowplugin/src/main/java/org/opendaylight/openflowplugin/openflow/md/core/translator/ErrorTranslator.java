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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev131103.TransactionMetadata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.node.error.notification.object.reference.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterInput;
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
				nodeErrBuilder.setTransactionUri(((TransactionMetadata) object)
						.getTransactionUri());
				FlowRefBuilder flowRef = new FlowRefBuilder();
				GroupRefBuilder groupRef = new GroupRefBuilder();
				MeterRefBuilder meterRef = new MeterRefBuilder();
				if (object instanceof AddFlowInput) {
					AddFlowInput addFlowinput = ((AddFlowInput) object);
					flowRef.setFlowRef(addFlowinput.getFlowRef());
				} else if (object instanceof UpdateFlowInput) {
					UpdateFlowInput updateFlowinput = ((UpdateFlowInput) object);
					flowRef.setFlowRef(updateFlowinput.getFlowRef());
				} else if (object instanceof RemoveFlowInput) {
					RemoveFlowInput removeFlowinput = ((RemoveFlowInput) object);
					flowRef.setFlowRef(removeFlowinput.getFlowRef());
				} else if (object instanceof AddGroupInput) {
					AddGroupInput addGroupinput = ((AddGroupInput) object);
					groupRef.setGroupRef(addGroupinput.getGroupRef());
				} else if (object instanceof UpdateGroupInput) {
					UpdateGroupInput updateGroupinput = ((UpdateGroupInput) object);
					groupRef.setGroupRef(updateGroupinput.getGroupRef());
				} else if (object instanceof RemoveGroupInput) {
					RemoveGroupInput removeGroupinput = ((RemoveGroupInput) object);
					groupRef.setGroupRef(removeGroupinput.getGroupRef());
				} else if (object instanceof AddMeterInput) {
					AddMeterInput addMeterinput = ((AddMeterInput) object);
					meterRef.setMeterRef(addMeterinput.getMeterRef());
				} else if (object instanceof UpdateMeterInput) {
					UpdateMeterInput updateMeterinput = ((UpdateMeterInput) object);
					meterRef.setMeterRef(updateMeterinput.getMeterRef());
				} else if (object instanceof RemoveMeterInput) {
					RemoveMeterInput removeMeterinput = ((RemoveMeterInput) object);
					meterRef.setMeterRef(removeMeterinput.getMeterRef());
				}
				if (flowRef.build().getFlowRef() != null) {
					nodeErrBuilder.setObjectReference(flowRef.build());
				}
				if (groupRef.build().getGroupRef() != null) {
					nodeErrBuilder.setObjectReference(groupRef.build());
				}
				if (meterRef.build().getMeterRef() != null) {
					nodeErrBuilder.setObjectReference(meterRef.build());
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
