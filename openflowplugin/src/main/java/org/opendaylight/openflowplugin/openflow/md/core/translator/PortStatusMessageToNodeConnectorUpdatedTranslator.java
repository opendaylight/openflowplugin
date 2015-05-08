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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * translates {@link PortStatusMessage} from OF-API model to MD-SAL model,
 * supports OF-{1.0; 1.3}
 */
public class PortStatusMessageToNodeConnectorUpdatedTranslator implements IMDMessageTranslator<OfHeader, List<DataObject>> {
    private static final Logger LOG = LoggerFactory
            .getLogger(PortStatusMessageToNodeConnectorUpdatedTranslator.class);

    @Override
    public List<DataObject> translate(SwitchConnectionDistinguisher cookie,
            SessionContext sc, OfHeader msg) {
        if(msg instanceof PortStatusMessage) {
            PortStatusMessage port = (PortStatusMessage)msg;
            List<DataObject> list = new CopyOnWriteArrayList<DataObject>();
            BigInteger datapathId = sc.getFeatures().getDatapathId();
            Long portNo = port.getPortNo();
            Short version = port.getVersion();
            if(port.getReason() == PortReason.OFPPRDELETE){
                LOG.debug("PortStatusMessage: version {}  dataPathId {} portNo {} reason {}",version, datapathId,portNo,port.getReason());
                list.add(PortTranslatorUtil.translatePortRemoved(version, datapathId, portNo, port));

            }else{
                LOG.debug("PortStatusMessage: version {}  dataPathId {} portNo {}",version, datapathId,portNo);
                list.add(PortTranslatorUtil.translatePort(version, datapathId, portNo, port));
            }
            return list;
        } else {
            // TODO - Do something smarter than returning null if translation fails... what Exception should we throw here?
            return Collections.emptyList();
        }
    }
}
