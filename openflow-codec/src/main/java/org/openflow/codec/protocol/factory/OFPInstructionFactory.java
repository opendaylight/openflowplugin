package org.openflow.codec.protocol.factory;

import java.util.List;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.instruction.OFPInstruction;
import org.openflow.codec.protocol.instruction.OFPInstructionType;

/**
 * The interface to factories used for retrieving OFPInstruction instances. All
 * methods are expected to be thread-safe.
 *
 * @author AnilGujele
 */
public interface OFPInstructionFactory {
    /**
     * Retrieves an OFPInstruction instance corresponding to the specified
     * OFPInstructionType
     *
     * @param t
     *            the type of the OFPInstruction to be retrieved
     * @return an OFPInstruction instance
     */
    public OFPInstruction getInstruction(OFPInstructionType t);

    /**
     * Attempts to parse and return all OFInstructions contained in the given
     * DataBuffer, beginning at the DataBuffer's position, and ending at
     * position+length.
     *
     * @param data
     *            the DataBuffer to parse for OpenFlow Instructions
     * @param length
     *            the number of Bytes to examine for OpenFlow Instructions
     * @return a list of OFPInstruction instances
     */
    public List<OFPInstruction> parseInstructions(IDataBuffer data, int length);

    /**
     * Attempts to parse and return number of specified OFInstructions contained
     * in the given DataBuffer, beginning at the DataBuffer's position, and
     * ending at position+length.
     *
     * @param data
     *            the DataBuffer to parse for OpenFlow Instructions
     * @param length
     *            the number of Bytes to examine for OpenFlow Instructions
     * @param limit
     *            the maximum number of messages to return, 0 means no limit
     * @return a list of OFPInstruction instances
     */
    public List<OFPInstruction> parseInstructions(IDataBuffer data, int length, int limit);
}
