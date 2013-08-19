package org.openflow.codec.protocol.instruction;

import org.openflow.codec.io.IDataBuffer;

/**
 * Represents WRITE_METADATA action and its corresponding struct
 * ofp_instruction_write_metadata
 *
 * @author AnilGujele
 *
 */
public class OFPInstructionWriteMetaData extends OFPInstruction {
    public static final short MINIMUM_LENGTH = 24;

    private long metadata;
    private long metadataMask;

    /**
     * constructor
     */
    public OFPInstructionWriteMetaData() {
        super.setOFInstructionType(OFPInstructionType.WRITE_METADATA);
        super.setLength(MINIMUM_LENGTH);
    }

    /**
     * read OFPInstruction from buffer
     *
     * @param data
     */
    public void readFrom(IDataBuffer data) {
        super.readFrom(data);
        data.getInt(); // pad
        this.metadata = data.getLong();
        this.metadataMask = data.getLong();
    }

    /**
     * write OFPInstruction to buffer
     *
     * @param data
     */
    public void writeTo(IDataBuffer data) {
        super.writeTo(data);
        data.putInt(0);
        data.putLong(this.metadata);
        data.putLong(this.metadataMask);
    }

    /**
     * get metadata
     *
     * @return
     */
    public long getMetadata() {
        return metadata;
    }

    /**
     * set metadata
     *
     * @param metadata
     */
    public void setMetadata(long metadata) {
        this.metadata = metadata;
    }

    /**
     * get metadata mask
     *
     * @return
     */
    public long getMetadataMask() {
        return metadataMask;
    }

    /**
     * set metadata mask
     *
     * @param metadataMask
     */
    public void setMetadataMask(long metadataMask) {
        this.metadataMask = metadataMask;
    }

    @Override
    public int hashCode() {
        final int prime = 746;
        long result = super.hashCode();
        result = prime * result + this.metadata;
        result = prime * result + this.metadataMask;
        return (int) result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof OFPInstructionWriteMetaData)) {
            return false;
        }
        OFPInstructionWriteMetaData other = (OFPInstructionWriteMetaData) obj;
        if (this.metadata != other.metadata) {
            return false;
        }
        if (this.metadataMask != other.metadataMask) {
            return false;
        }
        return true;
    }

    /**
     * Returns a string representation of the instruction
     */
    public String toString() {
        return "OFPInstruction[" + "type=" + this.getOFInstructionType() + ", length=" + this.getLength()
                + ", metadata=" + metadata + ", metadataMask=" + metadataMask + "]";
    }

}
