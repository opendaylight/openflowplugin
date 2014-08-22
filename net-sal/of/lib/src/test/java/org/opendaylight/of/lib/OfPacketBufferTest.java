/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib;

import org.opendaylight.of.lib.dt.*;
import org.opendaylight.util.packet.PacketReader;

/**
 * Base class for the {@link OfPacketReaderTest} and {@link OfPacketWriterTest}.
 *
 * @author Simon Hunt
 */
abstract class OfPacketBufferTest extends AbstractTest {

    static final String TF_OF_PACKET = "ofPacket.hex";

    static final int B = 256;

    static final int EXP_MAGIC = 0xbabe;

    static final GroupId EXP_GID = gid(0x10101019L);
    static final byte[] EXP_GID_ARRAY = {0x10, 0x10, 0x10, 0x19};

    static final VId EXP_VID = vid(3);
    static final byte[] EXP_VID_ARRAY = {0x0, 0x3};

    static final DataPathId EXP_DPID = dpid("0x1949/010203:040506");
    static final byte[] EXP_DPID_ARRAY = {
            0x19, 0x49, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06
    };

    static final BufferId EXP_BID = bid(0xabcdef00L);
    static final byte[] EXP_BID_ARRAY = {0xab-B, 0xcd-B, 0xef-B, 0x00};

    static final QueueId EXP_QID = qid(0x77);
    static final byte[] EXP_QID_ARRAY = {0x0, 0x0, 0x0, 0x77};

    static final MeterId EXP_MID = mid(0xcabb1e);
    static final byte[] EXP_MID_ARRAY = {0x0, 0xca-B, 0xbb-B, 0x1e};

    static final TableId EXP_TID = tid(130);
    static final byte[] EXP_TID_ARRAY = {0x82-B};

    /** Returns an OpenFlow packet reader wrapping a channel buffer wrapping
     * a byte array slurped from a test file.
     * The given path is relative to com/hp/net/of/.
     *
     * @param path the test file path
     * @return a packet reader for the given file
     */
    protected PacketReader getOfPacketReader(String path) {
        return new OfPacketReader(slurpedBytes(path));
    }

}
