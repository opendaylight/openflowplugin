/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.pipeline.impl;

import org.opendaylight.of.controller.RoleAdvisor;
import org.opendaylight.of.controller.flow.impl.FlowTrk;
import org.opendaylight.of.controller.impl.AbstractSubComponent;
import org.opendaylight.of.controller.impl.ListenerService;
import org.opendaylight.of.controller.pipeline.MutablePipelineDefinition;
import org.opendaylight.of.controller.pipeline.MutableTableContext;
import org.opendaylight.of.controller.pipeline.PipelineDefinition;
import org.opendaylight.of.controller.pipeline.TableContext;
import org.opendaylight.of.lib.OfpCodeBasedEnum;
import org.opendaylight.of.lib.OpenflowException;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.match.Match;
import org.opendaylight.of.lib.match.OxmBasicFieldType;
import org.opendaylight.of.lib.match.OxmFieldType;
import org.opendaylight.of.lib.mp.MBodyDesc;
import org.opendaylight.of.lib.mp.MBodyTableFeatures;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Provides PipelineReader implementation.
 * This forms a controller sub-component and manages the
 * {@link PipelineDefinition} for all the datapaths.
 * <p>
 * Controller Subcomponent {@link FlowTrk} uses this class to get the
 * supported Table IDs while sending a <em>FlowMod</em> message.
 *
 * @author Pramod Shanbhag
 * @author Radhika Hegde
 * @author Frank Wood
 * @author Simon Hunt
 */
public class PipelineManager extends AbstractSubComponent
            implements PipelineMgmt {

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            PipelineManager.class, "pipelineManager");

    private static final String LM_ERR_PROC_TF_REPLY = RES.getString("lm_err_proc_tf_reply");
    private static final String E_NO_PROP_TYPE = RES.getString("e_no_prop_type");

    private static final String MSG_EXPR_PROP_TYPE = RES.getString("msg_expr_prop_type");
    private static final String MSG_MISSING_PROP_TYPE = RES.getString("msg_miss_prop_type");
    private static final String E_BAD_NEXT_TABLE_ID = RES.getString("e_bad_table_id");

    private static final TableId TID_0 = TableId.valueOf(0);

    // logger
    private static final Logger DEFAULT_LOG =
            LoggerFactory.getLogger(PipelineManager.class);

    static Logger log = DEFAULT_LOG;

    static void setLogger(Logger testLogger) {
        log = testLogger;
    }

    static void restoreLogger() {
        log = DEFAULT_LOG;
    }

    
    // global map of dpid to pipeline definition
    private final ConcurrentHashMap<DataPathId, PipelineDefinition>
         deviceToDefinitionMap = new ConcurrentHashMap<>();

    @Override
    protected AbstractSubComponent init(ListenerService ls, RoleAdvisor ra) {
        super.init(ls, ra);
        log.debug("PipelineManager initialized");
        return this;
    }

    @Override
    protected void shutdown() {
        deviceToDefinitionMap.clear();
        super.shutdown();
    }

    @Override
    public void removeDefinition(DataPathId dpid) {
        deviceToDefinitionMap.remove(dpid);
    }

    @Override
    public PipelineDefinition getDefinition(DataPathId dpid) {
        PipelineDefinition pd = deviceToDefinitionMap.get(dpid);
        if (pd != null)
            return pd;
        
        // retrieve raw table features from core controller
        List<MBodyTableFeatures> tfs = new ArrayList<>();
        for (MBodyTableFeatures.Array a : listenerService.getCachedTableFeatures(dpid)) 
            tfs.addAll(a.getList());
        MBodyDesc dpDesc = listenerService.getCachedDeviceDesc(dpid);
        pd = buildPipelineDefinition(tfs, dpDesc);
        deviceToDefinitionMap.put(dpid, pd);
        return pd;
    }

    @Override
    public Set<TableId> align(OfmFlowMod mod, DataPathId dpid) {
        Set<TableId> supportedTableIds = new TreeSet<>();
        PipelineDefinition def = getDefinition(dpid);
        // If we don't have any table definitions, we can only assume table 0.
        if (!def.hasTables()) {
            supportedTableIds.add(TID_0);
            return supportedTableIds;
        }

        // For OpenFlow 1.3, empty match fields indicates that all fields are
        // wild carded. This becomes a 'Table-Miss' flow entry.
        Match match = mod.getMatch();
        boolean isTableMiss = true;
        if (match != null)
            isTableMiss = match.getMatchFields().isEmpty();
        for (TableId id: def.getTableIds()) {
            TableContext ctx = def.getTableContext(id);
            if (PipelineMediator.tableSupports(mod, isTableMiss, ctx)) {
                supportedTableIds.add(id);
            }
        }
        return supportedTableIds;
    }

    private PipelineDefinition
    buildPipelineDefinition(List<MBodyTableFeatures> tables,
                            MBodyDesc dpDesc) {
        MutablePipelineDefinition def = new DefaultMutablePipelineDefinition();

        try {
            for (MBodyTableFeatures tf: tables) {
                TableContext ct = processTableFeatures(tf, dpDesc);
                def.addTableContext(ct);
            }
        } catch (OpenflowException ex) {
            log.error(LM_ERR_PROC_TF_REPLY, ex.getMessage());
            return null;
        } catch (Exception e) {
            log.error(LM_ERR_PROC_TF_REPLY, e.getMessage());
            return null;
        }
        return def.toImmutable();
    }

    private TableContext processTableFeatures(MBodyTableFeatures tf,
                                              MBodyDesc dpDesc)
            throws OpenflowException {
        DefaultMutableTableContext ctx = new DefaultMutableTableContext();
        TableId tableId = tf.getTableId();
        ctx.tableId(tableId);
        ctx.maxEntries(tf.getMaxEntries());
        ctx.dpDesc(dpDesc);

        Set<TableFeaturePropType> unseen =
                EnumSet.allOf(TableFeaturePropType.class);

        for (TableFeatureProp prop : tf.getProps()) {
            unseen.remove(prop.getType());
            processTableFeatureProp(prop, ctx, tableId);
        }

        patchContext(ctx, unseen);

        return ctx.toImmutable();
    }

    /**
     * The MISS variant of a table feature property type may be omitted from
     * the table features response if it contains the same capabilities as the
     * matching regular table feature property type.  It may be necessary to
     * patch the pipeline definition to include the MISS variant if it was
     * not included in the response.
     * As the EXPERIMENTER type is not considered in our pipeline definition,
     * no additional processing is expected for the EXPERIMENTER_MISS type.
     *
     * @param ctx the table context
     * @param unseen the set of table feature prop types not seen in the reply
     */
    private void patchContext(DefaultMutableTableContext ctx,
                              Set<TableFeaturePropType> unseen) {
        for (TableFeaturePropType pt : unseen) {
            switch (pt) {
                case INSTRUCTIONS_MISS:
                case WRITE_ACTIONS_MISS:
                case APPLY_ACTIONS_MISS:
                    Set<? extends OfpCodeBasedEnum> regCaps =
                            ctx.getCapabilities(pt.regular());
                    if (regCaps != null)
                        ctx.addCapability(pt, regCaps);
                    break;

                case NEXT_TABLES_MISS:
                    for (TableId tableId : ctx.getNextTableIdSet())
                        ctx.addNextTableMiss(tableId);
                    break;

                case WRITE_SETFIELD_MISS:
                case APPLY_SETFIELD_MISS:
                    Set<? extends OxmFieldType> regMatchCaps =
                            ctx.getMatchFieldCapabilities(pt.regular());
                    if (regMatchCaps != null)
                        ctx.addMatchFieldCapability(pt, regMatchCaps);
                    break;

                case EXPERIMENTER:
                case EXPERIMENTER_MISS:
                    // ignore these for pipeline definition
                    break;

                default:
                    log.warn(MSG_MISSING_PROP_TYPE, pt);
            }
        }
    }

    private void processTableFeatureProp(TableFeatureProp prop,
                                         MutableTableContext ctx,
                                         TableId table) throws OpenflowException {
        TableFeaturePropType propType = prop.getType();

        switch (propType) {
            case INSTRUCTIONS:
            case INSTRUCTIONS_MISS:
                TableFeaturePropInstr instr = (TableFeaturePropInstr) prop;
                ctx.addCapability(propType, instr.getSupportedInstructions());
                break;

            case NEXT_TABLES:
                TableFeaturePropNextTable nt = (TableFeaturePropNextTable) prop;
                Set<TableId> nextSet = validNext(table, nt.getNextTables());

                for (TableId nextTable : nextSet)
                    ctx.addNextTable(nextTable);
                break;

            case NEXT_TABLES_MISS:
                TableFeaturePropNextTable nmt = (TableFeaturePropNextTable) prop;
                Set<TableId> nextMissSet = validNext(table, nmt.getNextTables());

                for (TableId nextTable : nextMissSet)
                    ctx.addNextTableMiss(nextTable);
                break;

            case WRITE_ACTIONS:
            case APPLY_ACTIONS:
            case WRITE_ACTIONS_MISS:
            case APPLY_ACTIONS_MISS:
                TableFeaturePropAction act = (TableFeaturePropAction) prop;
                ctx.addCapability(propType, act.getSupportedActions());
                break;

            case WRITE_SETFIELD:
            case APPLY_SETFIELD:
            case APPLY_SETFIELD_MISS:
            case WRITE_SETFIELD_MISS:
            case WILDCARDS:
                TableFeaturePropOxm oxm = (TableFeaturePropOxm)prop;
                ctx.addMatchFieldCapability(oxm.getType(),
                                            oxm.getSupportedFieldTypes());
                break;

            case MATCH:
                TableFeaturePropOxm poxm = (TableFeaturePropOxm)prop;
                for (OxmBasicFieldType type: poxm.getSupportedFieldTypes())
                    ctx.addMatchField(type, poxm.hasMaskBitSet(type));
                break;

            case EXPERIMENTER:
            case EXPERIMENTER_MISS:
                log.info(MSG_EXPR_PROP_TYPE, propType);
                break;

            default :
                throw new OpenflowException(E_NO_PROP_TYPE + propType);
        }
    }

    /**
     * To be considered a valid next table identification, the table must be
     * greater than the base table as the pipeline processing is only allowed to
     * move forward.  The valid determination is delegated to the
     * {@link TableId#compareTo(TableId)}.
     *
     * @param base the base table id
     * @param possibleSet the possible next tables
     * @return the set of valid next tables
     */
    private Set<TableId> validNext(TableId base, Set<TableId> possibleSet) {
        Set<TableId> valid = new HashSet<>();
        for (TableId id : possibleSet) {
            if (base.compareTo(id) < 0)
                valid.add(id);
            else
                log.warn(E_BAD_NEXT_TABLE_ID, base, id);
        }
        return valid;
    }

}
