package org.openflow.codec.protocol;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.util.U16;

/**
 * Class representing message structure ofp_table_mod
 *
 * @author AnilGujele
 *
 */
public class OFPTableMod extends OFPMessage {

    private static final long serialVersionUID = -5972069012765334899L;

    public static int MINIMUM_LENGTH = 16;

    // table number as per ofp_table
    enum OFTable {
        /* Last usable table number. */
        OFPTT_MAX(0xfe),

        /* Fake tables. */
        OFPTT_ALL(0xff);

        private int value;

        OFTable(int value) {
            this.value = value;
        }

        public short value() {
            return (short) this.value;
        }
    }

    // Flags to configure the table as per ofp_table_config
    public static final int OFPTC_DEPRECATED_MASK = 3;

    private byte tableId;
    private int config;

    /**
     * constructor
     */
    public OFPTableMod() {
        super();
        this.type = OFPType.TABLE_MOD;
        this.length = U16.t(MINIMUM_LENGTH);

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
     * set table id OFPTT_ALL is for all the table OFPTT_MAX is Max table number
     * limit
     *
     * @param tableId
     */
    public void setTableId(byte tableId) {

        this.tableId = tableId;
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

    @Override
    public void readFrom(IDataBuffer data) {
        super.readFrom(data);
        this.tableId = data.get();
        data.getShort(); // pad
        data.get(); // pad
        this.config = data.getInt();
    }

    @Override
    public void writeTo(IDataBuffer data) {
        super.writeTo(data);
        data.put(this.tableId);
        data.putShort((short) 0); // pad
        data.put((byte) 0); // pad
        data.putInt(this.config);
    }

    @Override
    public int hashCode() {
        final int prime = 811;
        int result = super.hashCode();
        result = prime * result + tableId;
        result = prime * result + config;
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
        if (!(obj instanceof OFPTableMod)) {
            return false;
        }
        OFPTableMod other = (OFPTableMod) obj;
        if (tableId != other.tableId) {
            return false;
        }
        if (config != other.config) {
            return false;
        }
        return true;
    }
}
