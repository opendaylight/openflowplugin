package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import junit.framework.Assert;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.queue.get.config.reply.Queues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.service.rev131107.GetQueueOutputBuilder;


public class QueueConvertorTest {

    @Test
    public void testGetQueueConfigRequest() {
        GetQueueOutputBuilder getQueueOutputBuilder = new GetQueueOutputBuilder();
        getQueueOutputBuilder.setPort(2123L);
        Queues queuesOF = QueueConvertor.getQueueDataForPort(getQueueOutputBuilder.build());

        Assert.assertEquals(2123L, (long) queuesOF.getPort().getValue());


    }

}
