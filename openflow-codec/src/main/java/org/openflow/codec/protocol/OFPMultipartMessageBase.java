package org.openflow.codec.protocol;

import java.util.List;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.factory.OFPStatisticsFactory;
import org.openflow.codec.protocol.factory.OFPStatisticsFactoryAware;
import org.openflow.codec.protocol.statistics.OFPMultipartTypes;
import org.openflow.codec.protocol.statistics.OFPStatistics;

/**
 * Base class for Multipart requests/replies
 *
 * @author David Erickson (daviderickson@cs.stanford.edu) - Mar 27, 2010
 * @author AnilGujele
 */
public abstract class OFPMultipartMessageBase extends OFPMessage implements OFPStatisticsFactoryAware {
    public static int MINIMUM_LENGTH = 16;

    protected OFPStatisticsFactory statisticsFactory;
    protected OFPMultipartTypes multipartType;
    protected short flags;
    protected List<OFPStatistics> statistics;

    /**
     * @return the multipartType
     */
    public OFPMultipartTypes getMultipartType() {
        return multipartType;
    }

    /**
     * @param multipartType
     *            the multipartType to set
     */
    public void setMultipartType(OFPMultipartTypes multipartType) {
        this.multipartType = multipartType;
    }

    /**
     * @return the flags
     */
    public short getFlags() {
        return flags;
    }

    /**
     * @param flags
     *            the flags to set
     */
    public void setFlags(short flags) {
        this.flags = flags;
    }

    /**
     * @return the statistics
     */
    public List<OFPStatistics> getStatistics() {
        return statistics;
    }

    /**
     * @param statistics
     *            the statistics to set
     */
    public void setStatistics(List<OFPStatistics> statistics) {
        this.statistics = statistics;
    }

    @Override
    public void setStatisticsFactory(OFPStatisticsFactory statisticsFactory) {
        this.statisticsFactory = statisticsFactory;
    }

    @Override
    public void readFrom(IDataBuffer data) {
        super.readFrom(data);
        this.multipartType = OFPMultipartTypes.valueOf(data.getShort(), this.getType());
        this.flags = data.getShort();
        data.getInt(); // pad
        if (this.statisticsFactory == null)
            throw new RuntimeException("OFPStatisticsFactory not set");
        this.statistics = statisticsFactory.parseStatistics(this.getType(), this.multipartType, data,
                super.getLengthU() - MINIMUM_LENGTH);
    }

    @Override
    public void writeTo(IDataBuffer data) {
        super.writeTo(data);
        data.putShort(this.multipartType.getTypeValue());
        data.putShort(this.flags);
        data.putInt(0); // pad
        if (this.statistics != null) {
            for (OFPStatistics statistic : this.statistics) {
                statistic.writeTo(data);
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 317;
        int result = super.hashCode();
        result = prime * result + flags;
        result = prime * result + ((multipartType == null) ? 0 : multipartType.hashCode());
        result = prime * result + ((statistics == null) ? 0 : statistics.hashCode());
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
        if (!(obj instanceof OFPMultipartMessageBase)) {
            return false;
        }
        OFPMultipartMessageBase other = (OFPMultipartMessageBase) obj;
        if (flags != other.flags) {
            return false;
        }
        if (multipartType == null) {
            if (other.multipartType != null) {
                return false;
            }
        } else if (!multipartType.equals(other.multipartType)) {
            return false;
        }
        if (statistics == null) {
            if (other.statistics != null) {
                return false;
            }
        } else if (!statistics.equals(other.statistics)) {
            return false;
        }
        return true;
    }
}
