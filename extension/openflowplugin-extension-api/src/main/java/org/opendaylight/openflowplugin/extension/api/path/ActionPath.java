/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.api.path;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * @author msunal
 *
 */
public enum ActionPath implements AugmentationPath {

    /**
     * openflowplugin-extension-general.yang
     * <pre>
     * module: opendaylight-inventory
     *    +--rw nodes
     *       +--rw node* [id]
     *          +--rw flownode:table* [id]
     *          |  +--rw flownode:flow* [id]
     *          |  |  +--rw flownode:instructions
     *          |  |  |  +--rw flownode:instruction* [order]
     *          |  |  |     +--rw (instruction)?
     *          |  |  |        +--:(write-actions-case)
     *          |  |  |        |  +--rw flownode:write-actions
     *          |  |  |        |     +--rw flownode:action* [order]
     *          |  |  |        |        +--rw (action)?
     * </pre>
     */
    NODES_NODE_TABLE_FLOW_INSTRUCTIONS_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION_EXTENSIONLIST_EXTENSION(null),
    /**
     * openflowplugin-extension-general.yang
     * <pre>
     * module: opendaylight-inventory
     *    +--rw nodes
     *       +--rw node* [id]
     *          +--rw flownode:table* [id]
     *          |  +--rw flownode:flow* [id]
     *          |  |  +--rw flownode:instructions
     *          |  |  |  +--rw flownode:instruction* [order]
     *          |  |  |     +--rw (instruction)?
     *          |  |  |        +--:(apply-actions-case)
     *          |  |  |        |  +--rw flownode:apply-actions
     *          |  |  |        |     +--rw flownode:action* [order]
     *          |  |  |        |        +--rw (action)?
     * 
     * </pre>
     */
    NODES_NODE_TABLE_FLOW_INSTRUCTIONS_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION_EXTENSIONLIST_EXTENSION(null),
    /**
     * openflowplugin-extension-general.yang
     * <pre>
     * module: opendaylight-flow-statistics
     * notifications:
     *    +---n flows-statistics-update             
     *    |  +--ro flow-and-statistics-map-list* [flow-id]
     *    |  |  +--ro instructions
     *    |  |  |  +--ro instruction* [order]
     *    |  |  |     +--ro (instruction)?
     *    |  |  |        +--:(write-actions-case)
     *    |  |  |        |  +--ro write-actions
     *    |  |  |        |     +--ro action* [order]
     *    |  |  |        |        +--ro (action)?
     * </pre>
     */
    FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION(null),
    /**
     * openflowplugin-extension-general.yang
     * <pre>
     * module: opendaylight-flow-statistics
     * notifications:
     *    +---n flows-statistics-update             
     *    |  +--ro flow-and-statistics-map-list* [flow-id]
     *    |  |  +--ro instructions
     *    |  |  |  +--ro instruction* [order]
     *    |  |  |     +--ro (instruction)?
     *    |  |  |        +--:(apply-actions-case)
     *    |  |  |        |  +--ro apply-actions
     *    |  |  |        |     +--ro action* [order]
     *    |  |  |        |        +--ro (action)?
     * </pre>
     */
    FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION(null),
    /**
     * openflowplugin-extension-general.yang
     * <pre>
     * module: opendaylight-group-statistics
     * notifications:
     *    +---n group-desc-stats-updated    
     *    |  +--ro group-desc-stats* [group-id]
     *    |  |  +--ro buckets
     *    |  |     +--ro bucket* [bucket-id]
     *    |  |        +--ro action* [order]
     *    |  |           +--ro (action)?
     * </pre>
     */
    GROUPDESCSTATSUPDATED_GROUPDESCSTATS_BUCKETS_BUCKET_ACTION(null),
    /**
     * openflowplugin-extension-general.yang
     * <pre>
     * module: opendaylight-direct-statistics
     * notifications:
     *    +---n get-flow-statistics
     *    |  +--ro flow-and-statistics-map-list* [flow-id]
     *    |  |  +--ro instructions
     *    |  |  |  +--ro instruction* [order]
     *    |  |  |     +--ro (instruction)?
     *    |  |  |        +--:(write-actions-case)
     *    |  |  |        |  +--ro write-actions
     *    |  |  |        |     +--ro action* [order]
     *    |  |  |        |        +--ro (action)?
     * </pre>
     */
    RPCFLOWSSTATISTICS_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION(null),
    /**
     * openflowplugin-extension-general.yang
     * <pre>
     * module: opendaylight-direct-statistics
     * notifications:
     *    +---n get-flow-statistics
     *    |  +--ro flow-and-statistics-map-list* [flow-id]
     *    |  |  +--ro instructions
     *    |  |  |  +--ro instruction* [order]
     *    |  |  |     +--ro (instruction)?
     *    |  |  |        +--:(apply-actions-case)
     *    |  |  |        |  +--ro apply-actions
     *    |  |  |        |     +--ro action* [order]
     *    |  |  |        |        +--ro (action)?
     * </pre>
     */
    RPCFLOWSSTATISTICS_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION(null);

    private final InstanceIdentifier<Extension> iid;

    private ActionPath(InstanceIdentifier<Extension> iid) {
        this.iid = iid;
    }

    @Override
    public final InstanceIdentifier<Extension> getInstanceIdentifier() {
        return iid;
    }

}
