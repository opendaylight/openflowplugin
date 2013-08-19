/**
 * @author Yugandhar Sarraju (ysarraju@in.ibm.com) - July 20, 2013
 */
package org.openflow.codec.protocol.action;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.util.U32;

/**
 * Represents an action struct ofp_action_output
 */
public class OFPActionOutput extends OFPAction implements Cloneable {
    public static int MINIMUM_LENGTH = 16;

    public enum OFControllerMaxLength {

        OFPCML_MAX((short) 0xffe5), OFPCML_NO_BUFFER((short) 0xffff);

        protected short value;

        private OFControllerMaxLength(short value) {
            this.value = value;
        }

        /**
         * @return the value
         */
        public short getValue() {
            return value;
        }
    }

    protected int port;
    protected short maxLength;

    public OFPActionOutput() {
        super.setType(OFPActionType.OUTPUT);
        super.setLength((short) MINIMUM_LENGTH);
    }

    public OFPActionOutput(int port, short maxLength) {
        super();
        super.setType(OFPActionType.OUTPUT);
        super.setLength((short) MINIMUM_LENGTH);
        this.port = port;
        this.maxLength = maxLength;
    }

    /**
     * Get the output port
     *
     * @return
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Set the output port
     *
     * @param port
     */
    public OFPActionOutput setPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * Get the max length to send to the controller
     *
     * @return
     */
    public short getMaxLength() {
        return this.maxLength;
    }

    /**
     * Set the max length to send to the controller
     *
     * @param maxLength
     */
    public OFPActionOutput setMaxLength(short maxLength) {
        this.maxLength = maxLength;
        return this;
    }

    @Override
    public void readFrom(IDataBuffer data) {
        super.readFrom(data);
        this.port = data.getInt();
        this.maxLength = data.getShort();
        data.getShort();
        data.getInt();
    }

    @Override
    public void writeTo(IDataBuffer data) {
        super.writeTo(data);
        data.putInt(port);
        data.putShort(maxLength);
        data.putShort((short) 0);
        data.putInt(0);

    }

    @Override
    public int hashCode() {
        final int prime = 367;
        int result = super.hashCode();
        result = prime * result + maxLength;
        result = prime * result + port;
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
        if (!(obj instanceof OFPActionOutput)) {
            return false;
        }
        OFPActionOutput other = (OFPActionOutput) obj;
        if (maxLength != other.maxLength) {
            return false;
        }
        if (port != other.port) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "OFPActionOutput [maxLength=" + maxLength + ", port=" + U32.f(port) + ", length=" + length + ", type="
                + type + "]";
    }
}