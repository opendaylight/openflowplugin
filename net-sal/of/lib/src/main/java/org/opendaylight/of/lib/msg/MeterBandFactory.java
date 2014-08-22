/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.*;
import org.opendaylight.util.ResourceUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static org.opendaylight.of.lib.CommonUtils.notNull;
import static org.opendaylight.of.lib.CommonUtils.verMin13;
import static org.opendaylight.util.PrimitiveUtils.verifyU32;
import static org.opendaylight.util.PrimitiveUtils.verifyU8;

/**
 * Provides facilities for parsing, creating and encoding {@link MeterBand}
 * instances.
 *
 * @author Simon Hunt
 */
public class MeterBandFactory extends AbstractFactory {

    private static final int BAND_LEN = 16;

    private static final int PAD_BAND_DROP = 4;
    private static final int PAD_BAND_DSCP = 3;


    private static final MeterBandFactory MBF = new MeterBandFactory();

    // no instantiation except here
    private MeterBandFactory() { }

    /** Returns an identifying tag for the meter band factory.
     *
     * @return an identifying tag
     */
    @Override
    protected String tag() {
        return "MBF";
    }


    //======================================================================
    // === Parsing Meter Bands

    /** Parses a list of meter band structures from the supplied buffer.
     * The caller must calculate and specify the target reader index of
     * the buffer that marks the end of the list, so we know when to stop.
     *  <p>
     *  Note that this method causes the reader index of the underlying
     *  {@code PacketBuffer} to be advanced by the length of the list,
     *  which should leave the reader index at {@code targetRi}.
     *  <p>
     *  This method delegates to {@link #parseMeterBand} for each
     *  individual band.
     *
     * @param targetRi the target reader index
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return a list of parsed meter bands
     * @throws MessageParseException if unable to parse the structure
     */
    public static List<MeterBand> parseMeterBandList(int targetRi,
                                                     OfPacketReader pkt,
                                                     ProtocolVersion pv)
            throws MessageParseException {
        List<MeterBand> bandList = new ArrayList<MeterBand>();
        while(pkt.ri() < targetRi) {
            MeterBand mb = parseMeterBand(pkt, pv);
            bandList.add(mb);
        }
        return bandList;
    }

    /** Parses a single meter band from the buffer.
     *
     * @param pkt the buffer
     * @param pv the protocol version
     * @return the instantiated meter band
     * @throws MessageParseException if there was an issue
     */
    static MeterBand parseMeterBand(OfPacketReader pkt, ProtocolVersion pv)
            throws MessageParseException {
        try {
            MeterBand.Header header = parseBandHeader(pkt, pv);
            return createParsedBandInstance(header, pkt, pv);
        } catch (Exception e) {
            throw MBF.mpe(pkt, e);
        }
    }

    /** Parses a meter band header from the buffer.
     *
     * @param pkt the buffer
     * @param pv the protocol version
     * @return the parsed band header
     * @throws MessageParseException if there is an issue parsing the header
     * @throws DecodeException if there is an issue decoding the band type
     */
    private static MeterBand.Header parseBandHeader(OfPacketReader pkt,
                                                    ProtocolVersion pv)
        throws MessageParseException, DecodeException {
        MeterBand.Header hdr = new MeterBand.Header();
        int code = pkt.readU16();
        hdr.type = MeterBandType.decode(code, pv);
        hdr.length = pkt.readU16();
        hdr.rate = pkt.readU32();
        hdr.burstSize = pkt.readU32();
        return hdr;
    }

    private static MeterBand createParsedBandInstance(MeterBand.Header header,
                                                      OfPacketReader pkt,
                                                      ProtocolVersion pv) {
        MeterBand mb = null;
        switch (header.type) {
            case DROP:
                mb = readDrop(new MeterBandDrop(pv, header), pkt);
                break;
            case DSCP_REMARK:
                mb = readDscpRemark(new MeterBandDscpRemark(pv, header), pkt);
                break;
            case EXPERIMENTER:
                mb = readExper(new MeterBandExperimenter(pv, header), pkt);
                break;
        }
        return mb;
    }

    private static MeterBand readDrop(MeterBandDrop band,
                                      OfPacketReader pkt) {
        pkt.skip(PAD_BAND_DROP);
        return band;
    }

    private static MeterBand readDscpRemark(MeterBandDscpRemark band,
                                            OfPacketReader pkt) {
        band.precLevel = pkt.readU8();
        pkt.skip(PAD_BAND_DSCP);
        return band;
    }

    private static MeterBand readExper(MeterBandExperimenter band,
                                            OfPacketReader pkt) {
        band.id = pkt.readInt();
        return band;
    }

    //======================================================================
    // === Creating Meter Bands

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            MeterBandFactory.class, "meterBandFactory");

    private static final String E_UNEX_TYPE = RES.getString("e_unex_type");

    /** Create a meter band header.
     *
     * @param type the meter band type
     * @param rate the rate
     * @param burstSize the burst size
     * @return the header
     * @throws IllegalArgumentException if rate or burstSize are not u16
     */
    private static MeterBand.Header createHeader(MeterBandType type,
                                                 long rate, long burstSize) {
        verifyU32(rate);
        verifyU32(burstSize);
        MeterBand.Header hdr = new MeterBand.Header();
        hdr.type = type;
        hdr.length = BAND_LEN;
        hdr.rate = rate;
        hdr.burstSize = burstSize;
        return hdr;
    }

    /** Creates a DROP meter band.
     *
     * @param pv the protocol version
     * @param type the meter band type (DROP)
     * @param rate the rate for dropping packets (u32)
     * @param burstSize the size of bursts (u32)
     * @return the meter band instance
     * @throws VersionNotSupportedException if the version is not supported
     * @throws VersionMismatchException if pv &lt; 1.3
     * @throws NullPointerException if pv or type is null
     * @throws IllegalArgumentException if any argument is inappropriate
     */
    public static MeterBand createBand(ProtocolVersion pv, MeterBandType type,
                                       long rate, long burstSize) {
        notNull(pv, type);
        verMin13(pv);
        MessageFactory.checkVersionSupported(pv);
        if (type != MeterBandType.DROP)
            throw new IllegalArgumentException(E_UNEX_TYPE + type);
        MeterBand.Header hdr = createHeader(type, rate, burstSize);
        return new MeterBandDrop(pv, hdr);
    }

    /** Creates a DSCP_REMARK or EXPERIMENTER meter band.
     *
     * @param pv the protocol version
     * @param type the meter band type (DSCP_REMARK or EXPERIMENTER)
     * @param rate the rate (u32)
     * @param burstSize the size of bursts (u32)
     * @param value either the number of precedence levels to subtract
     *              (DSCP_REMARK) or experimenter encoded id (EXPERIMENTER)
     * @return the meter band instance
     * @throws VersionNotSupportedException if the version is not supported
     * @throws VersionMismatchException if pv &lt; 1.3
     * @throws NullPointerException if pv or type is null
     * @throws IllegalArgumentException if any argument is inappropriate
     */
    public static MeterBand createBand(ProtocolVersion pv, MeterBandType type,
                                 long rate, long burstSize, int value) {
        notNull(pv, type);
        MessageFactory.checkVersionSupported(pv);
        MeterBand.Header hdr = createHeader(type, rate, burstSize);
        MeterBand band;
        switch (type) {
            case DSCP_REMARK:
                verifyU8(value);
                band = new MeterBandDscpRemark(pv, hdr);
                ((MeterBandDscpRemark) band).precLevel = value;
                break;
            case EXPERIMENTER:
                band = new MeterBandExperimenter(pv, hdr);
                ((MeterBandExperimenter) band).id = value;
                break;
            default:
                throw new IllegalArgumentException(E_UNEX_TYPE + type);
        }
        return band;
    }

    /** Creates an EXPERIMENTER meter band.
     *
     * @param pv the protocol version
     * @param type the meter band type (EXPERIMENTER)
     * @param rate the rate (u32)
     * @param burstSize the size of bursts (u32)
     * @param eid the experimenter id
     * @return the meter band instance
     * @throws VersionNotSupportedException if the version is not supported
     * @throws VersionMismatchException if pv &lt; 1.3
     * @throws NullPointerException if pv, type or eid is null
     * @throws IllegalArgumentException if any argument is inappropriate
     */
    public static MeterBand createBand(ProtocolVersion pv, MeterBandType type,
                                       long rate, long burstSize,
                                       ExperimenterId eid) {
        return createBand(pv, type, rate, burstSize, eid.encodedId());
    }

    //======================================================================
    // === Encoding Meter Bands

    /** Encodes a meter band, writing it into the supplied buffer.
     * Note that this method causes the writer index of the underlying
     * {@code PacketBuffer} to be advanced by the length of the band.
     *
     * @param band the meter band
     * @param pkt the buffer into which the band is to be written
     */
    private static void encodeBand(MeterBand band, OfPacketWriter pkt) {
        MeterBandType type = band.header.type;

        // start with header
        pkt.writeU16(type.getCode(band.getVersion()));
        pkt.writeU16(band.header.length);
        pkt.writeU32(band.header.rate);
        pkt.writeU32(band.header.burstSize);

        // now, switch on type
        switch (type) {
            case DROP:
                pkt.writeZeros(PAD_BAND_DROP);
                break;
            case DSCP_REMARK:
                pkt.writeU8(((MeterBandDscpRemark) band).precLevel);
                pkt.writeZeros(PAD_BAND_DSCP);
                break;
            case EXPERIMENTER:
                pkt.writeInt(((MeterBandExperimenter) band).id);
                break;
        }
    }

    /** Encodes a list of meter bands, writing them into the supplied buffer.
     * Note that this method causes the writer index of the underlying
     * {@code PacketBuffer} to be advanced by the length of the written bands.
     *
     * @param bands the list of meter bands
     * @param pkt the buffer into which the bands are to be written
     */
    public static void encodeBandList(List<MeterBand> bands, OfPacketWriter pkt) {
        for (MeterBand band: bands)
            encodeBand(band, pkt);
    }

    /**
     * Returns the length for the given meter band, in bytes.
     *
     * @param band the target band
     * @return the length of the band
     * @throws NullPointerException if the band is null
     */
    public static int getLength(MeterBand band) {
        return band.header.length;
    }
}