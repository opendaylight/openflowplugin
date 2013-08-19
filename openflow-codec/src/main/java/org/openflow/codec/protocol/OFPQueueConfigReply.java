package org.openflow.codec.protocol;

import java.util.ArrayList;
import java.util.List;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.factory.OFPQueuePropertyFactory;
import org.openflow.codec.protocol.factory.OFPQueuePropertyFactoryAware;
import org.openflow.codec.protocol.queue.OFPPacketQueue;
import org.openflow.codec.util.U16;

/**
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class OFPQueueConfigReply extends OFPMessage implements Cloneable, OFPQueuePropertyFactoryAware {
    public static int MINIMUM_LENGTH = 16;

    protected OFPQueuePropertyFactory queuePropertyFactory;

    protected int port;
    protected List<OFPPacketQueue> queues;

    /**
     *
     */
    public OFPQueueConfigReply() {
        super();
        this.type = OFPType.QUEUE_CONFIG_REPLY;
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
    public OFPQueueConfigReply setPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * @return the queues
     */
    public List<OFPPacketQueue> getQueues() {
        return queues;
    }

    /**
     * @param queues
     *            the queues to set
     */
    public void setQueues(List<OFPPacketQueue> queues) {
        this.queues = queues;
    }

    @Override
    public void readFrom(IDataBuffer data) {
        super.readFrom(data);
        this.port = data.getInt();
        data.getInt(); // pad
        int remaining = this.getLengthU() - MINIMUM_LENGTH;
        if (data.remaining() < remaining)
            remaining = data.remaining();
        this.queues = new ArrayList<OFPPacketQueue>();
        while (remaining >= OFPPacketQueue.MINIMUM_LENGTH) {
            OFPPacketQueue queue = new OFPPacketQueue();
            queue.setQueuePropertyFactory(this.queuePropertyFactory);
            queue.readFrom(data);
            remaining -= U16.f(queue.getLength());
            this.queues.add(queue);
        }
    }

    @Override
    public void writeTo(IDataBuffer data) {
        super.writeTo(data);
        data.putInt(this.port);
        data.putInt(0); // pad
        if (this.queues != null) {
            for (OFPPacketQueue queue : this.queues) {
                queue.writeTo(data);
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 4549;
        int result = super.hashCode();
        result = prime * result + port;
        result = prime * result + ((queues == null) ? 0 : queues.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof OFPQueueConfigReply))
            return false;
        OFPQueueConfigReply other = (OFPQueueConfigReply) obj;
        if (port != other.port)
            return false;
        if (queues == null) {
            if (other.queues != null)
                return false;
        } else if (!queues.equals(other.queues))
            return false;
        return true;
    }

    @Override
    public void setQueuePropertyFactory(OFPQueuePropertyFactory queuePropertyFactory) {
        this.queuePropertyFactory = queuePropertyFactory;
    }

    @Override
    public OFPQueueConfigReply clone() {
        try {
            OFPQueueConfigReply clone = (OFPQueueConfigReply) super.clone();
            if (this.queues != null) {
                List<OFPPacketQueue> queues = new ArrayList<OFPPacketQueue>();
                for (OFPPacketQueue queue : this.queues) {
                    queues.add(queue.clone());
                }
                clone.setQueues(queues);
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "OFPQueueConfigReply [port=" + port + ", queues=" + queues + ", xid=" + xid + "]";
    }
}
