package org.opendaylight.openflowplugin.openflow.md.core.translator;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.opendaylight.openflowplugin.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.TableUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyTableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table.features.TableFeatures;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultipartReplyTableFeaturesToTableUpdatedTranslator implements IMDMessageTranslator<OfHeader, List<DataObject>> {
    protected static final Logger LOG = LoggerFactory.getLogger(MultipartReplyTableFeaturesToTableUpdatedTranslator.class);
    @Override
    public List<DataObject> translate(SwitchConnectionDistinguisher cookie,
            SessionContext sc, OfHeader msg) {
        if(msg instanceof MultipartReply) {
            LOG.info("Multipartreply of type " + ((MultipartReply) msg).getType());
        }
        if(msg instanceof MultipartReply && ((MultipartReply) msg).getType() == MultipartType.OFPMPTABLEFEATURES) {
            BigInteger datapathId = sc.getFeatures().getDatapathId();
            LOG.info("MultipartReply Being translated to TableUpdated ");
            MultipartReplyMessage message = (MultipartReplyMessage) msg;
            MultipartReplyTableFeatures body = (MultipartReplyTableFeatures) message.getMultipartReplyBody();
            List<DataObject> list = new CopyOnWriteArrayList<DataObject>();
            List<TableFeatures> features = body.getTableFeatures();
            for (TableFeatures feature: features) {
                TableUpdatedBuilder builder = InventoryDataServiceUtil.tableUpdatedBuilderFromDataPathIdTableId(datapathId,feature.getTableId());
                list.add(builder.build());
            }
            return list;
        }
        return null;
    }
}
