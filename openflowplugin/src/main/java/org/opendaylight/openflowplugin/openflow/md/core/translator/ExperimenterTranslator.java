/**
 * Copyright (c) 2014 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor: prasanna.huddar@ericsson.com
 */
package org.opendaylight.openflowplugin.openflow.md.core.translator;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.opendaylight.openflowplugin.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.NodeExperimenterErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.NodeExperimenterErrorNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev131103.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExperimenterTranslator implements IMDMessageTranslator<OfHeader, List<DataObject>> {

	protected static final Logger LOG = LoggerFactory.getLogger(ExperimenterTranslator.class);

	@Override
	public List<DataObject> translate(SwitchConnectionDistinguisher cookie,
			SessionContext sc, OfHeader msg) {
		if( msg instanceof ExperimenterMessage) {
			ExperimenterMessage message = (ExperimenterMessage)msg ;
			List<DataObject> list = new CopyOnWriteArrayList<DataObject>();
			LOG.error(" Experimenter Error Message received: Exp type={}, Exp Id={}, data={} ",
	                message.getExpType(), message.getExperimenter(),
	                new String(message.getData()) ) ;

			// create a Node Experimenter Error Notification event builder
			NodeExperimenterErrorNotificationBuilder nodeErrBuilder = new NodeExperimenterErrorNotificationBuilder() ;

			nodeErrBuilder.setTransactionId(new TransactionId(BigInteger.valueOf( message.getXid() ))) ;

			// This is a fixed value 0xffff ( 65535 )
			nodeErrBuilder.setType(org.opendaylight.yang.gen.v1.urn.opendaylight.flow.errors.rev131116.ErrorType.Experimenter ) ;

			// The experimenterType is defined as long in ExperimenterMessage where is just needs to be integer
			nodeErrBuilder.setExpType(message.getExpType().intValue() ) ;

			nodeErrBuilder.setExperimenterId(message.getExperimenter()) ;

			nodeErrBuilder.setData(new String (message.getData()) ) ;

			//Not handling Augmentation

			NodeExperimenterErrorNotification nodeExpErrorEvent = nodeErrBuilder.build();
			list.add(nodeExpErrorEvent);
			return list;
		}else {
			LOG.error( "Message is not of Experimenter Error Message " ) ;
			return Collections.emptyList();
		}
	}


}
