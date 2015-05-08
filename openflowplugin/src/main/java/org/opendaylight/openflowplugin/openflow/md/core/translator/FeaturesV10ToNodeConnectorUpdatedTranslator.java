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
import org.opendaylight.openflowplugin.api.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.util.PortTranslatorUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.features.reply.PhyPort;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FeaturesV10ToNodeConnectorUpdatedTranslator implements IMDMessageTranslator<OfHeader, List<DataObject>> {
    private static final Logger LOG = LoggerFactory
            .getLogger(FeaturesV10ToNodeConnectorUpdatedTranslator.class);

    @Override
    public List<DataObject> translate(SwitchConnectionDistinguisher cookie,
            SessionContext sc, OfHeader msg) {
        if (msg instanceof GetFeaturesOutput) {
            GetFeaturesOutput features = (GetFeaturesOutput) msg;
            List<DataObject> list = new CopyOnWriteArrayList<DataObject>();
            BigInteger datapathId = sc.getFeatures().getDatapathId();
            if( features.getPhyPort() != null ) {
                for (PhyPort port : features.getPhyPort()) {
                    list.add(PortTranslatorUtil.translatePort(msg.getVersion(), datapathId, port.getPortNo(), port));
                }
            }
            return list;
        } else {
            // TODO - Do something smarter than returning null if translation fails... what Exception should we throw here?
            return Collections.emptyList();
        }
    }
}
