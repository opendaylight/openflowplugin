package org.opendaylight.openflowplugin.openflow.md.core.translator;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.opendaylight.openflowplugin.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.openflowplugin.openflow.md.util.PortTranslatorUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnectorUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnectorUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyPortDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.desc._case.MultipartReplyPortDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.desc._case.multipart.reply.port.desc.Ports;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiPartReplyPortToNodeConnectorUpdatedTranslator implements IMDMessageTranslator<OfHeader, List<DataObject>> {
    protected static final Logger LOG = LoggerFactory.getLogger(MultiPartReplyPortToNodeConnectorUpdatedTranslator.class);
    @Override
    public List<DataObject> translate(SwitchConnectionDistinguisher cookie,
            SessionContext sc, OfHeader msg) {
        if(msg instanceof MultipartReply && ((MultipartReply) msg).getType() == MultipartType.OFPMPPORTDESC) {
            BigInteger datapathId = sc.getFeatures().getDatapathId();
            LOG.info("MultiPartReplyPortToNodeConnectorUpdatedTranslator Being translated to NodeConnectorUpdated ");
            MultipartReplyMessage message = (MultipartReplyMessage) msg;
            MultipartReplyPortDescCase caseBody = (MultipartReplyPortDescCase) message.getMultipartReplyBody();
            MultipartReplyPortDesc body = caseBody.getMultipartReplyPortDesc();
            List<DataObject> list = new CopyOnWriteArrayList<DataObject>();
            for ( Ports port : body.getPorts() ) {
                LOG.info("Port: " + port);
                port.getPortNo();
                NodeConnectorUpdatedBuilder builder = InventoryDataServiceUtil
                .nodeConnectorUpdatedBuilderFromDatapathIdPortNo(datapathId,port.getPortNo());
                FlowCapableNodeConnectorUpdatedBuilder fcncub = new FlowCapableNodeConnectorUpdatedBuilder();
                fcncub.setAdvertisedFeatures(PortTranslatorUtil.translatePortFeatures(port.getAdvertisedFeatures()));
                fcncub.setConfiguration(PortTranslatorUtil.translatePortConfig(port.getConfig()));
                fcncub.setCurrentFeature(PortTranslatorUtil.translatePortFeatures(port.getCurrentFeatures()));
                fcncub.setCurrentSpeed(port.getCurrSpeed());
                fcncub.setHardwareAddress(port.getHwAddr());
                fcncub.setMaximumSpeed(port.getMaxSpeed());
                fcncub.setName(port.getName());
                fcncub.setPeerFeatures(PortTranslatorUtil.translatePortFeatures(port.getPeerFeatures()));
                fcncub.setPortNumber(port.getPortNo());
                fcncub.setState(PortTranslatorUtil.translatePortState(port.getState()));
                fcncub.setSupported(PortTranslatorUtil.translatePortFeatures(port.getSupportedFeatures()));
                builder.addAugmentation(FlowCapableNodeConnectorUpdated.class, fcncub.build());
                list.add(builder.build());
            }
            return list;
        } else {
            return null;
        }
    }



}
