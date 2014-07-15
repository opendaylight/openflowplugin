package org.opendaylight.openflowplugin.test.ForwardingConsumer;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

/**
 * Created by brent on 6/16/14.
 */
public class OvsFlowParams {
    static final Logger logger = LoggerFactory.getLogger(FlowClientParam.class);

    private Short tableID;
    private Integer hardTimeout;
    private FlowModFlags flowModFlags;
    private FlowId flowId;
    private FlowKey flowKey;
    private String flowName;
    private FlowCookie flowCookie;
    private FlowCookie flowCookieMask;
    private Boolean installHW;
    private Boolean strict;
    private Integer priority;
    private Integer idleTimeout;

    public Short tableID() {
        return this.tableID;
    }

    public Integer hardTimeout() {
        return this.hardTimeout;
    }

    public Integer idleTimeout() {
        return this.idleTimeout;
    }

    public FlowModFlags flowModFlags() {
        return this.flowModFlags;
    }

    public FlowId flowId() {
        return this.flowId;
    }

    public FlowKey flowKey() {
        return this.flowKey;
    }

    public String flowName() {
        return this.flowName;
    }

    public FlowCookie flowCookie() {
        return this.flowCookie;
    }

    public FlowCookie FlowCookieMask() {
        return this.flowCookieMask;
    }

    public Boolean installHW() {
        return this.installHW;
    }

    public Boolean strict() {
        return this.strict;
    }

    public Integer priority() {
        return this.priority;
    }

    public OvsFlowParams tableID(final Short tableID) {
        this.tableID = tableID;
        return this;
    }

    public OvsFlowParams hardTimeout(final Integer hardTimeout) {
        this.hardTimeout = hardTimeout;
        return this;
    }

    public OvsFlowParams flowModFlags(
            final FlowModFlags flowModFlags) {
        this.flowModFlags = flowModFlags;
        return this;
    }

    public OvsFlowParams flowId(
            final FlowId flowId) {
        this.flowId = flowId;
        return this;
    }

    public OvsFlowParams flowKey(
            final FlowKey flowKey) {
        this.flowKey = flowKey;
        return this;
    }

    public OvsFlowParams flowName(final String flowName) {
        this.flowName = flowName;
        return this;
    }

    public OvsFlowParams flowCookie(
            final FlowCookie flowCookie) {
        this.flowCookie = flowCookie;
        return this;
    }

    public OvsFlowParams flowCookieMask() {
        this.flowCookieMask = flowCookieMask;
        return this;
    }

    public OvsFlowParams installHW(final Boolean installHW) {
        this.installHW = installHW;
        return this;
    }

    public OvsFlowParams strict(final Boolean strict) {
        this.strict = strict;
        return this;
    }

    public OvsFlowParams priority(final Integer priority) {
        this.priority = priority;
        return this;
    }
    
    public OvsFlowParams idleTimeout(final Integer idleTimeout) {
        this.idleTimeout = idleTimeout;
        return this;
    }

    public FlowBuilder buildNewFlowParam(FlowBuilder flowBuilder, OvsFlowParams ovsFlowParams) {

        if (this.priority() != null) {
            flowBuilder.setPriority(ovsFlowParams.priority());
        }
        if (this.tableID != null) {
            flowBuilder.setTableId(ovsFlowParams.tableID);
        }
        if (this.idleTimeout() != null) {
            flowBuilder.setIdleTimeout(idleTimeout);
        } else flowBuilder.setIdleTimeout(0);
        if (this.hardTimeout() != null) {
            flowBuilder.setHardTimeout(idleTimeout);
        } else flowBuilder.setHardTimeout(0);
        if (this.strict() != false) {
            flowBuilder.setInstallHw(true);
        } else flowBuilder.setInstallHw(true);
        if (this.installHW() != false) {
            flowBuilder.setInstallHw(true);
        }
        if (this.installHW() != false) {
            flowBuilder.setInstallHw(true);
        } else flowBuilder.setInstallHw(false);
        //TODO: Fix Cookies
        BigInteger val = new BigInteger("10", 10);
        if (this.flowCookie() != null ) {
            flowBuilder.setCookie(new FlowCookie(val));
        } else flowBuilder.setCookie(new FlowCookie(val));
        //TODO: Fix Cookies
        if (this.flowCookieMask() != null ) {
            flowBuilder.setCookie(new FlowCookie(val));
        } else flowBuilder.setCookie(new FlowCookie(val));
        // TODO: Complete Field Conversions
        return flowBuilder;
    }
}