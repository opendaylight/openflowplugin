/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.translator;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializationFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializerRegistryImpl;
import org.opendaylight.openflowplugin.api.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.of.message.service.rev160511.OfMessageReceivedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;

public class OfMessagerReceivedTranslator implements IMDMessageTranslator<OfHeader, List<DataObject>> {
	private static final Logger LOG = LoggerFactory.getLogger(OfMessagerReceivedTranslator.class);
	
	  @Override
    public List<DataObject> translate(SwitchConnectionDistinguisher cookie, SessionContext sc, OfHeader msg) {
        OfMessageReceivedBuilder builder = new OfMessageReceivedBuilder();
        BigInteger datapathId = sc.getFeatures().getDatapathId();
        NodeRef nodeRef = new NodeRef(InventoryDataServiceUtil.identifierFromDatapathId(datapathId));
        short ofVersion = sc.getPrimaryConductor().getVersion();
        builder.setIngress(nodeRef);
        builder.setMessage(serialize(msg, ofVersion));
        List<DataObject> list = new CopyOnWriteArrayList<DataObject>();
        list.add(builder.build());
        return list;
    }

    private byte[] serialize(DataObject msg, short ofVersion) {
        SerializationFactory factory = new SerializationFactory();
        SerializerRegistry registry = new SerializerRegistryImpl();
        registry.init();
        ByteBuf output = UnpooledByteBufAllocator.DEFAULT.buffer();
        factory.setSerializerTable(registry);
        try{
        	factory.messageToBuffer(ofVersion, output, msg);
        	byte[] bytes = new byte[output.readableBytes()];
        	output.readBytes(bytes);
        	return bytes;
        } catch (NullPointerException ex){
        	LOG.debug("Error in serializing message. OpenFlow message is malformed: " + 
        	    msg.getImplementedInterface().getName());
        	return new byte[0];
        }
        
    }


}
