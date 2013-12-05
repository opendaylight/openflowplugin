package org.opendaylight.openflowplugin.openflow.md.core.translator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.opendaylight.openflowplugin.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.queue.rev130925.property.list.Property;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.queue.rev130925.property.list.PropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.queue.rev130925.queue.list.Queue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.queue.rev130925.queue.list.QueueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.QueueGetConfigReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.queue.get.config.reply.Queues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.service.rev131107.QueueGetConfigReplyBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author usha.m.s@ericsson.com This message will be triggered when we recieve
 *         a notification for queue_config_get_reply.
 *
 */
public class QueueGetConfigReplyTranslator implements IMDMessageTranslator<OfHeader, List<DataObject>> {

    protected static final Logger LOG = LoggerFactory.getLogger(FlowRemovedTranslator.class);
    private static final String PREFIX_SEPARATOR = "/";

    @Override
    public List<DataObject> translate(SwitchConnectionDistinguisher cookie, SessionContext sc, OfHeader msg) {

        if (msg instanceof QueueGetConfigReply) {

            List<DataObject> list = new CopyOnWriteArrayList<DataObject>();
            BigInteger datapathId = sc.getFeatures().getDatapathId();
            QueueGetConfigReply queueGetConfigReply = (QueueGetConfigReply) msg;

            QueueGetConfigReplyBuilder salQueueGetConfigReplyBuilder = new QueueGetConfigReplyBuilder();

            salQueueGetConfigReplyBuilder.setPort(queueGetConfigReply.getPort().getValue());

            salQueueGetConfigReplyBuilder.setNode(new NodeRef(InventoryDataServiceUtil.identifierFromDatapathId(sc
                    .getFeatures().getDatapathId())));
            // List of queueId,port and the list of properties for each
            // queueId,port
            List<Queues> ofQueueData = queueGetConfigReply.getQueues();
            List<Queue> salqueueList = new ArrayList<Queue>();

            for (int queueItem = 0; queueItem < ofQueueData.size(); queueItem++) {

                QueueBuilder queueB = new QueueBuilder();
                queueB.setPort(queueGetConfigReply.getQueues().get(queueItem).getPort().getValue());

                queueB.setQueueId(queueGetConfigReply.getQueues().get(queueItem).getQueueId().getValue());

                List<Property> salProperty = new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.queue.rev130925.property.list.Property>();
                for (int propertyItem = 0; propertyItem < ofQueueData.get(queueItem).getQueueProperty().size(); propertyItem++) {
                    PropertyBuilder propertyB = new PropertyBuilder();
                    propertyB.setProperty(ofQueueData.get(queueItem).getQueueProperty().get(propertyItem).getProperty()
                            .getIntValue());
                    // TODO,Rate for the property is missing from OF
                    salProperty.add(propertyB.build());
                }
                queueB.setProperty(salProperty);
                salqueueList.add(queueB.build());
            }
            salQueueGetConfigReplyBuilder.setQueue(salqueueList);

            list.add(salQueueGetConfigReplyBuilder.build());
            return list;
        }
    else {
        // TODO - Do something smarter than returning null if translation fails... what Exception should we throw here?
        return null;
    }
    }



}
