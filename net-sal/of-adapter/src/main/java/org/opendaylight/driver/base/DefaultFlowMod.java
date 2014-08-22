/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.driver.base;

import org.opendaylight.net.facet.FlowModAdjuster;
import org.opendaylight.net.facet.FlowUnsupportedException;
import org.opendaylight.of.controller.pipeline.PipelineDefinition;
import org.opendaylight.of.controller.pipeline.TableContext;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.VersionNotSupportedException;
import org.opendaylight.of.lib.dt.BufferId;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.instr.*;
import org.opendaylight.of.lib.match.*;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.StringUtils;
import org.opendaylight.util.driver.AbstractDefaultFacet;
import org.opendaylight.util.driver.DeviceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.msg.FlowModFlag.SEND_FLOW_REM;
import static org.opendaylight.util.CommonUtils.itemSet;
import static org.opendaylight.util.ResourceUtils.getBundledResource;

/**
 * A default implementation of the flow mod facet for openflow-capable devices.
 * This implementation will be used if an Openflow device cannot be properly 
 * identified. No special handling will occur on the flows other than what the
 * table context reports.  We only support Openflow versions 1.0 and 1.3.
 * 
 * @author Julie Britt
 * @author Simon Hunt
 */
public class DefaultFlowMod extends AbstractDefaultFacet
        implements FlowModAdjuster {
    
    protected final Logger logger = LoggerFactory.getLogger(DefaultFlowMod.class);

    // log message strings
    private static final ResourceBundle RES = getBundledResource(DefaultFlowMod.class);

    protected static final String E_NULL_PARAMS =
                                RES.getString("e_null_params");
    protected static final String E_FLOW_UNSUPPORTED = 
                                RES.getString("e_flow_unsupported");
    
    protected static final String E_VERSION_NOT_SUPPORTED = 
                            RES.getString("e_version_not_supported");

    protected static final int FLOW_IDLE_TIMEOUT = 0;
    protected static final int FLOW_HARD_TIMEOUT = 0;
    protected static final Set<FlowModFlag> FLAGS = EnumSet.of(SEND_FLOW_REM);
    protected static final int DEFAULT_PRIORITY = 0;
    protected static final long DEFAULT_COOKIE = 0xffff000000000000L;
    protected static final TableId BASE_TABLE = TableId.valueOf(0);


    PipelineDefinition tableProperties = null;
    ProtocolVersion version = null;
    boolean hybridMode = true;

    /**
     * Constructs a default flow mod facet that is projected onto the specified
     * Openflow device info context.
     * 
     * @param context the device info of which this is a facet
     */
    public DefaultFlowMod(DeviceInfo context) {
        super(context);
    }

    @Override
    public void setTableProperties(PipelineDefinition tableProps,
                                   ProtocolVersion pv, boolean isHybrid) {

        if (tableProps == null || pv == null)
            throw new NullPointerException(E_NULL_PARAMS);

        tableProperties = tableProps;
        version = pv;
        hybridMode = isHybrid;
    }

    /**
     * Returns the version of OpenFlow being run on the device.
     * 
     * @return the version of OpenFlow being run on the device
     */
    protected ProtocolVersion getVersion() {
        return version;
    }
    
    // Just the defaults since unknown what kind of device it is
    @Override
    public Set<OfmFlowMod> generateDefaultFlows() {
        
        if (getVersion().equals(V_1_0))
            return createDefaultFlow(null);

        // Special case for mininet
        if (!tableProperties.hasTables())
            return createDefaultFlow(BASE_TABLE);

        Set<OfmFlowMod> defaultFlows = new HashSet<>();
        for (TableId table : tableProperties.getTableIds())
            defaultFlows.addAll(createDefaultFlow(table));
        return defaultFlows;
    }

    // Make decisions based solely on the table properties reported in the
    // OF handshake
    @Override
    public Set<OfmFlowMod> adjustFlowMod(OfmFlowMod flow) {
        Set<OfmFlowMod> adjustedFlows = new HashSet<>();

        // For OF 1.0, there are no tables... return flow as is
        if (getVersion().equals(V_1_0)) {
            adjustedFlows.add(flow);
            return adjustedFlows;
        }
        
        // If the caller has already populated table ID, we should leave as is
        if (flow.getTableId() != null) {
            adjustedFlows.add(flow);
            return adjustedFlows;
        }
        
        // If there are no table features (e.g. OVS mininet), then
        // patch in table 0; we know we're OF 1.3 at this point
        if (!tableProperties.hasTables()) {
            adjustedFlows.add(assignToTable(flow, BASE_TABLE));
            return adjustedFlows;
        }


        // At this point we know: OF 1.3+ and table id is not set yet...
        
        // When adjusting a flow mod, make sure that the resulting flows are
        // not duplicates of the basic flows that are laid down by default
        Set<OfmFlowMod> baseFlows = generateDefaultFlows();
        
        // Find the best table to use for the given match fields and
        // instructions.
        TableId mfTableId = getTableIdForMatch(flow.getMatch());
        TableId instrTableId = getTableIdForInstructions(flow);

        // Something in this flow is not supported at all
        if (mfTableId == null || instrTableId == null)
            throw new FlowUnsupportedException(StringUtils.format(
                    E_FLOW_UNSUPPORTED, flow, getContext().getTypeName()));

        // If both the match and the instructions can go straight into the
        // same table, just use that.
        if (mfTableId.equals(instrTableId)) {
            adjustedFlows.add(assignToTable(flow, mfTableId));
            return removeDuplicates(baseFlows, adjustedFlows);
        }

        // At this point, either the match fields or the instruction actions
        // can only be supported in one of the tables. As this is a best-
        // guess implementation, figure out which one supports both.
        TableId bestGuess = bestGuess(flow, mfTableId, instrTableId);
        adjustedFlows.add(assignToTable(flow, bestGuess));
        TableId other = bestGuess.equals(mfTableId) ? instrTableId : mfTableId;
        // TODO: review - this is dangerous: what if other > bestGuess??
        adjustedFlows.add(createGotoFlow(flow, other, bestGuess));

        return removeDuplicates(baseFlows, adjustedFlows);
    }

    @Override
    public String toString() {
        return "[" + this.getClass().getSimpleName() +
                " ipAddress = " + getIpAddress() + "]";
    }

    /**
     * Go through the list of newly added flows and make sure they are not
     * duplicated with existing flows.
     * 
     * @param currentFlows represents the flows already being pushed
     * @param newFlows represents the new flows we want to add
     * @return Set of new flows that are not duplicated
     */
    protected Set<OfmFlowMod> removeDuplicates(Set<OfmFlowMod> currentFlows,
                                             Set<OfmFlowMod> newFlows) {
        Set<OfmFlowMod> notDuplicates = new HashSet<OfmFlowMod>();
        for (OfmFlowMod flow : newFlows) {
            if (!alreadyContains(flow, currentFlows)) {
                notDuplicates.add(flow);
            }
        }

        return notDuplicates;
    }

    /**
     * Determines whether a single flow is already in a list of flows.
     * 
     * @param flow to be examined
     * @param currentFlows to be compared against
     * @return boolean to indicate whether this flow is already there
     */
    protected boolean alreadyContains(OfmFlowMod flow,
                                    Set<OfmFlowMod> currentFlows) {
        for (OfmFlowMod currentFlow : currentFlows) {
            if (sameFlow(flow, currentFlow)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Compares only the things the flow mod adjustments care about to
     * determine if the two flows are the same.
     * 
     * @param firstFlow to be compared
     * @param secondFlow to be compared
     * @return boolean indicating whether they are the same
     */
    protected boolean sameFlow(OfmFlowMod firstFlow, OfmFlowMod secondFlow) {
        // if one of the actions is null and the others aren't, not the same
        if (!sameNull(firstFlow.getActions(), secondFlow.getActions())) {
            return false;
        }

        // if one of the matches is null and the other is not, not the same
        if (!sameNull(firstFlow.getActions(), secondFlow.getActions())) {
            return false;
        }

        // if the matches aren't null, make sure they are the same
        if (firstFlow.getMatch() != null) {
            if (!firstFlow.getMatch().equals(secondFlow.getMatch())) {
                return false;
            }
        }

        // if one of the instructions is null and the others aren't, not the
        // same
        if (!sameNull(firstFlow.getInstructions(), secondFlow.getInstructions())) {
            return false;
        }

        // if they are both null, then this is equal
        if (firstFlow.getInstructions() == null) {
            return true;
        }

        // else make sure the size of each is the same
        if (firstFlow.getInstructions().size() != secondFlow.getInstructions()
            .size()) {
            return false;
        }

        // then make sure the instructions match
        for (Instruction instr : firstFlow.getInstructions()) {
            boolean found = false;

            for (Instruction secInstr : secondFlow.getInstructions()) {
                if ((instr.getInstructionType().equals(secInstr
                    .getInstructionType()))
                        && (instr.getTotalLength() == secInstr.getTotalLength())
                        && (instr.getVersion().equals(secInstr.getVersion()))) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                return false;
            }
        }

        return true;
    }

    /**
     * Does a simple comparison for nulls.
     * 
     * @param first object to compare
     * @param second object to compare
     * @return true if their both null or neither null
     */
    protected boolean sameNull(Object first, Object second) {
        return (((first == null) && (second == null)) || ((first != null) && (second != null)));
    }
    
    /**
     * Try to find the best table to put this flow into. Depending on which
     * kind of table it is in (if we have that knowledge), create a goto flow
     * to connect one to the other.
     * 
     * @param flow being inspected
     * @param mfTableId that was best for the match
     * @param instrTableId that was best for the instruction
     * @return TableId that's the best for this flow
     * @throws FlowUnsupportedException if things go wrong
     */
    protected TableId bestGuess(OfmFlowMod flow, TableId mfTableId,
                                TableId instrTableId) {
        // At this point, either the match fields or the instruction actions
        // can only be supported in one of the tables. As this is a best-
        // guess implementation, figure out which one supports both.
        boolean isTableMiss = (flow.getMatch() == null) ? true : flow
            .getMatch().getMatchFields().isEmpty();
        TableId bestGuess;
        if (doesTableSupportMatch(mfTableId, flow.getMatch())
                && doesTableSupportInstructions(mfTableId,
                                                flow.getInstructions(),
                                                isTableMiss)) {
            bestGuess = mfTableId;
        } else if (doesTableSupportMatch(instrTableId, flow.getMatch())
                && doesTableSupportInstructions(instrTableId,
                                                flow.getInstructions(),
                                                isTableMiss)) {
            bestGuess = instrTableId;
        } else {
            throw new FlowUnsupportedException(StringUtils.format(
                    E_FLOW_UNSUPPORTED, flow, getContext().getTypeName()));
        }

        return bestGuess;
    }

    /**
     * Returns a simple match all flow mod.
     * 
     * @param tableId for flow
     * @return Set of default flow mods
     */
    protected Set<OfmFlowMod> createDefaultFlow(TableId tableId) {
        OfmMutableFlowMod flow = createBasicFlow(tableId);
        flow.match(matchAll());

        if (getVersion().equals(V_1_0)) {
            flow.addAction(createDefaultAction());
        } else {
            flow.addInstruction(createDefaultInstruction(tableId));
        }

        return itemSet((OfmFlowMod) flow.toImmutable());
    }

    /**
     * Determines if the given table supports the match.
     * 
     * @param tableId of the table being examined
     * @param match to be analyzed
     * @return boolean indicating whether the table supports the match
     */
    protected boolean doesTableSupportMatch(TableId tableId, Match match) {

        if (match == null) {
            // All tables should support no matches
            return true;
        }

        Set<MatchField> validFields = findValidMatchFields(tableId, match);
        if (validFields.size() != match.getMatchFields().size()) {
            return false;
        }

        Set<OxmFieldType> fields = new HashSet<OxmFieldType>();
        for (MatchField mf : validFields) {
            fields.add(mf.getFieldType());
        }

        // The fields that are not specified are considered wildcarded
        TableContext tableContext = tableProperties.getTableContext(tableId);
        return (tableContext.supportsWildCards(fields));
    }

    /**
     * Determines whether a specific goto instruction is supported by the
     * table.
     * 
     * @param instr goto instruction wanting to be placed
     * @param isTableMiss indicates whether the match fields were empty
     * @param tableId being analyzed
     * @return boolean indicating whether the table supports it or not
     */
    protected boolean tableSupportsInstGotoTable(InstrGotoTable instr,
                                                 boolean isTableMiss,
                                                 TableId tableId) {
        TableContext context = tableProperties.getTableContext(tableId);
        if (isTableMiss) {
            if (context
                .supportsCapability(TableFeaturePropType.INSTRUCTIONS_MISS,
                                    instr.getInstructionType())) {
                return context.containsNextTableMiss(instr.getTableId());
            }
        } else {
            if (context.supportsCapability(TableFeaturePropType.INSTRUCTIONS,
                                           instr.getInstructionType())) {
                return context.containsNextTable(instr.getTableId());
            }
        }

        return false;
    }

    /**
     * Determines whether a specific apply instruction is supported by the
     * table.
     * 
     * @param instr goto instruction wanting to be placed
     * @param isTableMiss indicates whether the match fields were empty
     * @param tableId being analyzed
     * @return boolean indicating whether the table supports it or not
     */
    protected boolean tableSupportsInstApplyActions(InstrApplyActions instr,
                                                    boolean isTableMiss,
                                                    TableId tableId) {
        TableContext context = tableProperties.getTableContext(tableId);
        TableFeaturePropType prop = isTableMiss ? TableFeaturePropType.APPLY_ACTIONS_MISS
                : TableFeaturePropType.APPLY_ACTIONS;

        for (Action action : instr.getActionList()) {
            if (!context.supportsCapability(prop, action.getActionType())) {
                return false;
            }

            // Validate the apply actions specially
            if (prop == TableFeaturePropType.APPLY_ACTIONS) {
                if (action.getActionType() == ActionType.OUTPUT) {
                    // Check for specific analysis
                    if (!analyzeOutputType((ActOutput)action)) {
                        return false;
                    }
                } else if (action.getActionType() == ActionType.SET_FIELD) {
                    ActSetField acsf = (ActSetField) action;
                    TableFeaturePropType mprop = isTableMiss ? TableFeaturePropType.APPLY_SETFIELD_MISS
                            : TableFeaturePropType.APPLY_SETFIELD;
                    if (!context.supportsMatchFieldCapability(mprop, acsf
                        .getField().getFieldType())) {
                        return false;
                    }
                    
                    // Check further analysis if necessary
                    if (!analyzeSetType((ActSetField)action)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Determines whether a specific write instruction is supported by the
     * table.
     * 
     * @param instr goto instruction wanting to be placed
     * @param isTableMiss indicates whether the match fields were empty
     * @param tableId being analyzed
     * @return boolean indicating whether the table supports it or not
     */
    protected boolean tableSupportsInstWriteActions(InstrWriteActions instr,
                                                    boolean isTableMiss,
                                                    TableId tableId) {
        TableContext context = tableProperties.getTableContext(tableId);
        TableFeaturePropType prop = isTableMiss ? TableFeaturePropType.WRITE_ACTIONS_MISS
                : TableFeaturePropType.WRITE_ACTIONS;
        for (Action act : instr.getActionSet()) {
            if (!context.supportsCapability(prop, act.getActionType())) {
                return false;
            }

            // Action SET_FIELD needs to validate supported field as well.
            if (act.getActionType() == ActionType.SET_FIELD) {
                ActSetField acsf = (ActSetField) act;
                TableFeaturePropType mprop = isTableMiss ? TableFeaturePropType.WRITE_SETFIELD_MISS
                        : TableFeaturePropType.WRITE_SETFIELD;
                if (!context.supportsMatchFieldCapability(mprop, acsf
                    .getField().getFieldType())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Analyze whether the list of instructions are supported by the given
     * table.
     * 
     * @param tableId is the table for which we are analyzing the instruction
     * @param instructions Instructions being analyzed
     * @param isTableMiss indicates whether the matches in the flow were
     *        wildcarded
     * @return boolean indicating whether instruction is supported by table
     */
    protected boolean doesTableSupportInstructions(TableId tableId,
                                                   List<Instruction> instructions,
                                                   boolean isTableMiss) {

        for (Instruction instr : instructions) {
            InstructionType type = instr.getInstructionType();
            switch (type) {
            case GOTO_TABLE:
                if (!tableSupportsInstGotoTable((InstrGotoTable) instr,
                                                isTableMiss, tableId)) {
                    return false;
                }
                break;

            case WRITE_METADATA:
            case CLEAR_ACTIONS:
            case METER:
            case EXPERIMENTER:
                TableFeaturePropType prop = isTableMiss ? TableFeaturePropType.INSTRUCTIONS_MISS
                        : TableFeaturePropType.INSTRUCTIONS;
                if (!tableProperties.getTableContext(tableId)
                    .supportsCapability(prop, type)) {
                    return false;
                }
                break;

            case WRITE_ACTIONS:
                if (!tableSupportsInstWriteActions((InstrWriteActions) instr,
                                                   isTableMiss, tableId)) {
                    return false;
                }
                break;

            case APPLY_ACTIONS:
                if (!tableSupportsInstApplyActions((InstrApplyActions) instr,
                                                   isTableMiss, tableId)) {
                    return false;
                }
                break;

            default:
                throw new IllegalArgumentException("Bad InstructionType: "
                        + type);
            }
        }

        return true;
    }

    /**
     * Generic flow creator with default fields.
     * 
     * @param tableId of the flow
     * @return OfmMutableFlowMod of the basic flow
     */
    protected OfmMutableFlowMod createBasicFlow(TableId tableId) {
        OfmMutableFlowMod flow = createAddFlow();
        if (tableId != null) {
            flow.tableId(tableId);
        }
        return flow;
    }

    /**
     * Make a copy of the given FlowMod, but don't include the instructions or
     * the matches as those might have to be adjusted.
     * 
     * @param origFlow Original flow being analyzed for compatibility with the
     *        given properties of this device
     * @return FlowMod describing the inputed data
     */
    protected OfmMutableFlowMod copyFlow(OfmFlowMod origFlow) {
        OfmMutableFlowMod newFlow = (OfmMutableFlowMod) MessageFactory.mutableCopy(origFlow);
        newFlow.match(matchAll());    //essentially clears out the match field
        if (getVersion() == V_1_0) {
            newFlow.clearActions();
        } else {
            newFlow.clearInstructions();
        }
        return newFlow;
    }

    /**
     * Create a mutable ADD flow.
     * 
     * @return Mutable ADD flow
     */
    protected OfmMutableFlowMod createAddFlow() {
        OfmMutableFlowMod flow = (OfmMutableFlowMod) MessageFactory.create(getVersion(),
                                                         MessageType.FLOW_MOD,
                                                         FlowModCommand.ADD);
        flow.bufferId(BufferId.NO_BUFFER).idleTimeout(FLOW_IDLE_TIMEOUT)
                    .hardTimeout(FLOW_HARD_TIMEOUT).flowModFlags(FLAGS)
                    .priority(DEFAULT_PRIORITY).cookie(DEFAULT_COOKIE);
        return flow;
    }

    /**
     * Create a forward normal action.
     * 
     * @return Action representing forward normal
     */
    protected Action forwardNormalAction() {
        return ActionFactory.createAction(getVersion(), ActionType.OUTPUT,
                                          Port.NORMAL);
    }

    /**
     * Create a steal action.
     * 
     * @return Action representing steal
     */
    protected Action stealAction() {
        return ActionFactory.createAction(getVersion(), ActionType.OUTPUT,
                                          Port.CONTROLLER,
                                          ActOutput.CONTROLLER_MAX);
    }

    /**
     * Create a match that matches all packets.
     * 
     * @return Match that matches all packets
     */
    protected Match matchAll() {
        return (Match) MatchFactory.createMatch(getVersion()).toImmutable();
    }

    /**
     * Create an action based on the protocol version and hybrid mode.
     * 
     * @return Action representing default action
     */
    protected Action createDefaultAction() {
        if (hybridMode) {
            // In hybrid mode, datapath should forward as normal
            return forwardNormalAction();
        }

        // In pure OF mode, datapath should ask controller
        return stealAction();
    }

    /**
     * Wrap a default action in an instruction.
     * 
     * @param tableId for which this default instruction is being created; may
     *        not be needed
     * @return Instruction representing default action
     */
    protected Instruction createDefaultInstruction(TableId tableId) {
        if (getVersion().equals(V_1_0)) {
            throw new VersionNotSupportedException(E_VERSION_NOT_SUPPORTED);
        }
        
        // Configure the table misses in non-hybrid
        if (tableProperties.hasTables() && createTableMisses()) {
            if (tableProperties.getTableContext(tableId).hasNextTablesMiss()) {
                return createGotoInstruction(tableProperties.getTableContext(tableId).getNextTableMiss());
            }
        }

        // Instruction with default action
        InstrMutableAction instr = InstructionFactory
            .createMutableInstruction(getVersion(),
                                      InstructionType.APPLY_ACTIONS);
        instr.addAction(createDefaultAction());
        return (Instruction) instr.toImmutable();
    }

    /**
     * Indicates whether we should create table misses for the default flows.
     * The default is to create table misses.
     * 
     * @return boolean true if we should
     */
    protected boolean createTableMisses() {
        return true;
    }
    
    /**
     * Create a goto instruction to a specific table.
     * 
     * @param tableId to go to
     * @return Instruction that is a goto
     */
    protected Instruction createGotoInstruction(TableId tableId) {
        return InstructionFactory.createInstruction(getVersion(),
                                                    InstructionType.GOTO_TABLE,
                                                    tableId);
    }

    /**
     * Create a goto flow from the given firstTableId to the given
     * secondTableId. This will be called when a flow must be split between
     * two tables.
     * 
     * @param flow the original FlowMod requested to send to the device
     * @param firstTableId of the first table into which the Goto will be
     *        placed
     * @param secondTableId of the second table to which the Goto will be
     *        pointing
     * @return FlowMod representing the goto
     */
    protected OfmFlowMod createGotoFlow(OfmFlowMod flow, TableId firstTableId,
                                        TableId secondTableId) {

        Instruction newInstr = createGotoInstruction(secondTableId);

        // For the hardware table, we need to extract any match fields that
        // aren't supported
        OfmMutableFlowMod hwTableFlow = copyFlow(flow);
        //TODO:  dangerous.  What if newMatch becomes a matchAll?
        Match newMatch = removeInvalidMatchesForTable(flow.getMatch(),
                                                      firstTableId);

        hwTableFlow.match(newMatch).tableId(firstTableId)
            .addInstruction(newInstr);
        return (OfmFlowMod) hwTableFlow.toImmutable();
    }

    /**
     * Create a chain of goto flows throughout all tables in the
     * tableProperties.
     * 
     * @param flow to use as the base of the goto flows
     * @return Set of goto flow mods
     */
    protected Set<OfmFlowMod> createTableMissFlows(OfmFlowMod flow) {

        Set<OfmFlowMod> gotoFlows = new HashSet<OfmFlowMod>();
        OfmFlowMod newFlow;

        for (TableId table : tableProperties.getTableIds()) {
            if (tableProperties.getTableContext(table).hasNextTablesMiss()) {
                newFlow = createGotoFlow(flow, table, tableProperties.getTableContext(table).getNextTableMiss());
                gotoFlows.add(newFlow);
            }
        }

        return gotoFlows;
    }

    /**
     * Make sure the device allows all the match fields in the given table.
     * Remove them if needed.
     * 
     * @param match to check for invalid match fields
     * @param tableId for which the match is targeted
     * @return Match representing valid match fields only
     */
    private Match removeInvalidMatchesForTable(Match match, TableId tableId) {
        if (match == null) {
            return match;
        }

        Set<MatchField> validFields = findValidMatchFields(tableId, match);

        if (validFields.size() == match.getMatchFields().size()) {
            return match;
        }

        MutableMatch newMatch = MatchFactory.createMatch(getVersion());
        for (MatchField mf : validFields) {
            newMatch.addField(mf);
        }

        return (Match) newMatch.toImmutable();
    }

    /**
     * Add all the match fields if they are all valid.
     * 
     * @param match from which fields will be analyzed
     * @param tableId to which match is destined
     * @return newly created match with valid match fields
     */
    protected Set<MatchField> findValidMatchFields(TableId tableId, Match match) {
        Set<MatchField> validFields = new HashSet<MatchField>();
        TableContext tableContext = tableProperties.getTableContext(tableId);
        for (MatchField mf : match.getMatchFields()) {
            // This is for device specific classes to be able to add extra
            // criteria for matches
            if (!tableContext.supportsMatchField(mf)) {
                continue;
            }

            validFields.add(mf);
        }
        return validFields;
    }

    /**
     * Determine the best table supporting this instruction. Since we don't
     * know information about the device, just use table context.
     * 
     * @param flow whose instructions are being analyzed
     * @return TableId determined to be the best place for this instruction
     */
    protected TableId getTableIdForInstructions(OfmFlowMod flow) {

        boolean isTableMiss = true;
        if ((flow.getMatch() != null)
                && (!flow.getMatch().getMatchFields().isEmpty())) {
            isTableMiss = false;
        }

        // If there are no instructions, don't know
        if (flow.getInstructions().isEmpty()) {
            return null;
        }

        // Find a table in which all instructions are supported
        for (TableId tableId : tableProperties.getTableIds()) {
            if (doesTableSupportInstructions(tableId, flow.getInstructions(),
                                             isTableMiss)) {
                return tableId;
            }
        }

        // Will return null if no table supports it
        return null;
    }

    /**
     * Determine the best table id for the match in the flow. "Best" is
     * defined by each implementation of this facet.
     * 
     * @param match to be analyzed
     * @return TableId of "best" table to use
     */
    protected TableId getTableIdForMatch(Match match) {

        for (TableId tableId : tableProperties.getTableIds()) {
            // If this is a not a wildcard match, see if all the fields
            // are supported in this match
            if (doesTableSupportMatch(tableId, match)) {
                return tableId;
            }
        }

        // Will return null if no table supports it
        return null;
    }

    /**
     * Change the given flow to point to the given tableId.
     * 
     * @param flow to be changed
     * @param tableId to which flow should be directed
     * @return adjusted flow mod
     */
    protected OfmFlowMod assignToTable(OfmFlowMod flow, TableId tableId) {
        OfmMutableFlowMod newFlow =
                (OfmMutableFlowMod) MessageFactory.mutableCopy(flow);
        return (OfmFlowMod) newFlow.tableId(tableId).toImmutable();
    }


    public String toDebugString() {
        return toString();
    }

    /**
     * Verify whether the given set action is permitted on this device.
     * 
     * @param action the set action being analyzed
     * @return boolean indicating whether the action is supported or not
     */
    protected boolean analyzeSetType(ActSetField action) {
        return true;
    }
    
    /**
     * Verify whether the given output action is permitted on this device.
     * 
     * @param action the output action being analyzed
     * @return boolean indicating whether the action is supported or not
     */
    protected boolean analyzeOutputType(ActOutput action) {
        return true;
    }
}
