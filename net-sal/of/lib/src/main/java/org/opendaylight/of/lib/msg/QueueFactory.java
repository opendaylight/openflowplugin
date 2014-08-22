/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.*;
import org.opendaylight.util.PrimitiveUtils;
import org.opendaylight.util.ResourceUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static org.opendaylight.of.lib.CommonUtils.verMin12;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;

/**
 * Provides facilities for parsing, creating, and encoding {@link Queue}
 * instances.
 *
 * @author Simon Hunt
 */
public class QueueFactory extends AbstractFactory {

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            QueueFactory.class, "queueFactory");

    private static final String E_NOT_RATE = RES.getString("e_not_rate");
    private static final String E_NOT_EXPER = RES.getString("e_not_exper");

    static final int LIB_QUEUE_STATS_V0 = 32;
    static final int LIB_QUEUE_STATS_V3 = 40;

    static final int PV_01_Q_HEAD_PAD = 2;
    static final int PV_23_Q_HEAD_PAD = 6;
    static final int PV_01_Q_HEAD_LEN = 8;
    static final int PV_23_Q_HEAD_LEN = 16;

    static final int PROP_HEADER_PAD = 4;
    static final int PROP_HEADER_LEN = 8;
    static final int RATE_PROP_PAD = 6;
    static final int EXP_PROP_PAD = 4;
    static final int EXP_ID_LEN = 4;

    private static final int RATE_PROP_LEN = 16;
    private static final int EXP_PROP_FIXED_LEN = 16;
    private static final int PORT_ID_LEN = 4;


    private static final QueueFactory QF = new QueueFactory();

    // no instantiation except here
    private QueueFactory() { }

    /** Returns an identifying tag for the queue factory.
     *
     * @return an identifying tag
     */
    @Override
    protected String tag() {
        return "QF";
    }

    // ========================================================= PARSING ====

    /**
     * Parses a list of Queue structures from the supplied buffer.  The caller
     * must calculate and specify the target reader index of the buffer that
     * marks the end of the list, so we know when to stop.
     * <p>
     * Note that this method causes the reader index of the underlying
     * {@code PacketBuffer} to be advanced by the length of the list, which
     * should leave the read index at {@code targetRi}.
     * <p>
     * This method delegates to {@link #parseQueue} for each individual
     * queue.
     *
     * @param targetRi the target reader index
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return a list of parsed queues
     * @throws MessageParseException if a problem parsing the buffer
     */
    public static List<Queue> parseQueueList(int targetRi, OfPacketReader pkt,
                                             ProtocolVersion pv)
            throws MessageParseException {
        List<Queue> queueList = new ArrayList<Queue>();
        while(pkt.ri() < targetRi) {
            Queue q = parseQueue(pkt, pv);
            queueList.add(q);
        }
        return queueList;
    }

    /** Parses the given packet buffer as a queue structure.
     *
     * @param pkt the packet buffer
     * @param pv the protocol version
     * @return the parsed queue instance
     * @throws MessageParseException if a problem parsing the buffer
     */
    static Queue parseQueue(OfPacketReader pkt, ProtocolVersion pv)
            throws MessageParseException {
        Queue queue = new Queue(pv);
        final boolean newer = pv.ge(ProtocolVersion.V_1_2);
        int qHeadLen = newer ? PV_23_Q_HEAD_LEN : PV_01_Q_HEAD_LEN;
        try {
            queue.queueId = pkt.readQueueId();
            if (newer)
                queue.port = PortFactory.parsePortNumber(pkt, pv);
            queue.length = pkt.readU16();
            pkt.skip(newer ? PV_23_Q_HEAD_PAD : PV_01_Q_HEAD_PAD);
            // time to read in the properties
            int remainingBytes = queue.length - qHeadLen;
            List<QueueProperty> props = new ArrayList<QueueProperty>();
            while (remainingBytes > 0) {
                QueueProperty qp = parseProperty(pkt, pv);
                remainingBytes -= qp.header.length;
                props.add(qp);
            }
            queue.props = props;
            if (remainingBytes != 0)
                throw QF.mpe(pkt, "bad QHead len (too small): " +
                            queue.length + " (remaining bytes: " +
                            remainingBytes + ")");
        } catch (VersionMismatchException vme) {
            throw QF.mpe(pkt, vme);
        }
        return queue;
    }

    /** Parses a single property from the buffer.
     *
     * @param pkt the buffer
     * @param pv the protocol version
     * @return the instantiated property
     * @throws MessageParseException if there was an issue
     */
    private static QueueProperty parseProperty(OfPacketReader pkt,
                                               ProtocolVersion pv)
            throws MessageParseException {
        try {
            QueueProperty.Header header = parseHeader(pkt, pv);
            return createParsedPropInstance(header, pkt, pv);
        } catch (Exception e) {
            throw QF.mpe(pkt, e);
        }
    }

    /** Parses a property header from the buffer.
     *
     * @param pkt the buffer
     * @param pv the protocol version
     * @return the parsed property header
     * @throws MessageParseException if there is an issue parsing the header
     * @throws DecodeException if there is an issue parsing the property type
     */
    private static QueueProperty.Header parseHeader(OfPacketReader pkt,
                                                    ProtocolVersion pv)
            throws MessageParseException, DecodeException {
        QueueProperty.Header hdr = new QueueProperty.Header();
        int code = pkt.readU16();
        hdr.type = QueuePropType.decode(code, pv);
        hdr.length = pkt.readU16();
        pkt.skip(PROP_HEADER_PAD);
        return hdr;
    }

    /** Creates the correct concrete instance for the property type.
     *
     * @param hdr the property header
     * @param pkt the buffer to continue reading from
     * @param pv the protocol version
     * @return the property instance
     */
    private static QueueProperty
    createParsedPropInstance(QueueProperty.Header hdr, OfPacketReader pkt,
                             ProtocolVersion pv) {
        QueueProperty qp = null;
        switch (hdr.type) {
            case MIN_RATE:
                qp = readRate(new QPropMinRate(hdr), pkt, pv);
                break;
            case MAX_RATE:
                verMin12(pv);
                qp = readRate(new QPropMaxRate(hdr), pkt, pv);
                break;
            case EXPERIMENTER:
                verMin12(pv);
                qp = readExper(new QPropExperimenter(hdr), pkt, pv);
                break;
        }
        return qp;
    }

    /** Reads the queue rate field from the specified packet reader.
     *
     * @param prop queue property
     * @param pkt packet reader
     * @param pv protocol version
     * @return queue property descriptor
     */
    private static QueueProperty readRate(QPropRate prop,
                                          OfPacketReader pkt,
                                          ProtocolVersion pv) {
        prop.rate = pkt.readU16();
        pkt.skip(RATE_PROP_PAD);
        return prop;
    }

    /** Reads the queue experimenter field from the specified packet reader.
     *
     * @param prop queue property
     * @param pkt packet reader
     * @param pv protocol version
     * @return queue property descriptor
     */
    private static QueueProperty readExper(QPropExperimenter prop,
                                           OfPacketReader pkt,
                                           ProtocolVersion pv) {
        prop.id = pkt.readInt();
        pkt.skip(EXP_PROP_PAD);
        int dataBytes = prop.header.length -
                (PROP_HEADER_LEN + EXP_ID_LEN + EXP_PROP_PAD);
        prop.data = pkt.readBytes(dataBytes);
        return prop;
    }

    // ======================================================== CREATING ====

    /** Creates a mutable queue. Initially, no fields are set.
     *
     * @param pv the required protocol version
     * @return the queue structure
     * @throws VersionNotSupportedException if the version is not supported
     */
    public static MutableQueue createQueue(ProtocolVersion pv) {
        MessageFactory.checkVersionSupported(pv);
        return new MutableQueue(pv);
    }

    /** Creates a rate property. The {@code type} parameter should be either
     * {@link QueuePropType#MIN_RATE MIN_RATE} or
     * {@link QueuePropType#MAX_RATE MAX_RATE}.
     * The {@code rate} parameter is the rate expressed in 1/10ths of
     * a percent. For example:
     * <ul>
     *     <li>455 = 45.5%</li>
     *     <li>1000 = 100.0%</li>
     *     <li>&gt;1000 = disabled</li>
     * </ul>
     *
     * @param type either MIN_RATE or MAX_RATE
     * @param rate the rate in 1/10ths of a percent
     * @return a rate property
     * @throws IllegalArgumentException if rate is invalid
     */
    public static QueueProperty createProperty(QueuePropType type, int rate) {
        PrimitiveUtils.verifyU16(rate);

        QueueProperty.Header hdr = new QueueProperty.Header();
        hdr.type = type;
        hdr.length = RATE_PROP_LEN;

        QPropRate qp = null;
        switch (type) {
            case MIN_RATE:
                qp = new QPropMinRate(hdr);
                break;
            case MAX_RATE:
                qp = new QPropMaxRate(hdr);
                break;
            default:
                throw new IllegalArgumentException(E_NOT_RATE + type);
        }
        qp.rate = rate;
        return qp;
    }

    /** Creates an experimenter property. The {@code type} parameter
     * must be {@link QueuePropType#EXPERIMENTER EXPERIMENTER}.
     *
     * @param type EXPERIMENTER
     * @param eid Experimenter id
     * @param data experimenter-defined data
     * @return an experimenter property
     */
    public static QueueProperty createProperty(QueuePropType type,
                                               ExperimenterId eid,
                                               byte[] data) {
        return createProperty(type, eid.encodedId(), data);
    }

    /** Creates an experimenter property. The {@code type} parameter
     * must be {@link QueuePropType#EXPERIMENTER EXPERIMENTER}.
     *
     * @param type EXPERIMENTER
     * @param encId an encoded experimenter id
     * @param data experimenter-defined data
     * @return an experimenter property
     */
    public static QueueProperty createProperty(QueuePropType type, int encId,
                                               byte[] data) {
        if (type != QueuePropType.EXPERIMENTER)
            throw new IllegalArgumentException(E_NOT_EXPER + type);

        QueueProperty.Header hdr = new QueueProperty.Header();
        hdr.type = type;
        QPropExperimenter qp = new QPropExperimenter(hdr);
        qp.id = encId;
        qp.data = data.clone();
        hdr.length = EXP_PROP_FIXED_LEN + data.length;
        // TODO - need to validate prop length is div/8 ?
        return qp;
    }


    // ======================================================== ENCODING ====

    /**
     * Encodes a list of queues, writing them into the supplied buffer.
     * Note that this method causes the writer index of the underlying
     * {@code PacketBuffer} to be advanced by the length of the written
     * queues.
     *
     * @param queues the list of queues
     * @param pkt the buffer into which the queues are to be written
     */
    public static void encodeQueueList(List<Queue> queues, OfPacketWriter pkt) {
        for (Queue q : queues)
            encodeQueue(q, pkt);
    }

    /** Writes the specified queue structure into the specified buffer.
     * It is assumed that the writer index is in the correct place for the
     * start of the structure. At the end of this method call, the
     * writer index will have advanced by the length of the encoded queue.
     *
     * @param queue the queue to encode
     * @param pkt the buffer to write into
     * @throws IllegalArgumentException if the queue is mutable
     */
    public static void encodeQueue(Queue queue, OfPacketWriter pkt) {
        if (MutableQueue.class.isInstance(queue))
            throw new IllegalArgumentException("Mutable Queue: " + queue);
        final ProtocolVersion pv = queue.getVersion();
        final boolean newer = pv.ge(ProtocolVersion.V_1_2);

        pkt.write(queue.queueId);
        if (newer) {
            if (queue.port == null)
                pkt.writeZeros(PORT_ID_LEN);
            else
                pkt.write(queue.port);
        }
        pkt.writeU16(queue.length);
        pkt.writeZeros(newer ? PV_23_Q_HEAD_PAD : PV_01_Q_HEAD_PAD);
        // time to write out the properties
        for (QueueProperty qp: queue.props) {
            encodeProperty(qp, pkt, pv);
        }
    }

    /** Encodes the specified property into the given buffer.
     *
     * @param qp the queue property to encode
     * @param pkt the buffer to write into
     * @param pv the protocol version
     */
    private static void encodeProperty(QueueProperty qp, OfPacketWriter pkt,
                                       ProtocolVersion pv) {
        QueuePropType type = qp.getType();
        pkt.writeU16(type.getCode(pv));
        pkt.writeU16(qp.header.length);
        pkt.writeZeros(PROP_HEADER_PAD);
        switch (type) {
            case MIN_RATE:
            case MAX_RATE:
                writeRate((QPropRate) qp, pkt, pv);
                break;
            case EXPERIMENTER:
                writeExper((QPropExperimenter) qp, pkt, pv);
                break;
        }
    }

    /** Write queue rate field into the specified packet writer.
     *
     * @param qp queue field
     * @param pkt packet writer
     * @param pv protocol version
     */
    private static void writeRate(QPropRate qp, OfPacketWriter pkt,
                                  ProtocolVersion pv) {
        pkt.writeU16(qp.getRate());
        pkt.writeZeros(RATE_PROP_PAD);
    }

    /** Write queue experimenter field into the specified packet writer.
     *
     * @param qp queue field
     * @param pkt packet writer
     * @param pv protocol version
     */
    private static void writeExper(QPropExperimenter qp, OfPacketWriter pkt,
                                   ProtocolVersion pv) {
        pkt.writeInt(qp.id);
        pkt.writeZeros(EXP_PROP_PAD);
        pkt.writeBytes(qp.data);
    }

    // ======================================================== UTILITIES ===

    /** Returns the length of a queue stats structure in bytes.
     *
     * @param pv the protocol version
     * @return the length in bytes
     */
    public static int getQueueStatsLength(ProtocolVersion pv) {
        return pv == V_1_0 ? LIB_QUEUE_STATS_V0 : LIB_QUEUE_STATS_V3;
    }
}