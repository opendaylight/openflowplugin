/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.instr;


import org.opendaylight.of.lib.OfPacketWriter;

/**
 * Provides facilities for encoding {@link Instruction} instances.
 * <p>
 * Used by the {@link InstructionFactory}.
 *
 * @author Simon Hunt
 */
class InstructionEncoder {

    // No instantiation
    private InstructionEncoder() { }

    /** Encodes an instruction, writing it into the supplied buffer.
     * Note that this method causes the writer index of the underlying
     * {@code PacketBuffer} to be advanced by the length of the instruction.
     *
     * @param ins the instruction
     * @param pkt the buffer into which the instruction is to be written
     */
    static void encodeInstruction(Instruction ins, OfPacketWriter pkt) {
        // first, write out the header..
        pkt.writeU16(ins.header.type.getCode(ins.getVersion()));
        pkt.writeU16(ins.header.length);

        // if the instruction is header-only, we are done.
        if (InstrHeader.class.isInstance(ins))
            return;

        // now deal with the payload, based on type
        switch (ins.header.type) {
            case GOTO_TABLE:
                encodeGotoTable((InstrGotoTable) ins, pkt);
                break;

            case WRITE_METADATA:
                encodeWriteMetadata((InstrWriteMetadata) ins, pkt);
                break;

            case WRITE_ACTIONS:
            case APPLY_ACTIONS:
            case CLEAR_ACTIONS:
                encodeActions((InstrAction) ins, pkt);
                break;

            case METER:
                encodeMeter((InstrMeter) ins, pkt);
                break;

            case EXPERIMENTER:
                encodeExperimenter((InstrExperimenter) ins, pkt);
                break;
        }
    }

    //=====================================================================
    // == encode specific instruction types

    // encodes a goto-table instruction
    private static void encodeGotoTable(InstrGotoTable ins, OfPacketWriter pkt) {
        pkt.write(ins.tableId);
        pkt.writeZeros(InstructionFactory.PAD_GOTO_TABLE);
    }

    // encodes a write-metadata instruction
    private static void encodeWriteMetadata(InstrWriteMetadata ins,
                                            OfPacketWriter pkt) {
        pkt.writeZeros(InstructionFactory.PAD_WR_META);
        pkt.writeLong(ins.metadata);
        pkt.writeLong(ins.mask);
    }

    // encodes an action-based instruction
    private static void encodeActions(InstrAction ins, OfPacketWriter pkt) {
        pkt.writeZeros(InstructionFactory.PAD_ACTION);
        for (Action act: ins.actions)
            ActionFactory.encodeAction(act, pkt);
    }

    // encodes a meter instruction
    private static void encodeMeter(InstrMeter ins, OfPacketWriter pkt) {
        pkt.write(ins.meterId);
    }

    // encodes an experimenter instruction
    private static void encodeExperimenter(InstrExperimenter ins,
                                           OfPacketWriter pkt) {
        pkt.writeInt(ins.id);
        // assumption is that data is a valid length (multiple of 8)
        pkt.writeBytes(ins.data);
    }

}
