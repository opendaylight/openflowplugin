package org.opendaylight.openflowplugin.openflow.md.core.translator;


import java.math.BigInteger;

import org.opendaylight.openflowplugin.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.errors.rev131116.ErrorType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.NodeErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.NodeErrorNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev131103.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorTranslator implements IMDMessageTranslator<OfHeader, DataObject> {

	protected static final Logger LOG = LoggerFactory.getLogger(ErrorTranslator.class);
	@Override
	public DataObject translate(SwitchConnectionDistinguisher cookie,
			SessionContext sc, OfHeader msg) {
		if( msg instanceof ErrorMessage) {
			ErrorMessage message = (ErrorMessage)msg ;
			LOG.error(" Error Message received: type={}, code={}, data={} ", 
	                message.getType(), message.getCode(),
	                new String(message.getData()) ) ;
					
			
			// create a Node Error Notification event builder
			NodeErrorNotificationBuilder nodeErrBuilder = new NodeErrorNotificationBuilder() ;
			
			// Fill in the Node Error Notification Builder object from the Error Message
						
			nodeErrBuilder.setTransactionId(new TransactionId(BigInteger.valueOf(message.getXid() ) ) ) ;
			
			nodeErrBuilder.setCode(message.getCode()); 
		
			nodeErrBuilder.setType(ErrorType.forValue(message.getType().getIntValue())); 
			
			nodeErrBuilder.setData( new String( message.getData() ) ) ;
			
			// TODO -- Augmentation is not handled
			 
			// Note Error_TypeV10 is not handled.
			
			NodeErrorNotification nodeErrorEvent = nodeErrBuilder.build() ;
			
			return nodeErrorEvent;
		}else {
			LOG.error( "Message is not of Error Message " ) ;
			return null;
		}
	}

}
