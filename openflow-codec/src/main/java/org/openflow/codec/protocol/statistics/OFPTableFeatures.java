package org.openflow.codec.protocol.statistics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.factory.OFPActionFactory;
import org.openflow.codec.protocol.factory.OFPActionFactoryAware;
import org.openflow.codec.protocol.factory.OFPInstructionFactory;
import org.openflow.codec.protocol.factory.OFPInstructionFactoryAware;
import org.openflow.codec.protocol.statistics.table.OFPTableFeaturePropHeader;
import org.openflow.codec.util.StringByteSerializer;

/**
 * Represents an ofp_table_features structure
 *
 * @author AnilGujele
 */
public class OFPTableFeatures implements OFPStatistics, OFPInstructionFactoryAware, OFPActionFactoryAware, Serializable {
    private static final int MINIMUM_LENGTH = 64;
    private static final int MAX_TABLE_NAME_LEN = 32;

    private short length = MINIMUM_LENGTH;
    private byte tableId;
    private String name;
    private long metadataMatch;
    private long metadataWrite;
    private int config;
    private int maxEntries;
    private List<OFPTableFeaturePropHeader> properties;
    private OFPInstructionFactory instructionFactory;
    private OFPActionFactory actionFactory;

    /**
     * @return the tableId
     */
    public byte getTableId() {
        return tableId;
    }

    /**
     * @param tableId
     *            the tableId to set
     */
    public void setTableId(byte tableId) {
        this.tableId = tableId;
    }

    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     */
    public long getMetadataMatch() {
        return metadataMatch;
    }

    /**
     *
     * @param metadataMatch
     */
    public void setMetadataMatch(long metadataMatch) {
        this.metadataMatch = metadataMatch;
    }

    /**
     *
     * @return
     */
    public long getMetadataWrite() {
        return metadataWrite;
    }

    /**
     *
     * @param metadataWrite
     */
    public void setMetadataWrite(long metadataWrite) {
        this.metadataWrite = metadataWrite;
    }

    /**
     *
     * @return
     */
    public int getConfig() {
        return config;
    }

    /**
     *
     * @param config
     */
    public void setConfig(int config) {
        this.config = config;
    }

    /**
     *
     * @return
     */
    public int getMaxEntries() {
        return maxEntries;
    }

    /**
     *
     * @param maxEntries
     */
    public void setMaxEntries(int maxEntries) {
        this.maxEntries = maxEntries;
    }

    /**
     *
     * @return
     */
    public List<OFPTableFeaturePropHeader> getProperties() {
        return properties;
    }

    /**
     *
     * @param properties
     */
    public void setProperties(List<OFPTableFeaturePropHeader> properties) {
        this.properties = properties;
        updateLength();
    }

    /**
     * update length TODO: All the associated properties are 8 byte (64 bits)
     * aligned. Length of each property excludes the padding, if we sum up each
     * associated property length it will give us length excluding the total
     * bytes of padding added to make it align. We assume that table feature
     * response message switch will send in response to OFPMP_TABLE_FEATURES
     * request, will also consider padding in the message lengh. Something to
     * cross check with switch implementation.
     */
    private void updateLength() {
        length = MINIMUM_LENGTH;
        for (OFPTableFeaturePropHeader prop : properties) {
            int len = prop.getLengthU();
            /* Add the aligned length, including padding */
            length += len + (8 - (len % 8));

        }
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public void readFrom(IDataBuffer data) {

        this.length = data.getShort();
        this.tableId = data.get();
        data.get(); // pad
        data.getInt(); // pad
        this.name = StringByteSerializer.readFrom(data, MAX_TABLE_NAME_LEN);
        this.metadataMatch = data.getLong();
        this.metadataWrite = data.getLong();
        this.config = data.getInt();
        this.maxEntries = data.getInt();
        int propLength = this.length - OFPTableFeatures.MINIMUM_LENGTH;
        this.properties = readTableFeatureProp(data, propLength);

    }

    /**
     * read table feature property type
     *
     * @param data
     * @param length
     * @return
     */
    private List<OFPTableFeaturePropHeader> readTableFeatureProp(IDataBuffer data, int length) {
        List<OFPTableFeaturePropHeader> results = null;
        OFPTableFeaturePropHeader tempProp = new OFPTableFeaturePropHeader();
        OFPTableFeaturePropHeader ofTFProp;
        int end = data.position() + length;

        while (data.position() <= end) {
            if (data.remaining() < OFPTableFeaturePropHeader.MINIMUM_LENGTH
                    || (data.position() + OFPTableFeaturePropHeader.MINIMUM_LENGTH) > end)
                return results;

            // to read property type
            data.mark();
            tempProp.readFrom(data);
            data.reset();

            if (tempProp.getLengthU() > data.remaining() || (data.position() + tempProp.getLengthU()) > end)
                return results;
            // create instance of porperty type
            ofTFProp = tempProp.getOFTableFeaturePropType().newInstance();
            if (ofTFProp instanceof OFPInstructionFactoryAware) {
                ((OFPInstructionFactoryAware) ofTFProp).setInstructionFactory(this.instructionFactory);
            }
            if (ofTFProp instanceof OFPActionFactoryAware) {
                ((OFPActionFactoryAware) ofTFProp).setActionFactory(this.actionFactory);
            }
            ofTFProp.readFrom(data);
            if (null == results) {
                results = new ArrayList<OFPTableFeaturePropHeader>();
            }
            results.add(ofTFProp);
        }

        return results;
    }

    @Override
    public void writeTo(IDataBuffer data) {

        data.putShort(length);
        data.put(this.tableId);
        data.put((byte) 0); // pad
        data.putInt(0); // pad
        StringByteSerializer.writeTo(data, MAX_TABLE_NAME_LEN, this.name);
        data.putLong(this.metadataMatch);
        data.putLong(this.metadataWrite);
        data.putInt(this.config);
        data.putInt(this.maxEntries);
        if (null != properties) {
            for (OFPTableFeaturePropHeader prop : properties) {
                prop.writeTo(data);
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 4491;
        int result = 1;
        result = prime * result + maxEntries;
        result = prime * result + config;
        result = prime * result + (int) (metadataMatch ^ (metadataMatch >>> 32));
        result = prime * result + (int) (metadataWrite ^ (metadataWrite >>> 32));
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + tableId;
        result = prime * result + length;
        result = prime * result + ((properties == null) ? 0 : properties.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof OFPTableFeatures)) {
            return false;
        }
        OFPTableFeatures other = (OFPTableFeatures) obj;
        if (maxEntries != other.maxEntries) {
            return false;
        }
        if (config != other.config) {
            return false;
        }
        if (metadataMatch != other.metadataMatch) {
            return false;
        }
        if (metadataWrite != other.metadataWrite) {
            return false;
        }
        if (length != other.length) {
            return false;
        }
        if (tableId != other.tableId) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (properties == null) {
            if (other.properties != null) {
                return false;
            }
        } else if (!properties.equals(other.properties)) {
            return false;
        }
        return true;
    }

    @Override
    public void setActionFactory(OFPActionFactory actionFactory) {
        this.actionFactory = actionFactory;

    }

    @Override
    public void setInstructionFactory(OFPInstructionFactory instructionFactory) {
        this.instructionFactory = instructionFactory;

    }
}
