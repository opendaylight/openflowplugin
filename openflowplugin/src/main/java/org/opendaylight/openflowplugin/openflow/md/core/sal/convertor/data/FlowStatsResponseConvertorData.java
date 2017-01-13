package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data;


import java.math.BigInteger;

public class FlowStatsResponseConvertorData extends VersionDatapathIdConvertorData {
    private BigInteger datapathId;

    public Boolean getRpcStats() {
        return rpcStats;
    }

    public void setRpcStats(Boolean rpcStats) {
        this.rpcStats = rpcStats;
    }

    private Boolean rpcStats;

    public FlowStatsResponseConvertorData(short version) {
        super(version);
    }

    @Override
    public BigInteger getDatapathId() {
        return datapathId;
    }

    @Override
    public void setDatapathId(BigInteger datapathId) {
        this.datapathId = datapathId;
    }
}
