/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.*;
import org.opendaylight.of.lib.instr.Action;
import org.opendaylight.of.lib.instr.ActionFactory;
import org.opendaylight.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static org.opendaylight.of.lib.CommonUtils.EOLI;
import static org.opendaylight.of.lib.CommonUtils.verMin11;

/**
 * Provides facilities for parsing, creating and encoding {@link Bucket}
 * instances.
 *
 * @author Simon Hunt
 */
public class BucketFactory extends AbstractFactory {

    // internal bucket padding
    private static final int PAD_BUCKET = 4;

    /** The length of a bucket in bytes (fixed portion). */
    static final int BUCKET_LEN = 16;

    private static final BucketFactory BF = new BucketFactory();

    // no instantiation except here.
    private BucketFactory() { }

    /** Returns an identifying tag for the bucket factory.
     *
     * @return an identifying tag
     */
    @Override
    protected String tag() {
        return "BF";
    }

    //======================================================================
    // === Parsing Buckets

    /** Parses a list of bucket structures from the supplied buffer.
     * The caller must calculate and specify the target reader index of
     * the buffer that marks the end of the list, so we know when to stop.
     *  <p>
     *  Note that this method causes the reader index of the underlying
     *  {@code PacketBuffer} to be advanced by the length of the list,
     *  which should leave the reader index at {@code targetRi}.
     *  <p>
     *  This method delegates to {@link #parseBucket} for each individual
     *  bucket.
     *
     * @param targetRi the target reader index
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return a list of parsed buckets
     * @throws MessageParseException if unable to parse the structure
     */
    public static List<Bucket> parseBucketList(int targetRi, OfPacketReader pkt,
                                               ProtocolVersion pv)
            throws MessageParseException {
        List<Bucket> bucketList = new ArrayList<Bucket>();
        while (pkt.ri() < targetRi) {
            Bucket b = parseBucket(pkt, pv);
            bucketList.add(b);
        }
        return bucketList;
    }


    /** Parses a single bucket from the supplied buffer.
     * Note that this method causes the reader index of the underlying
     * {@code PacketBuffer} to be advanced by the length of the bucket.
     *
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return a parsed bucket
     * @throws MessageParseException if unable to parse the bucket
     */
    static Bucket parseBucket(OfPacketReader pkt, ProtocolVersion pv)
            throws MessageParseException {
        Bucket bucket = new Bucket(pv);
        try {
            int currentRi = pkt.ri();
            bucket.length = pkt.readU16();
            final int targetRi = currentRi + bucket.length;
            bucket.weight = pkt.readU16();
            bucket.watchPort = pkt.readBigPortNumber();
            bucket.watchGroup = pkt.readGroupId();
            pkt.skip(PAD_BUCKET);
            while (pkt.ri() < targetRi) {
                Action act = ActionFactory.parseAction(pkt, pv);
                bucket.actions.add(act);
            }

        } catch (VersionMismatchException vme) {
            throw BF.mpe(pkt, vme);
        }
        return bucket;
    }

    //======================================================================
    // === Creating Buckets

    /** Creates a mutable bucket instance.
     *
     * @param pv the protocol version
     * @return a mutable bucket
     * @throws VersionNotSupportedException if the version is not supported
     * @throws VersionMismatchException if the version is &lt; 1.1
     */
    public static MutableBucket createMutableBucket(ProtocolVersion pv) {
        MessageFactory.checkVersionSupported(pv);
        verMin11(pv);
        return new MutableBucket(pv);
    }

    //======================================================================
    // === Encoding Buckets

    /** Encodes a bucket, writing it into the supplied buffer.
     * Note that this method causes the writer index of the underlying
     * {@code PacketBuffer} to be advanced by the length of the bucket.
     *
     * @param bucket the bucket
     * @param pkt the buffer into which the bucket is to be written
     */
    public static void encodeBucket(Bucket bucket, OfPacketWriter pkt) {
        pkt.writeU16(bucket.length);
        pkt.writeU16(bucket.weight);
        // TODO: Review - what to do if port or group is not set?
        //  (watch port/group only required for FastFailover (FF) groups
        pkt.write(bucket.watchPort);
        pkt.write(bucket.watchGroup);
        pkt.writeZeros(PAD_BUCKET);
        ActionFactory.encodeActionList(bucket.actions, pkt);
    }

    /** Encodes a list of buckets, writing them into the supplied buffer.
     * Note that this method causes the writer index of the underlying
     * {@code PacketBuffer} to be advanced by the length of the written
     * buckets.
     *
     * @param buckets the list of buckets
     * @param pkt the buffer into which the buckets are to be written
     */
    public static void encodeBucketList(List<Bucket> buckets,
                                        OfPacketWriter pkt) {
        for (Bucket b: buckets)
            encodeBucket(b, pkt);
    }

    /**
     * Returns the length for the given bucket, in bytes.
     *
     * @param bkt the target bucket
     * @return the length of the bkt
     * @throws NullPointerException if the bkt is null
     */
    public static int getLength(Bucket bkt) {
        return bkt.length;    }

    //======================================================================
    // === Utilities

    /** Outputs a list of buckets in debug string format.
     *
     * @param indent the additional indent (number of spaces)
     * @param bkt the list of buckets
     * @return a multi-line string representation of the list of buckets
     */
    public static String toDebugString(int indent, List<Bucket> bkt) {
        final String indStr = EOLI + StringUtils.spaces(indent);
        StringBuilder sb = new StringBuilder();
        for (Bucket bucket: bkt)
            sb.append(indStr).append(bucket.toDebugString(indent));
        return sb.toString();
    }
}
