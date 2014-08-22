/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.pipeline.impl;

import org.opendaylight.of.controller.pipeline.TableContext;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.instr.*;
import org.opendaylight.of.lib.match.Match;
import org.opendaylight.of.lib.match.MatchField;
import org.opendaylight.of.lib.match.OxmFieldType;
import org.opendaylight.of.lib.mp.MBodyDesc;
import org.opendaylight.of.lib.msg.OfmFlowMod;
import org.opendaylight.of.lib.msg.Port;
import org.opendaylight.of.lib.msg.TableFeaturePropType;

import java.util.HashSet;
import java.util.Set;

import static org.opendaylight.of.lib.msg.TableFeaturePropType.*;

/** Utility class for determining the table context which supports the given 
 * <em>flowMod</em>.
 * 
 * @author Pramod Shanbhag
 */
class PipelineMediator {

    /** Returns true if the given <em>FlowMod</em> is supported 
     * 
     * @param mod the given <em>flowMod</em>
     * @param isTableMiss boolean indicating if the <em>flowMod</em>
     *         is a table miss rule.
     * @param ctx the given table context
     * @return true if the given context supports the given <em>flowMod</em>
     */
    static boolean tableSupports(OfmFlowMod mod, boolean isTableMiss,
                                 TableContext ctx) {
        if (tableSupportsInstructions(mod, isTableMiss, ctx))
            if (tableSupportsMatch(mod, isTableMiss, ctx))
                return true;
        
        return false;
    }

    private static boolean tableSupportsInstructions(OfmFlowMod mod, 
                                                     boolean isTableMiss,
                                                     TableContext ctx) {
        for (Instruction in : mod.getInstructions()) 
            if (!tableSupportsInstruction(in, isTableMiss, ctx)) 
                return false;

        return true;
    }

    private static boolean tableSupportsInstruction(Instruction in,
                                                    boolean isTableMiss,
                                                    TableContext ctx) {
        InstructionType type = in.getInstructionType();
        switch (type) {
            case GOTO_TABLE:
                return tableSupportsInstGotoTable((InstrGotoTable) in,
                        isTableMiss, ctx);

            case WRITE_METADATA:
            case CLEAR_ACTIONS:
            case METER:
            case EXPERIMENTER:
                TableFeaturePropType prop = isTableMiss
                        ? INSTRUCTIONS_MISS : INSTRUCTIONS;
                return ctx.supportsCapability(prop, type);

            case WRITE_ACTIONS:
                return tableSupportsInstWriteActions((InstrWriteActions) in,
                        isTableMiss, ctx);

            case APPLY_ACTIONS:
                return tableSupportsInstApplyActions((InstrApplyActions) in,
                        isTableMiss, ctx);

            default:
                throw new IllegalArgumentException("Bad InstructionType: " + type);
        }
    }

    private static boolean tableSupportsInstGotoTable(InstrGotoTable ingt,
                                                      boolean isTableMiss,
                                                      TableContext ctx) {
        if (isTableMiss) {
            if (ctx.supportsCapability(INSTRUCTIONS_MISS, 
                                  ingt.getInstructionType()))
                return ctx.containsNextTableMiss(ingt.getTableId());
        } else {
            if (ctx.supportsCapability(INSTRUCTIONS, ingt.getInstructionType()))
                return ctx.containsNextTable(ingt.getTableId());
        }
        return false;
    }
    
    private static boolean tableSupportsInstWriteActions(InstrWriteActions inwa,
                                                         boolean isTableMiss,
                                                         TableContext ctx) {
        TableFeaturePropType prop = isTableMiss ? WRITE_ACTIONS_MISS 
                : WRITE_ACTIONS;
        for (Action act: inwa.getActionSet()) {
            if (!ctx.supportsCapability(prop, act.getActionType())) 
                return false;
            
            // Action SET_FIELD needs to validate supported field as well.
            if (act.getActionType() == ActionType.SET_FIELD) {
                ActSetField acsf = (ActSetField) act;
                TableFeaturePropType mprop = isTableMiss ? WRITE_SETFIELD_MISS 
                        : WRITE_SETFIELD; 
                if (!ctx.supportsMatchFieldCapability(mprop, 
                                                 acsf.getField().getFieldType()))
                    return false;
            }
        }
        return true;
    }
    
    private static boolean tableSupportsInstApplyActions(InstrApplyActions inaa,
                                                         boolean isTableMiss,
                                                         TableContext ctx) {
        TableFeaturePropType prop = isTableMiss ? APPLY_ACTIONS_MISS 
                : APPLY_ACTIONS;
        boolean actionCopyController = false;
        boolean actionFwdNormal = false;
        /* Table ID for the ProVision Software table */
        final TableId PROVISION_SW_TABLE = TableId.valueOf(200);

        for (Action act: inaa.getActionList()) {
            if (!ctx.supportsCapability(prop, act.getActionType())) 
                return false;
            /*
             * TODO: If the action combination is copy+forward normal
             * for provision device then allow only table id 
             * PROVISION_SW_TABLE. For all other tables return false.
             * The hack needs to be removed completely once
             * device driver changes are available as such device specific
             * constraints will be taken care by device driver framework.
             */
            if ((prop == APPLY_ACTIONS) &&
                (act.getActionType() == ActionType.OUTPUT)) {
                ActOutput  acop = (ActOutput) act;
                if (acop.getPort() == Port.CONTROLLER)
                    actionCopyController = true;
                else if (acop.getPort() == Port.NORMAL)
                    actionFwdNormal = true;

                if ((actionCopyController) && (actionFwdNormal) &&
                    (isTableOnProvisionSwitch(((DefaultTableContext)ctx).dpDesc())) &&
                    (PROVISION_SW_TABLE != ctx.tableId())) {
                   return false;
                }
            }

            // Action SET_FIELD needs to validate supported field as well.
            if (act.getActionType() == ActionType.SET_FIELD) {
                ActSetField acsf = (ActSetField) act;
                TableFeaturePropType mprop = isTableMiss ? APPLY_SETFIELD_MISS 
                        : APPLY_SETFIELD; 
                if (!ctx.supportsMatchFieldCapability(mprop, 
                                                 acsf.getField().getFieldType()))
                    return false;
            }
        }
        return true;
    }

    /**
     * Returns true if the given device description belongs to HP's provision
     * switches.
     * @param dpDesc datapath description
     * @return true or false
     */
    private static boolean isTableOnProvisionSwitch(MBodyDesc dpDesc) {
        // Check that the manufacturer is HP. We use startsWith() because 
        // KA.15.14.* code used "HP Networking" and KA.15.15.+ code
        // uses "HP". Our controller must work with both firmware revisions.
        // FIXME Replace this method once Bantha device drivers are in place.
        return dpDesc.getMfrDesc().startsWith("HP") &&
                !dpDesc.getSwDesc().contains("Comware");
    }

    private static boolean tableSupportsMatch(OfmFlowMod mod, boolean isTableMiss,
                                              TableContext ctx) {
        Match match = mod.getMatch();
        if (match != null) {
            if (!isTableMiss) {
                // Implementation Note:
                // no match signifies a table miss rule. All supported match 
                // fields will be wild carded. This will be supported by table.
                // Hence return true for table miss.
                
                Set<OxmFieldType> fmMatches = new HashSet<OxmFieldType>();
                for (MatchField mf : match.getMatchFields()) {
                    if (!ctx.supportsMatchField(mf))
                        return false;
                    fmMatches.add(mf.getFieldType());
                }

                // validate wild cards
                if (!ctx.supportsWildCards(fmMatches))
                    return false;
            }
        }
        return true;
    }
}
