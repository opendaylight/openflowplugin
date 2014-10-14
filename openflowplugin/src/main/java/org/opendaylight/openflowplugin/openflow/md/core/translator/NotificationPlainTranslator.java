/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.translator;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.md.core.NotificationQueueWrapper;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * 
 */
public class NotificationPlainTranslator implements IMDMessageTranslator<OfHeader, List<DataObject>> {
    
    private static final Logger LOG = LoggerFactory
            .getLogger(NotificationPlainTranslator.class);
    
    @Override
    public List<DataObject> translate(SwitchConnectionDistinguisher cookie,
            SessionContext sc, OfHeader msg) {
        List<DataObject> results = null;
        
        if(msg instanceof NotificationQueueWrapper) {
            NotificationQueueWrapper wrappedNotification = (NotificationQueueWrapper) msg;
            BigInteger datapathId = sc.getFeatures().getDatapathId();
            Short version = wrappedNotification.getVersion();
            LOG.debug("NotificationQueueWrapper: version {}  dataPathId {} notification {}", version, datapathId, wrappedNotification.getImplementedInterface());
            results = Lists.newArrayList((DataObject) wrappedNotification.getNotification());
        } else {
            // TODO - Do something smarter than returning null if translation fails... what Exception should we throw here?
            results = Collections.emptyList();
        }
        return results;
    }

}
