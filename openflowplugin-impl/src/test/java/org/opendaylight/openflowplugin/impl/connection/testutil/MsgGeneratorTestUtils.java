/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.connection.testutil;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.desc._case.MultipartReplyDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.desc._case.MultipartReplyDescBuilder;

/**
 * openflowplugin-impl
 * org.opendaylight.openflowplugin.impl.connection.testutil
 *
 *
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 *
 * Created: Mar 26, 2015
 */
public class MsgGeneratorTestUtils {

    private MsgGeneratorTestUtils () {
        throw new UnsupportedOperationException("Test Utility class");
    }

    public static MultipartReplyMessageBuilder makeMultipartDescReply(final long xid, final String value, final boolean hasNext) {
        final MultipartReplyDesc descValue = new MultipartReplyDescBuilder().setHwDesc(value).build();
        final MultipartReplyDescCase replyBody = new MultipartReplyDescCaseBuilder()
                                                        .setMultipartReplyDesc(descValue).build();

        final MultipartReplyMessageBuilder messageBuilder = new MultipartReplyMessageBuilder()
                .setMultipartReplyBody(replyBody)
                .setXid(xid)
                .setFlags(new MultipartRequestFlags(hasNext))
                .setType(MultipartType.OFPMPDESC);
        return messageBuilder;
    }

//    public static MultipartReplyMessageBuilder makeMultipartFlowReply(final long xid, final int nrOfFlows, final boolean hasNext) {
//        final List<FlowStats> flowStatsList = generateFlows(nrOfFlows);
//        final MultipartReplyFlow replyFlows = new MultipartReplyFlowBuilder().setFlowStats(flowStatsList).build();
//        final MultipartReplyFlowCase replyBody = new MultipartReplyFlowCaseBuilder()
//                                                        .setMultipartReplyFlow(replyFlows).build();
//
//        final MultipartReplyMessageBuilder messageBuilder = new MultipartReplyMessageBuilder()
//                .setMultipartReplyBody(replyBody)
//                .setXid(xid)
//                .setFlags(new MultipartRequestFlags(hasNext))
//                .setType(MultipartType.OFPMPFLOW);
//        return messageBuilder;
//    }
//
//    private static List<FlowStats> generateFlows(final int nrOfFlows) {
//        final List<FlowStats> flowStatsList = new ArrayList<>();
//        for (int i = 0; i < nrOfFlows; i++) {
//            final FlowStatsBuilder flowStatBuilder = new FlowStatsBuilder();
//            flowStatBuilder.setDurationSec(1L);
//            flowStatBuilder.setDurationNsec(1000000000L);
//            flowStatBuilder.setPacketCount(BigInteger.ONE);
//            flowStatBuilder.setTableId((short) 0);
//            flowStatBuilder.setPriority(i);
//            flowStatBuilder.setCookie(BigInteger.valueOf(i));
//            flowStatBuilder.setMatch(makeMatch());
//            flowStatBuilder.setByteCount(BigInteger.valueOf(i));
//            flowStatsList.add(flowStatBuilder.build());
//        }
//        return flowStatsList;
//    }
//
//    private static Match makeMatch() {
//        final MatchEntryBuilder builder = new MatchEntryBuilder().set;
//        MatchEntryValue value;
//        builder.setMatchEntryValue(value);
//        builder.build();
//        final MatchBuilder matchBuilder = new MatchBuilder();
//        final List<MatchEntry> matchEntries = Collections.singletonList(new MatchEntryBuilder().build());
//        matchBuilder.setMatchEntry(matchEntries);
//        return matchBuilder.build();
//    }
}
