/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.instr;

import org.opendaylight.of.lib.MessageParseException;
import org.opendaylight.of.lib.OfPacketReader;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.util.ResourceUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static org.opendaylight.of.lib.instr.Instruction.Header;
import static org.opendaylight.of.lib.instr.InstructionFactory.*;

/**
 * Provides facilities for parsing {@link Instruction} instances.
 * <p>
 * Used by the {@link InstructionFactory}.
 *
 * @author Simon Hunt
 */
class InstructionParser {

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            InstructionParser.class, "instructionParser");

    private static final String E_OFF_BY = RES.getString("e_off_by");

    // No instantiation
    private InstructionParser() { }

    private static void verifyTargetRi(int targetRi, OfPacketReader pkt)
            throws MessageParseException {
        if (pkt.ri() != targetRi) {
            int offby = pkt.ri() - targetRi;
            throw IF.mpe(pkt, E_OFF_BY + offby);
        }
    }

    /** Parses a list of instruction structures from the supplied buffer.
     *  The caller must calculate and specify the target reader index of
     *  the buffer that marks the end of the list, so we know when to stop.
     *  <p>
     *  Note that this method causes the reader index of the underlying
     *  {@code PacketBuffer} to be advanced by the length of the list,
     *  which should leave the reader index at {@code targetRi}.
     *  <p>
     *  This method delegates to {@link #parseInstruction} for each individual
     *  instruction.
     *
     * @param targetRi the target reader index
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return a list of parsed instructions
     * @throws MessageParseException if unable to parse the structure
     */
    static List<Instruction> parseInstructionList(int targetRi,
                                                  OfPacketReader pkt,
                                                  ProtocolVersion pv)
            throws MessageParseException {
        List<Instruction> insList = new ArrayList<Instruction>();
        while (pkt.ri() < targetRi) {
            Instruction ins = parseInstruction(pkt, pv);
            insList.add(ins);
        }
        verifyTargetRi(targetRi, pkt);
        return insList;
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
    static Instruction parseInstruction(OfPacketReader pkt, ProtocolVersion pv)
            throws MessageParseException {
        try {
            pkt.resetOdometer();
            Header header = Instruction.parseHeader(pkt, pv);
            return createParsedInstrInstance(header, pkt, pv);
        } catch (MessageParseException mpe) {
            // rethrow MPE
            throw mpe;
        } catch (Exception e) {
            // wrap any unexpected exception in an MPE
            throw IF.mpe(pkt, e);
        }
    }

    /** Parses a list of instruction header structures from the supplied
     * buffer. This method is provided to support the parsing of an
     * "instruction" table feature property.
     * The list returned contains either
     * {@link InstrHeader} instances or {@link InstrExperimenter} instances.
     *
     * @see org.opendaylight.of.lib.msg.TableFeaturePropInstr
     *
     * @param targetRi the target reader index
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return a list of parsed instructions (header info only)
     * @throws MessageParseException if unable to parse the structure
     */
    static List<Instruction> parseInstructionHeaders(int targetRi,
                                                     OfPacketReader pkt,
                                                     ProtocolVersion pv)
            throws MessageParseException {
        List<Instruction> insList = new ArrayList<Instruction>();
        while (pkt.ri() < targetRi) {
            Instruction ins = parseInstructionHeader(pkt, pv);
            insList.add(ins);
        }
        verifyTargetRi(targetRi, pkt);
        return insList;
    }

    /** Parses an Instruction structure (header only) from the supplied buffer.
     * Note that this method causes the reader index of the underlying
     * {@code PacketBuffer} to be advanced by the length of the structure.
     *
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return a parsed instruction structure (header info only)
     * @throws MessageParseException if unable to parse the structure
     */
    private static Instruction parseInstructionHeader(OfPacketReader pkt,
                                                      ProtocolVersion pv)
            throws MessageParseException {
        try {
            Header header = Instruction.parseHeader(pkt, pv, false);
            Instruction result;
            switch (header.type) {
                case EXPERIMENTER:
                    result = exper(new InstrExperimenter(pv, header), pkt);
                    break;
                
                default:
                    // all non-experimenter instructions should consist of
                    // a u16 type and a u16 length field set to 4.
                    if (header.length != INSTR_HEADER_LEN)
                        throw IF.mpe(pkt, E_UNEXPECTED_HEADER_LENGTH);
                    result = new InstrHeader(pv, header);
                    break;
            }
            return result;
        } catch (Exception e) {
            // wrap any unexpected exception in an MPE
            throw IF.mpe(pkt, e);
        }
    }

    /** Uses Instruction header to instantiate the appropriate class of
     * Instruction structure.
     *
     * @param header the instruction header
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return the fully parsed instruction structure
     * @throws MessageParseException if there is an issue parsing
     *          the instruction
     */
    private static Instruction createParsedInstrInstance(Header header,
                                                         OfPacketReader pkt,
                                                         ProtocolVersion pv)
            throws MessageParseException {
        Instruction result = null;
        switch (header.type) {
            case GOTO_TABLE:
                result = gotoTable(new InstrGotoTable(pv, header), pkt);
                break;
            case WRITE_METADATA:
                result = writeMetadata(new InstrWriteMetadata(pv, header), pkt);
                break;
            case WRITE_ACTIONS:
                result = readActions(new InstrWriteActions(pv, header), pkt);
                break;
            case APPLY_ACTIONS:
                result = readActions(new InstrApplyActions(pv, header), pkt);
                break;
            case CLEAR_ACTIONS:
                result = readActions(new InstrClearActions(pv, header), pkt);
                if ( !((InstrClearActions)result).actions.isEmpty() )
                    throw IF.mpe(pkt, E_CLEAR_WITH_ACTIONS);
                break;
            case METER:
                result = meter(new InstrMeter(pv, header), pkt);
                break;
            case EXPERIMENTER:
                result = exper(new InstrExperimenter(pv, header), pkt);
                break;
        }

        // check that the number of bytes read from the buffer is multiple of 8
        int bytesRead = pkt.odometer();
        if (bytesRead % 8 != 0)
            throw IF.mpe(pkt, E_NOT_DIV_BY_8 + bytesRead);
        return result;
    }


    // Completes parsing a GOTO_TABLE instruction.
    private static Instruction gotoTable(InstrGotoTable instr,
                                         OfPacketReader pkt) {
        instr.tableId = pkt.readTableId();
        pkt.skip(PAD_GOTO_TABLE);
        return instr;
    }

    // Completes parsing a WRITE_METADATA instruction.
    private static Instruction writeMetadata(InstrWriteMetadata instr,
                                             OfPacketReader pkt) {
        pkt.skip(PAD_WR_META);
        instr.metadata = pkt.readLong();
        instr.mask = pkt.readLong();
        return instr;
    }

    // Completes parsing an action-list based instruction.
    private static Instruction readActions(InstrAction instr, OfPacketReader pkt)
            throws MessageParseException {
        final int targetRi = pkt.ri() + instr.header.length - INSTR_HEADER_LEN;
        final ProtocolVersion pv = instr.getVersion();
        pkt.skip(PAD_ACTION);
        while (pkt.ri() < targetRi) {
            Action act = ActionFactory.parseAction(pkt, pv);
            instr.actions.add(act);
        }
        return instr;
    }

    // Completes parsing a METER instruction.
    private static Instruction meter(InstrMeter instr, OfPacketReader pkt) {
        instr.meterId = pkt.readMeterId();
        return instr;
    }

    // Completes parsing an EXPERIMENTER instruction.
    private static Instruction exper(InstrExperimenter instr,
                                     OfPacketReader pkt) {
        instr.id = pkt.readInt();
        int dataBytes = instr.header.length - INSTR_HEADER_LEN - EXP_ID_LEN;
        instr.data = pkt.readBytes(dataBytes);
        return instr;
    }
}