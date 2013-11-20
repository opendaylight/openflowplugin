package org.opendaylight.openflowplugin.openflow.md.core.translator;

import java.math.BigInteger;

import org.opendaylight.openflowplugin.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyDesc;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiPartMessageDescToNodeUpdatedTranslator implements IMDMessageTranslator<OfHeader, DataObject> {
    protected static final Logger LOG = LoggerFactory.getLogger(PacketInTranslator.class);
    @Override
    public NodeUpdated translate(SwitchConnectionDistinguisher cookie,
            SessionContext sc, OfHeader msg) {
        if(msg instanceof MultipartReply && ((MultipartReply) msg).getType() == MultipartType.OFPMPDESC) {
            LOG.info("MultipartReplyMessage Being translated to NodeUpdated ");
            MultipartReplyMessage message = (MultipartReplyMessage) msg;
            BigInteger datapathId = sc.getFeatures().getDatapathId();
            NodeUpdatedBuilder builder = InventoryDataServiceUtil.nodeUpdatedBuilderFromDataPathId(datapathId);
            FlowCapableNodeUpdatedBuilder fnub = new FlowCapableNodeUpdatedBuilder();
            MultipartReplyDesc body = (MultipartReplyDesc) message.getMultipartReplyBody();
            fnub.setHardware(body.getHwDesc());
            fnub.setManufacturer(body.getMfrDesc());
            fnub.setSerialNumber(body.getSerialNum());
            fnub.setDescription(body.getDpDesc());
            fnub.setSoftware(body.getSwDesc());
            builder.addAugmentation(FlowCapableNodeUpdated.class, fnub.build());
            return builder.build();
        } else {
            return null;
        }
    }

}
