/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.instr;

import org.opendaylight.of.lib.*;
import org.opendaylight.of.lib.dt.MeterId;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.msg.MessageFactory;
import org.opendaylight.util.ResourceUtils;
import org.opendaylight.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import static org.opendaylight.of.lib.CommonUtils.EOLI;
import static org.opendaylight.of.lib.CommonUtils.notNull;

/**
 * Provides facilities for parsing, creating and encoding {@link Instruction}
 * instances.
 *
 * @author Simon Hunt
 */
public class InstructionFactory extends AbstractFactory {

    static final int INSTR_HEADER_LEN = 4;
    static final int EXP_ID_LEN = 4;

    static final int PAD_GOTO_TABLE = 3;
    static final int PAD_WR_META = 4;
    static final int PAD_ACTION = 4;

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            InstructionFactory.class, "instructionFactory");

    static final String E_NOT_DIV_BY_8 = RES.getString("e_not_div_by_8");
    static final String E_CLEAR_WITH_ACTIONS = RES
            .getString("e_clear_with_actions");
    static final String E_UNEXPECTED_HEADER_LENGTH = RES
            .getString("e_unexpected_header_length");

    static final InstructionFactory IF = new InstructionFactory();

    // No instantiation except here
    private InstructionFactory() { }

    /** Returns an identifying tag for the instruction factory.
     *
     * @return an identifying tag
     */
    @Override
    protected String tag() {
        return "IF";
    }

    // =======================================================================
    // === Delegate to the InstructionParser to parse instructions.

    /** Parses a list of instruction structures from the supplied buffer.
     *  The caller must calculate and specify the target reader index of
     *  the buffer that marks the end of the list, so we know when to
     *  stop.
     *  <p>
     *  Note that this method causes the reader index of the underlying
     *  {@code PacketBuffer} to be advanced by the length of the list,
     *  which should leave the reader index at {@code targetRi}.
     *
     * @param targetRi the target reader index
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return a list of parsed instructions
     * @throws MessageParseException if unable to parse the structure
     */
    public static List<Instruction> parseInstructionList(int targetRi,
                                                         OfPacketReader pkt,
                                                         ProtocolVersion pv)
            throws MessageParseException {
        return InstructionParser.parseInstructionList(targetRi, pkt, pv);
    }


    /** Parses an Instruction structure from the supplied buffer.
     * Note that this method causes the reader index of the underlying
     * {@code PacketBuffer} to be advanced by the length of the structure.
     * Also note that this method resets and uses the buffer's
     * odometer, to verify that the total number of bytes parsed
     * is divisible by 8.
     *
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return a parsed instruction structure
     * @throws MessageParseException if unable to parse the structure
     */
    public static Instruction parseInstruction(OfPacketReader pkt,
                                               ProtocolVersion pv)
            throws MessageParseException {
        return InstructionParser.parseInstruction(pkt, pv);
    }

    /** Parses a list of instruction header structures from the supplied
     * buffer. This method is provided to support the parsing of an
     * "instruction" table feature property.
     * The list returned contains either
     * {@link InstrHeader} instances or {@link InstrExperimenter} instances.
     *
     * @see org.opendaylight.of.lib.msg.TableFeatureFactory
     * @see org.opendaylight.of.lib.msg.TableFeaturePropInstr
     *
     * @param targetRi the target reader index
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return a list of parsed instructions (header info only)
     * @throws MessageParseException if unable to parse the structure
     */
    public static List<Instruction> parseInstructionHeaders(int targetRi,
                                                            OfPacketReader pkt,
                                                            ProtocolVersion pv)
        throws MessageParseException {
        return InstructionParser.parseInstructionHeaders(targetRi, pkt, pv);
    }

    //======================================================================
    // === Creating Instructions

    private static final String E_UNEX_TYPE = RES.getString("e_unex_type");

    // Length-In-Bytes...
    private static final int LIB_GOTO_TABLE = 8;
    private static final int LIB_WRITE_METADATA = 24;
    private static final int LIB_METER = 8;

    /** Creates an instruction header.
     *
     * @param pv the protocol version
     * @param type the instruction type
     * @return the header
     */
    private static Instruction.Header createHeader(ProtocolVersion pv,
                                                   InstructionType type) {
        // NOTE: pv is currently unused, but may be required in future versions
        Instruction.Header header = new Instruction.Header();
        header.type = type;
        header.length = INSTR_HEADER_LEN;
        return header;
    }

    /** Creates a GOTO_TABLE instruction.
     *
     * @param pv the protocol version
     * @param type the instruction type (GOTO_TABLE)
     * @param tid the table id
     * @return the instruction
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if type is not GOTO_TABLE
     */
    public static Instruction createInstruction(ProtocolVersion pv,
                                                InstructionType type,
                                                TableId tid) {
        notNull(pv, type, tid);
        MessageFactory.checkVersionSupported(pv);
        if (type != InstructionType.GOTO_TABLE)
            throw new IllegalArgumentException(E_UNEX_TYPE + type);

        Instruction.Header hdr = createHeader(pv, type);
        InstrGotoTable ins = new InstrGotoTable(pv, hdr);
        ins.tableId = tid;
        ins.header.length = LIB_GOTO_TABLE;
        return ins;
    }

    /** Creates a WRITE_METADATA instruction.
     *
     * @param pv the protocol version
     * @param type the instruction type (WRITE_METADATA)
     * @param metadata the metadata
     * @param mask the metadata mask
     * @return the instruction
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if type is not WRITE_METADATA
     */
    public static Instruction createInstruction(ProtocolVersion pv,
                                                InstructionType type,
                                                long metadata,
                                                long mask) {
        notNull(pv, type);
        MessageFactory.checkVersionSupported(pv);
        if (type != InstructionType.WRITE_METADATA)
            throw new IllegalArgumentException(E_UNEX_TYPE + type);

        Instruction.Header hdr = createHeader(pv, type);
        InstrWriteMetadata ins = new InstrWriteMetadata(pv, hdr);
        ins.metadata = metadata;
        ins.mask = mask;
        ins.header.length = LIB_WRITE_METADATA;
        return ins;
    }

    /** Creates a mutable (action-based) instruction.
     *
     * @param pv the protocol version
     * @param type the instruction type (WRITE_ACTIONS, APPLY_ACTIONS)
     * @return the instruction
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if type is not WRITE_ACTIONS or
     *          APPLY_ACTIONS
     */
    public static InstrMutableAction
    createMutableInstruction(ProtocolVersion pv, InstructionType type) {
        notNull(pv, type);
        MessageFactory.checkVersionSupported(pv);
        Instruction.Header hdr = createHeader(pv, type);
        InstrMutableAction ins;
        switch (type) {
            case WRITE_ACTIONS:
                ins = new InstrMutableWriteActions(pv, hdr);
                break;
            case APPLY_ACTIONS:
                ins = new InstrMutableApplyActions(pv, hdr);
                break;
            default:
                throw new IllegalArgumentException(E_UNEX_TYPE + type);
        }
        ins.header.length += PAD_ACTION; // account for padding
        return ins;
    }

    /** Creates a CLEAR_ACTIONS instruction.
     * <p>
     * Note, use {@link #createMutableInstruction} for {@code WRITE_ACTIONS}
     * or {@code APPLY_ACTIONS}.
     *
     * @param pv the protocol version
     * @param type the instruction type (CLEAR_ACTIONS)
     * @return the instruction
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if type is not CLEAR_ACTIONS
     */
    public static Instruction createInstruction(ProtocolVersion pv,
                                                InstructionType type) {
        notNull(pv, type);
        MessageFactory.checkVersionSupported(pv);
        if (type != InstructionType.CLEAR_ACTIONS)
            throw new IllegalArgumentException(E_UNEX_TYPE + type);

        Instruction.Header hdr = createHeader(pv, type);
        InstrClearActions ins = new InstrClearActions(pv, hdr);
        ins.header.length += PAD_ACTION; // account for padding
        return ins;
    }

    /** Creates a METER instruction.
     *
     * @param pv the protocol version
     * @param type the instruction type (METER)
     * @param mid the meter id
     * @return the instruction
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if type is not METER
     */
    public static Instruction createInstruction(ProtocolVersion pv,
                                                InstructionType type,
                                                MeterId mid) {
        notNull(pv, type, mid);
        MessageFactory.checkVersionSupported(pv);
        if (type != InstructionType.METER)
            throw new IllegalArgumentException(E_UNEX_TYPE + type);

        Instruction.Header hdr = createHeader(pv, type);
        InstrMeter ins = new InstrMeter(pv, hdr);
        ins.meterId = mid;
        ins.header.length = LIB_METER;
        return ins;
    }

    /** Creates an EXPERIMENTER instruction. Note that the experimenter-defined
     * data must be an array whose length pads the whole instruction out to a
     * 64-bit boundary. Given the 4-byte header and the 4-byte experimenter ID,
     * this means that the array length must be a multiple of 8.
     *
     * @param pv the protocol version
     * @param type the instruction type (EXPERIMENTER)
     * @param id the experimenter encoded ID
     * @param data experimenter-defined data
     * @return the instruction
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if type is not EXPERIMENTER or if
     *          data array is an unsupported length
     */
    public static Instruction createInstruction(ProtocolVersion pv,
                                                InstructionType type,
                                                int id, byte[] data) {
        notNull(pv, type, data);
        MessageFactory.checkVersionSupported(pv);
        if (type != InstructionType.EXPERIMENTER)
            throw new IllegalArgumentException(E_UNEX_TYPE + type);
        if (data.length % 8 != 0)
            throw new IllegalArgumentException(E_NOT_DIV_BY_8 + data.length);

        Instruction.Header hdr = createHeader(pv, type);
        InstrExperimenter ins = new InstrExperimenter(pv, hdr);
        ins.id = id;
        ins.data = data.clone();
        hdr.length = INSTR_HEADER_LEN + EXP_ID_LEN + data.length;
        return ins;
    }
    /** Creates an EXPERIMENTER instruction. Note that the experimenter-defined
     * data must be an array whose length pads the whole instruction out to a
     * 64-bit boundary. Given the 4-byte header and the 4-byte experimenter ID,
     * this means that the array length must be a multiple of 8.
     *
     * @param pv the protocol version
     * @param type the instruction type (EXPERIMENTER)
     * @param eid the experimenter ID
     * @param data experimenter-defined data
     * @return the instruction
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if type is not EXPERIMENTER or if
     *          data array is an unsupported length
     */
    public static Instruction createInstruction(ProtocolVersion pv,
                                                InstructionType type,
                                                ExperimenterId eid,
                                                byte[] data) {
        return createInstruction(pv, type, eid.encodedId(), data);
    }

        //====

    /** Creates instruction headers to be used in encoding a table features
     * instructions property.
     *
     * @param pv the protocol version
     * @param types the types of headers to create
     * @return the list of headers
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     */
    public static List<Instruction>
    createInstructionHeaders(ProtocolVersion pv, Set<InstructionType> types) {
        notNull(pv, types);
        MessageFactory.checkVersionSupported(pv);
        List<Instruction> ins = new ArrayList<Instruction>(types.size());
        for (InstructionType t: types)
            ins.add(new InstrHeader(pv, createHeader(pv, t)));
        return ins;
    }

    //======================================================================
    // === Encoding Instructions

    /** Encodes an instruction, writing it into the supplied buffer.
     * Note that this method causes the writer index of the underlying
     * {@code PacketBuffer} to be advanced by the length of the instruction.
     *
     * @param ins the instruction
     * @param pkt the buffer into which the instruction is to be written
     */
    public static void encodeInstruction(Instruction ins, OfPacketWriter pkt) {
        InstructionEncoder.encodeInstruction(ins, pkt);
    }

    /** Encodes a list of instructions, writing them into the supplied
     * buffer. Note that this method causes the writer index of the
     * underlying {@code PacketBuffer} to be advanced by the length of
     * the written instructions.
     *
     * @param instrs the list of instructions
     * @param pkt the buffer into which the instructions are to be written
     */
    public static void encodeInstructionList(List<Instruction> instrs,
                                             OfPacketWriter pkt) {
        for (Instruction ins: instrs)
            encodeInstruction(ins, pkt);
    }

    /** Encodes a list of experimenter instructions, writing them into the
     * supplied buffer. Note that this method causes the writer index of the
     * underlying {@code PacketBuffer} to be advanced by the length of
     * the written instructions.
     *
     * @param instrs the list of instructions
     * @param pkt the buffer into which the instructions are to be written
     */
    public static void encodeInstrExperList(List<InstrExperimenter> instrs,
                                            OfPacketWriter pkt) {
        for (Instruction ins: instrs)
            encodeInstruction(ins, pkt);
    }


    //======================================================================
    // === Utilities

    /** Outputs a list of instructions in debug string format.
     *
     * @param indent the additional indent (number of spaces)
     * @param ins the list of instructions
     * @return a multi-line string representation of the list of instructions
     */
    public static String toDebugString(int indent, List<Instruction> ins) {
        final String indStr = EOLI + StringUtils.spaces(indent);
        StringBuilder sb = new StringBuilder();
        for (Instruction instruction: ins)
            sb.append(indStr).append(instruction.toDebugString(indent));
        return sb.toString();
    }

    /** Ensures that the specified instruction is appropriate to add to
     * a message of the specified version. If all is well, silently returns.
     * If not, throws an exception.
     *
     * @param pv the protocol version
     * @param ins the instruction to validate
     * @param msgType the message type (label)
     * @throws IllegalArgumentException if the instruction is invalid
     */
    public static void validateInstruction(ProtocolVersion pv,
                                           Instruction ins, String msgType) {
        // TODO ...
    }
}