package org.opendaylight.openflowplugin.test.ForwardingConsumer;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by brent on 6/16/14.
 */
public class FlowClientParam {
    static final Logger logger = LoggerFactory.getLogger(FlowClientParam.class);

    private Short tableID;
    private int hardTimeout;
    private int softTimeout;
    private FlowModFlags flowModFlags;
    private FlowId flowId;
    private FlowKey flowKey;
    private String flowName;
    private FlowCookie flowCookie;
    private FlowCookie FlowCookieMask;
    private Boolean installHW;
    private Boolean strict;
    private Integer priority;

    public FlowClientParam() {
        this.hardTimeout = 0;
        this.softTimeout = 0;
        this.strict = true;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Boolean getStrict() {
        return strict;
    }

    public void setStrict(Boolean strict) {
        this.strict = strict;
    }

    public int getTableID() {
        return tableID;
    }

    public void setTableID(Short tableID) {
        this.tableID = tableID;
    }

    public int getHardTimeout() {
        return hardTimeout;
    }

    public void setHardTimeout(int hardTimeout) {
        this.hardTimeout = hardTimeout;
    }

    public int getSoftTimeout() {
        return softTimeout;
    }

    public void setSoftTimeout(int softTimeout) {
        this.softTimeout = softTimeout;
    }

    public FlowModFlags getFlowModFlags() {
        return flowModFlags;
    }

    public void setFlowModFlags(FlowModFlags flowModFlags) {
        this.flowModFlags = flowModFlags;
    }

    public FlowId getFlowId() {
        return flowId;
    }

    public void setFlowId(FlowId flowId) {
        this.flowId = flowId;
    }

    public FlowKey getFlowKey() {
        return flowKey;
    }

    public void setFlowKey(FlowKey flowKey) {
        this.flowKey = flowKey;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public FlowCookie getFlowCookie() {
        return flowCookie;
    }

    public void setFlowCookie(FlowCookie flowCookie) {
        this.flowCookie = flowCookie;
    }

    public FlowCookie getFlowCookieMask() {
        return FlowCookieMask;
    }

    public void setFlowCookieMask(FlowCookie flowCookieMask) {
        FlowCookieMask = flowCookieMask;
    }

    public Boolean getInstallHW() {
        return installHW;
    }

    public void setInstallHW(Boolean installHW) {
        this.installHW = installHW;
    }

    public FlowBuilder buildClientFlow(FlowBuilder flowBuilder, FlowClientParam flowClientParam) {

        if (flowClientParam.getPriority() != null) {
            flowBuilder.setPriority(flowClientParam.getPriority());
        }
        if (flowClientParam.tableID != null) {
            flowBuilder.setTableId(flowClientParam.tableID);
        }
        // TODO: Complete Field Conversions
        return flowBuilder;
    }


    @Override
    public String toString() {
        return "FlowClient{" +
                "tableID=" + tableID +
                ", hardTimeout=" + hardTimeout +
                ", softTimeout=" + softTimeout +
                ", flowModFlags=" + flowModFlags +
                ", flowId=" + flowId +
                ", flowKey=" + flowKey +
                ", flowName='" + flowName + '\'' +
                ", flowCookie=" + flowCookie +
                ", flowCookieMask=" + FlowCookieMask +
                ", installHW=" + installHW +
                '}';
    }
}
