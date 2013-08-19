package org.openflow.codec.protocol.instruction;

import org.openflow.codec.io.IDataBuffer;

/**
 * Instruction structure for GOTO_Table correspond to struct
 * ofp_instruction_goto_table
 *
 * @author AnilGujele
 *
 */
public class OFPInstructionGoToTable extends OFPInstruction {
    private static final short MINIMUM_LENGTH = 8;

    private byte tableId;

    /**
     * constructor
     */
    public OFPInstructionGoToTable() {
        super.setOFInstructionType(OFPInstructionType.GOTO_TABLE);
        super.setLength(MINIMUM_LENGTH);
    }

    /**
     * get table id
     *
     * @return
     */
    public byte getTableId() {
        return tableId;
    }

    /**
     * set table id
     *
     * @param tableId
     */
    public void setTableId(byte tableId) {
        this.tableId = tableId;
    }

    /**
     * read OFPInstruction from buffer
     *
     * @param data
     */
    public void readFrom(IDataBuffer data) {
        super.readFrom(data);
        this.tableId = data.get();
        data.get(); // pad
        data.getShort(); // pad

    }

    /**
     * write OFPInstruction to buffer
     *
     * @param data
     */
    public void writeTo(IDataBuffer data) {
        super.writeTo(data);
        data.put(this.tableId);
        data.put((byte) 0);
        data.putShort((short) 0);
    }

    @Override
    public int hashCode() {
        final int prime = 743;
        int result = super.hashCode();
        result = prime * result + this.tableId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof OFPInstructionGoToTable)) {
            return false;
        }
        OFPInstructionGoToTable other = (OFPInstructionGoToTable) obj;
        if (this.tableId != other.tableId) {
            return false;
        }
        return true;
    }

    /**
     * Returns a string representation of the instruction
     */
    public String toString() {
        return "OFPInstruction[" + "type=" + this.getOFInstructionType() + ", length=" + this.getLength()
                + ", tableId=" + tableId + "]";
    }

}
