/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.app.oneping.impl;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.opendaylight.of.controller.*;
import org.opendaylight.of.controller.pkt.MessageContext;
import org.opendaylight.of.controller.pkt.SequencedPacketListener;
import org.opendaylight.of.controller.pkt.SequencedPacketListenerRole;
import org.opendaylight.of.lib.OpenflowException;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.BufferId;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.dt.DataPathInfo;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.instr.ActOutput;
import org.opendaylight.of.lib.instr.Action;
import org.opendaylight.of.lib.instr.ActionType;
import org.opendaylight.of.lib.match.MFieldBasicMac;
import org.opendaylight.of.lib.match.Match;
import org.opendaylight.of.lib.match.MatchField;
import org.opendaylight.of.lib.match.MutableMatch;
import org.opendaylight.of.lib.mp.MBodyFlowStats;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.cache.AgeOutHashMap;
import org.opendaylight.util.net.EthernetType;
import org.opendaylight.util.net.IpProtocol;
import org.opendaylight.util.net.MacAddress;
import org.opendaylight.util.packet.Ethernet;
import org.opendaylight.util.packet.Packet;
import org.opendaylight.util.packet.ProtocolId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;

import static org.apache.felix.scr.annotations.ReferenceCardinality.MANDATORY_UNARY;
import static org.apache.felix.scr.annotations.ReferencePolicy.DYNAMIC;
import static org.opendaylight.of.lib.instr.ActionFactory.createAction;
import static org.opendaylight.of.lib.match.FieldFactory.createBasicField;
import static org.opendaylight.of.lib.match.MatchFactory.createMatch;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.*;
import static org.opendaylight.of.lib.msg.FlowModCommand.ADD;
import static org.opendaylight.of.lib.msg.FlowModCommand.DELETE_STRICT;
import static org.opendaylight.of.lib.msg.MessageFactory.create;

/**
 * Sample application that demonstrates various interactions with the
 * OpenFlow aspects of the controller API.
 * <p/>
 * The sample illustrates the following:
 * <ul>
 * <li>expressing dependency on {@link org.opendaylight.of.controller.ControllerService}</li>
 * <li>registering a listener for datapath connects/disconnects</li>
 * <li>registering a listener for packet in messages</li>
 * <li>handling packet-in messages</li>
 * <li>re-actively installing flow mods to datapaths to intercept and/or drop traffic</li>
 * <li>removing application-specific flow mods from datapaths</li>
 * </ul>
 * <p/>
 * The basic premise of the application is that it allows each unique src/dst
 * MAC pair one ping, "one ping only" - remember that movie? - per minute
 * per switch.
 * <p/>
 * It works by installing an intercept flow mod rule for ICMP traffic on each
 * connected switch to direct such traffic to the controller. The application
 * also registers as a packet-in listener and this is how it monitors each
 * ICMP packet. If it detects a second attempt to ping for the same src/dst
 * pair within the permitted time, it adds a flow rule on the source switch
 * (from which offending packet-in came) to ban any ICMP traffic for this
 * src/dst pair for one additional minute above what time remained.
 *
 * @author Thomas Vachuska
 */
@Component(immediate = true)
public class OnePingOnly {

    private final Logger log = LoggerFactory.getLogger(OnePingOnly.class);

    // Packet listener altitude and interest set
    // This will be replaced by altitude broker shortly
    private static final int ALTITUDE = 32000;
    private static final EnumSet<ProtocolId> PKT_TYPES =
            EnumSet.of(ProtocolId.ICMP);

    // Message listener interest set.
    private static final EnumSet<MessageType> MSG_TYPES =
            EnumSet.of(MessageType.FLOW_REMOVED);

    // Cookies and masks to help us identify the rules we put down
    private static final long COOKIE_MASK = 0x00000000ffffffffL;
    private static final long PING_COOKIE = 0x00000000deadbeefL;

    private static final int DROP_PRIORITY  = 35020;
    private static final int STEAL_PRIORITY = 35000;


    // Length of period (in millis) during which we expect only one ping
    private static final int AGE = 60 * 1000;

    // Log message strings
    private static final String FIRST_PING =
            "Switch {} received one ping from {} to {}; Thank you Vasili!";
    private static final String SECOND_PING =
            "Switch {} received second ping from {} to {}; " +
                    "What are you doing Vasili? I said one ping only!";
    private static final String FLOW_REMOVED =
            "Switch {} removed ping block from {} to {}; Carry on Vasili.";


    // Various listeners
    private final DataPathListener switchListener = new SwitchListener();
    private final MessageListener msgListener = new MsgListener();
    private final SequencedPacketListener packetListener = new PacketListener();

    // Cache of src/dst
    private final AgeOutHashMap<SrcDst, Long> recentPings = new AgeOutHashMap<>(AGE);

    @Reference(name = "ControllerService",
               cardinality = MANDATORY_UNARY, policy = DYNAMIC)
    protected ControllerService controllerService;

    @Activate
    public void activate() {
        // Listen for switch connections and packet-in messages
        controllerService.addDataPathListener(switchListener);
        controllerService.addMessageListener(msgListener, MSG_TYPES);
        controllerService.addPacketListener(packetListener,
                SequencedPacketListenerRole.DIRECTOR, ALTITUDE, PKT_TYPES);

        pushStealRules();
        log.info("OnePingOnly started");
    }

    @Deactivate
    public void deactivate() {
        removeStealAndDropRules();
        controllerService.removePacketListener(packetListener);
        log.info("OnePingOnly stopped");
    }


    /**
     * Enforces the one-ping-only policy per minute per src/dst per dpid.
     */
    private boolean isSecondPing(DataPathId dpid,
                                 MacAddress src, MacAddress dst) {
        // Check if our age-out cache has an entry for the given src/dst/dpid
        SrcDst key = new SrcDst(src, dst, dpid);
        Long time = recentPings.get(key);
        Long now = System.currentTimeMillis();

        // If there is no record of a prior ping or the ping is sufficiently
        // old, simply record it and let the path daemon forward it.
        if (time == null || time < now - AGE) {
            log.info(FIRST_PING, dpid, src, dst);
            recentPings.put(key, now);
            return false;       // let path daemon forward it
        }

        // Otherwise, record the fact that a second attempt was made and
        // send off a drop rule for an extra minute of penalty atop of what
        // remains.
        log.info(SECOND_PING, dpid, src, dst);
        long penalty = (now - time) + AGE;
        recentPings.put(key, penalty);

        // Note that the rule age is given in seconds, not millis
        addRule(dpid, createDrop(src, dst, (int) penalty / 1000));

        return true;
    }

    /**
     * Pushes steal rule for ICMP traffic to all currently connected switches.
     */
    private void pushStealRules() {
        for (DataPathInfo dpi : controllerService.getAllDataPathInfo())
            addRule(dpi.dpid(), createSteal());
    }

    /**
     * Removes steal rule for ICM traffic from all currently connected switches.
     */
    private void removeStealAndDropRules() {
        for (DataPathInfo dpi : controllerService.getAllDataPathInfo())
            removeRules(dpi.dpid());
    }

    /**
     * Pushes drop rule to the specified switch.
     *
     * @param dpid datapath id
     * @param rule flow mod to be pushed
     */
    private void addRule(DataPathId dpid, OfmFlowMod rule) {
        log.debug("Adding rule to switch {}", dpid);
        try {
            controllerService.sendFlowMod(rule, dpid);
        } catch (OpenflowException e) {
            log.warn("Unable to add rule", rule, dpid);
        }
    }

    /**
     * Removes all rules matching the base cookie from the given switch.
     *
     * @param dpid datapath id
     */
    private void removeRules(DataPathId dpid) {
        try {
            for (MBodyFlowStats fs : controllerService.getFlowStats(dpid, TableId.ALL))
                if (PING_COOKIE == (COOKIE_MASK & fs.getCookie())) {
                    OfmFlowMod flowMod =
                            createRemoveMatchAndPriority(fs.getMatch(),
                                                         fs.getPriority());
                    controllerService.sendConfirmedFlowMod(flowMod, dpid);
                }
        } catch (OpenflowException e) {
            log.warn("Unable to delete rule", dpid);
        }
    }

    /**
     * Create a flow mod message matching the given src/dst pair for IP/ICMP,
     * with action to drop the packet. The rule will have the prescribed
     * hard time-out.
     *
     * @param src source MAC to match
     * @param dst destination MAC to match
     * @param age number of seconds before the rule times out
     * @return immutable flow mod
     */
    private OfmFlowMod createDrop(MacAddress src, MacAddress dst, int age) {
        // TODO: use negotiated protocol instead
        ProtocolVersion pv = ProtocolVersion.V_1_0;

        MutableMatch match = createMatch(pv)
                .addField(createBasicField(pv, ETH_SRC, src))
                .addField(createBasicField(pv, ETH_DST, dst))
                .addField(createBasicField(pv, ETH_TYPE, EthernetType.IPv4))
                .addField(createBasicField(pv, IP_PROTO, IpProtocol.ICMP));

        OfmMutableFlowMod flow = (OfmMutableFlowMod)
                create(pv, MessageType.FLOW_MOD, ADD);

        // Mark the flow to time-out, to use our assigned cookie and priority.
        // Also, ask to be sent a message when the flow gets removed.
        flow.bufferId(BufferId.NO_BUFFER).hardTimeout(age)
                .cookie(PING_COOKIE)
                .priority(DROP_PRIORITY)
                .flowModFlags(EnumSet.of(FlowModFlag.SEND_FLOW_REM))
                .match((Match) (match.toImmutable()));
        return (OfmFlowMod) flow.toImmutable();

    }

    /**
     * Creates a message to remove all flow mods with the given match and
     * priority.
     *
     * @param match    match to use for the remove message
     * @param priority to use for the remove message
     */
    private OfmFlowMod createRemoveMatchAndPriority(Match match, int priority) {
        // TODO: use negotiated protocol instead
        ProtocolVersion pv = ProtocolVersion.V_1_0;
        OfmMutableFlowMod flow = (OfmMutableFlowMod)
                create(pv, MessageType.FLOW_MOD, DELETE_STRICT);
        flow.bufferId(BufferId.NO_BUFFER).priority(priority).match(match);
        return (OfmFlowMod) flow.toImmutable();
    }

    /**
     * Creates a steal rule ready to be sent to a switch.
     */
    private OfmFlowMod createSteal() {
        // TODO: use negotiated protocol instead
        ProtocolVersion pv = ProtocolVersion.V_1_0;

        Action steal = createAction(pv, ActionType.OUTPUT, Port.CONTROLLER,
                                    ActOutput.CONTROLLER_NO_BUFFER);

        MutableMatch match = createMatch(pv)
                .addField(createBasicField(pv, ETH_TYPE, EthernetType.IPv4))
                .addField(createBasicField(pv, IP_PROTO, IpProtocol.ICMP));

        OfmMutableFlowMod flow = (OfmMutableFlowMod)
                create(pv, MessageType.FLOW_MOD, ADD);

        // Mark the to use our assigned cookie and priority.
        flow.bufferId(BufferId.NO_BUFFER)
                .cookie(PING_COOKIE)
                .priority(STEAL_PRIORITY)
                .match((Match) (match.toImmutable())).addAction(steal);
        return (OfmFlowMod) flow.toImmutable();
    }

    /**
     * Handler for processing switch connect/disconnect events.
     */
    // TODO: Review - register as InitialFlowContributor?
    private class SwitchListener implements DataPathListener {
        @Override
        public void queueEvent(QueueEvent event) {
        }

        @Override
        public void event(DataPathEvent event) {
            if (event.type() == OpenflowEventType.DATAPATH_CONNECTED)
                addRule(event.dpid(), createSteal());
        }
    }

    /**
     * Handler for processing switch messages (other than packet-ins).
     */
    private class MsgListener implements MessageListener {
        @Override
        public void queueEvent(QueueEvent event) {
        }

        @Override
        public void event(MessageEvent event) {
            // Extract the match from the message; since we asked only for
            // flow removes, we don't have to fuss about discerning what type
            // of message this is.
            OfmFlowRemoved msg = (OfmFlowRemoved) event.msg();

            // We do have to worry, however, about whether this message is
            // about our own flow mods. This is where the cookie comes in.
            // If the cookie indicates this is not our flow, simply bail out.
            if (PING_COOKIE != (msg.getCookie() & COOKIE_MASK))
                return;

            // Extract the match from the message
            Match match = msg.getMatch();

            // Extract the values of the src/dst match fields.
            MacAddress src = null, dst = null;
            for (MatchField field : match.getMatchFields()) {
                if (field.getFieldType() == ETH_SRC)
                    src = ((MFieldBasicMac) field).getMacAddress();
                if (field.getFieldType() == ETH_DST)
                    dst = ((MFieldBasicMac) field).getMacAddress();
            }

            // Log message that the flow was removed
            log.info(FLOW_REMOVED, event.dpid(), src, dst);
        }
    }

    /**
     * Handler for processing packet-in messages.
     */
    private class PacketListener implements SequencedPacketListener {
        @Override
        public void event(MessageContext context) {
            if (context.isHandled() || context.isTestPacket())
                return;

            Packet packet = context.decodedPacket();
            Ethernet eth = packet.get(ProtocolId.ETHERNET);
            if (eth != null && isSecondPing(context.srcEvent().dpid(),
                                            eth.srcAddr(), eth.dstAddr()))
                context.packetOut().block();
        }

        @Override
        public void errorEvent(ErrorEvent event) {
        }

    }

    /**
     * Auxiliary used as a key for the src/dst pair.
     */
    private class SrcDst {

        private final MacAddress src, dst;
        private final DataPathId dpid;

        SrcDst(MacAddress src, MacAddress dst, DataPathId dpid) {
            this.src = src;
            this.dst = dst;
            this.dpid = dpid;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SrcDst srcDst = (SrcDst) o;
            return dpid.equals(srcDst.dpid) &&
                    dst.equals(srcDst.dst) && src.equals(srcDst.src);
        }

        @Override
        public int hashCode() {
            int result = src.hashCode();
            result = 31 * result + dst.hashCode();
            result = 31 * result + dpid.hashCode();
            return result;
        }
    }

}
