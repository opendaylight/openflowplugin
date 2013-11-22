package org.opendaylight.openflowplugin.openflow.md.core.translator;

import java.math.BigInteger;

import org.opendaylight.openflowplugin.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PortStatusMessageToNodeConnectorUpdatedTranslator implements IMDMessageTranslator<OfHeader, DataObject> {
    protected static final Logger LOG = LoggerFactory
            .getLogger(PortStatusMessageToNodeConnectorUpdatedTranslator.class);

    @Override
    public NodeConnectorUpdated translate(SwitchConnectionDistinguisher cookie,
            SessionContext sc, OfHeader msg) {
        if(msg instanceof PortStatusMessage) {
            PortStatusMessage message = (PortStatusMessage)msg;
            BigInteger datapathId = sc.getFeatures().getDatapathId();
            Long portNo = message.getPortNo();
            LOG.error("PortStatusMessage: dataPathId {} portNo {}",datapathId,portNo);
            NodeConnectorUpdatedBuilder builder = new NodeConnectorUpdatedBuilder();
            builder.setId(InventoryDataServiceUtil.nodeConnectorIdfromDatapathPortNo(sc.getFeatures().getDatapathId(),message.getPortNo()));
            builder.setNodeConnectorRef(InventoryDataServiceUtil.nodeConnectorRefFromDatapathIdPortno(datapathId,portNo));
            return builder.build();
        } else {
            // TODO - Do something smarter than returning null if translation fails... what Exception should we throw here?
            return null;
        }
    }
}
