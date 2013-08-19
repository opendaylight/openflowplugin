package org.openflow.codec.protocol;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.util.U16;

/**
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class OFPQueueConfigRequest extends OFPMessage implements Cloneable {
    public static int MINIMUM_LENGTH = 16;

    protected int port;

    /**
     *
     */
    public OFPQueueConfigRequest() {
        super();
        this.type = OFPType.QUEUE_CONFIG_REQUEST;
        this.length = U16.t(MINIMUM_LENGTH);
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port
     *            the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void readFrom(IDataBuffer data) {
        super.readFrom(data);
        this.port = data.getInt();
        data.getInt(); // pad
    }

    @Override
    public void writeTo(IDataBuffer data) {
        super.writeTo(data);
        data.putInt(this.port);
        data.putInt(0); // pad
    }

    @Override
    public int hashCode() {
        final int prime = 7211;
        int result = super.hashCode();
        result = prime * result + port;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof OFPQueueConfigRequest))
            return false;
        OFPQueueConfigRequest other = (OFPQueueConfigRequest) obj;
        if (port != other.port)
            return false;
        return true;
    }

    @Override
    public OFPQueueConfigRequest clone() {
        try {
            return (OFPQueueConfigRequest) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
