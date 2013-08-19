package org.openflow.codec.protocol.queue;

import java.util.ArrayList;
import java.util.List;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.factory.OFPQueuePropertyFactory;
import org.openflow.codec.protocol.factory.OFPQueuePropertyFactoryAware;
import org.openflow.codec.util.U16;

/**
 * Corresponds to the struct ofp_packet_queue OpenFlow structure
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class OFPPacketQueue implements Cloneable, OFPQueuePropertyFactoryAware {
    public static int MINIMUM_LENGTH = 16;

    protected OFPQueuePropertyFactory queuePropertyFactory;

    protected int queueId;
    private int port;
    protected short length;
    protected List<OFPQueueProperty> properties;

    /**
     * @return the queueId
     */
    public int getQueueId() {
        return queueId;
    }

    /**
     * @param queueId
     *            the queueId to set
     */
    public OFPPacketQueue setQueueId(int queueId) {
        this.queueId = queueId;
        return this;
    }

    /**
     * @return the length
     */
    public short getLength() {
        return length;
    }

    /**
     * @param length
     *            the length to set
     */
    public void setLength(short length) {
        this.length = length;
    }

    /**
     * @return the properties
     */
    public List<OFPQueueProperty> getProperties() {
        return properties;
    }

    /**
     * @param properties
     *            the properties to set
     */
    public OFPPacketQueue setProperties(List<OFPQueueProperty> properties) {
        this.properties = properties;
        return this;
    }

    public void readFrom(IDataBuffer data) {
        this.queueId = data.getInt();
        this.port = data.getInt();
        this.length = data.getShort();
        data.getInt(); // pad
        data.getShort(); // pad

        if (this.queuePropertyFactory == null)
            throw new RuntimeException("OFPQueuePropertyFactory not set");
        this.properties = queuePropertyFactory.parseQueueProperties(data, U16.f(this.length) - MINIMUM_LENGTH);
    }

    public void writeTo(IDataBuffer data) {
        data.putInt(this.queueId);
        data.putInt(this.port);
        data.putShort(this.length);
        data.putInt(0); // pad
        data.putShort((short) 0); // pad
        if (this.properties != null) {
            for (OFPQueueProperty queueProperty : this.properties) {
                queueProperty.writeTo(data);
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 6367;
        int result = 1;
        result = prime * result + length;
        result = prime * result + ((properties == null) ? 0 : properties.hashCode());
        result = prime * result + queueId;
        result = prime * result + port;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof OFPPacketQueue))
            return false;
        OFPPacketQueue other = (OFPPacketQueue) obj;
        if (length != other.length)
            return false;
        if (properties == null) {
            if (other.properties != null)
                return false;
        } else if (!properties.equals(other.properties))
            return false;
        if (queueId != other.queueId)
            return false;
        if (port != other.port)
            return false;

        return true;
    }

    @Override
    public OFPPacketQueue clone() {
        try {
            OFPPacketQueue clone = (OFPPacketQueue) super.clone();
            if (this.properties != null) {
                List<OFPQueueProperty> queueProps = new ArrayList<OFPQueueProperty>();
                for (OFPQueueProperty prop : this.properties) {
                    queueProps.add(prop.clone());
                }
                clone.setProperties(queueProps);
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setQueuePropertyFactory(OFPQueuePropertyFactory queuePropertyFactory) {
        this.queuePropertyFactory = queuePropertyFactory;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "OFPPacketQueue [queueId=" + queueId + ", port=" + port + ", properties=" + properties + "]";
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
