package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.queue.rev130925.QueuePacket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.queue.get.config.reply.Queues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.queue.get.config.reply.QueuesBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author usha.m.s@ericsson.com This class is used for converting the data for
 *         the queue_get_config_request
 *
 */
public final class QueueConvertor {

    private static final Logger logger = LoggerFactory.getLogger(ActionConvertor.class);
    private static final String PREFIX_SEPARATOR = "/";

    private QueueConvertor() {

    }

    /**
     * @param Controller
     *            sends queue_get_config_request to the switch and the reply
     *            comes a notification.
     * @return Queues.
     */
    public static Queues getQueueDataForPort(QueuePacket source) {

        source.getPort();
        QueuesBuilder getConfigRequestRequest = new QueuesBuilder();
        getConfigRequestRequest.setPort(new PortNumber(source.getPort()));
        return getConfigRequestRequest.build();

    }

}
