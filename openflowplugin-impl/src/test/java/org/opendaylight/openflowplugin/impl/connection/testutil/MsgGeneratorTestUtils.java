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

        MultipartReplyMessageBuilder messageBuilder = new MultipartReplyMessageBuilder()
                .setMultipartReplyBody(replyBody)
                .setXid(xid)
                .setFlags(new MultipartRequestFlags(hasNext))
                .setType(MultipartType.OFPMPDESC);
        return messageBuilder;
    }
}
