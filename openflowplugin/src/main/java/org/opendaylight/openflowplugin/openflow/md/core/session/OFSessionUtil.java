/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.session;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.common.api.TransactionStatus;
import org.opendaylight.controller.md.sal.common.api.data.DataModification;
import org.opendaylight.controller.sal.binding.api.data.DataBrokerService;
import org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.openflow.md.core.TranslatorKey;
import org.opendaylight.openflowplugin.openflow.md.queue.PopListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mirehak
 */
public abstract class OFSessionUtil {

    private static final Logger LOG = LoggerFactory
            .getLogger(OFSessionUtil.class);

    /**
     * @param connectionConductor
     * @param features
     * @param version
     */
    public static void registerSession(ConnectionConductor connectionConductor,
            GetFeaturesOutput features, short version) {
        SwitchConnectionDistinguisher sessionKey = createSwitchSessionKey(features
                .getDatapathId());
        SessionContext sessionContext = getSessionManager().getSessionContext(sessionKey);
        if (LOG.isDebugEnabled()) {
            LOG.debug("registering sessionKey: {}", Arrays.toString(sessionKey.getId()));
        }

        if (features.getAuxiliaryId() == null || features.getAuxiliaryId() == 0) {
            // handle primary
            if (sessionContext != null) {
                LOG.warn("duplicate datapathId occured while registering new switch session: "
                        + dumpDataPathId(features.getDatapathId()));
                getSessionManager().invalidateSessionContext(sessionKey);
            }
            // register new session context (based primary conductor)
            SessionContextOFImpl context = new SessionContextOFImpl();
            context.setPrimaryConductor(connectionConductor);
            context.setFeatures(features);
            context.setSessionKey(sessionKey);
            connectionConductor.setSessionContext(context);
            context.setValid(true);
            getSessionManager().addSessionContext(sessionKey, context);
        } else {
            // handle auxiliary
            if (sessionContext == null) {
                throw new IllegalStateException("unexpected auxiliary connection - primary connection missing: "
                        + dumpDataPathId(features.getDatapathId()));
            } else {
                // register auxiliary conductor into existing sessionContext
                SwitchConnectionDistinguisher auxiliaryKey = createConnectionCookie(features);
                if (sessionContext.getAuxiliaryConductor(auxiliaryKey) != null) {
                    LOG.warn("duplicate datapathId+auxiliary occured while registering switch session: "
                            + dumpDataPathId(features.getDatapathId())
                            + " | "
                            + features.getAuxiliaryId());
                    getSessionManager().invalidateAuxiliary(sessionKey,
                            auxiliaryKey);
                }

                sessionContext.addAuxiliaryConductor(auxiliaryKey,
                        connectionConductor);
                connectionConductor.setSessionContext(sessionContext);
                connectionConductor.setConnectionCookie(auxiliaryKey);
            }
        }

        // check registration result
        SessionContext resulContext = getSessionManager().getSessionContext(sessionKey);
        if (resulContext == null) {
            throw new IllegalStateException("session context registration failed");
        } else {
            if (!resulContext.isValid()) {
                throw new IllegalStateException("registered session context is invalid");
            }
        }
    }

    /**
     * @param datapathId
     * @return readable version of datapathId (hex)
     */
    public static String dumpDataPathId(BigInteger datapathId) {
        return datapathId.toString(16);
    }

    /**
     * @param datapathId
     * @return new session key
     */
    public static SwitchConnectionDistinguisher createSwitchSessionKey(
            BigInteger datapathId) {
        SwitchSessionKeyOFImpl key = new SwitchSessionKeyOFImpl();
        key.setDatapathId(datapathId);
        key.initId();
        return key;
    }

    /**
     * @param features
     * @return connection cookie key
     * @see #createConnectionCookie(BigInteger, short)
     */
    public static SwitchConnectionDistinguisher createConnectionCookie(
            GetFeaturesOutput features) {
        return createConnectionCookie(features.getDatapathId(),
                features.getAuxiliaryId());
    }

    /**
     * @param datapathId
     * @param auxiliaryId
     * @return connection cookie key
     */
    public static SwitchConnectionDistinguisher createConnectionCookie(
            BigInteger datapathId, short auxiliaryId) {
        SwitchConnectionCookieOFImpl cookie = null;
        if (auxiliaryId != 0) {
            cookie = new SwitchConnectionCookieOFImpl();
            cookie.setDatapathId(datapathId);
            cookie.setAuxiliaryId(auxiliaryId);
            cookie.initId();
        }
        return cookie;
    }

    /**
     * @return session manager singleton instance
     */
    public static SessionManager getSessionManager() {
        return SessionManagerOFImpl.getInstance();
    }

    /**
    * @return session manager listener Map
    */
    public static Map<TranslatorKey, Collection<IMDMessageTranslator<OfHeader, List<DataObject>>>> getTranslatorMap() {
        return getSessionManager().getTranslatorMapping();
    }

    /**
     * @return pop listener Map
     */
    public static Map<Class<? extends DataObject>, Collection<PopListener<DataObject>>> getPopListenerMapping() {
        return getSessionManager().getPopListenerMapping();
    }
    
    /**
     * clean flows from all tables under given switch (target = md-sal:config datastore)
     * @param switchId
     * @param dataBrokerService
     * @param tables 
     */
    public static void cleanFlowsConfig(NodeId switchId, DataBrokerService dataBrokerService, Short tables) {
        if (dataBrokerService == null) {
            return;
        }
        
        DataModification<InstanceIdentifier<?>, DataObject> modification = dataBrokerService.beginTransaction();
        NodeKey switchKey = new NodeKey(switchId);

        for (short tableId = 0; tableId < tables; tableId++) {
            InstanceIdentifier<Table> pathToSwitchTable = InstanceIdentifier.builder(Nodes.class)
                    .child(Node.class, switchKey).augmentation(FlowCapableNode.class)
                    .child(Table.class, new TableKey(tableId)).build();
            Table tbl = (Table) modification.readConfigurationData(pathToSwitchTable);
            if (tbl != null) {
                for (Flow flow : tbl.getFlow()) {
                    LOG.info("flow: {}", flow);
                    
                    InstanceIdentifier<Flow> pathToFlow = InstanceIdentifier.builder(pathToSwitchTable)
                            .child(Flow.class, flow.getKey()).build();
                    modification.removeConfigurationData(pathToFlow);
                }
            }
        }

        commitClean(switchId, modification, "flow");
    }
    
    /**
     * clean groups under given switch (target = md-sal:config datastore)
     * @param switchId
     * @param dataBrokerService
     */
    public static void cleanGroupsConfig(NodeId switchId, DataBrokerService dataBrokerService) {
        if (dataBrokerService == null) {
            return;
        }
        
        DataModification<InstanceIdentifier<?>, DataObject> modification = dataBrokerService.beginTransaction();
        NodeKey switchKey = new NodeKey(switchId);

        InstanceIdentifier<FlowCapableNode> pathToSwitchFlowCapable = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, switchKey).augmentation(FlowCapableNode.class)
                .build();
        FlowCapableNode node = (FlowCapableNode) modification.readConfigurationData(pathToSwitchFlowCapable);
        if (node != null) {
            for (Group group : node.getGroup()) {
                LOG.info("group: {}", group);
                InstanceIdentifier<Group> pathToGroup = InstanceIdentifier.builder(pathToSwitchFlowCapable)
                        .child(Group.class, group.getKey()).build();
                modification.removeConfigurationData(pathToGroup);
            }
        }

        commitClean(switchId, modification, "group");
    }
    
    /**
     * clean meters under given switch (target = md-sal:config datastore)
     * @param switchId
     * @param dataBrokerService
     */
    public static void cleanMetersConfig(NodeId switchId, DataBrokerService dataBrokerService) {
        if (dataBrokerService == null) {
            return;
        }
        
        DataModification<InstanceIdentifier<?>, DataObject> modification = dataBrokerService.beginTransaction();
        NodeKey switchKey = new NodeKey(switchId);

        InstanceIdentifier<FlowCapableNode> pathToSwitchFlowCapable = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, switchKey).augmentation(FlowCapableNode.class)
                .build();
        FlowCapableNode node = (FlowCapableNode) modification.readConfigurationData(pathToSwitchFlowCapable);
        if (node != null) {
            for (Meter meter : node.getMeter()) {
                LOG.info("meter: {}", meter);
                InstanceIdentifier<Meter> pathToMeter = InstanceIdentifier.builder(pathToSwitchFlowCapable)
                        .child(Meter.class, meter.getKey()).build();
                modification.removeConfigurationData(pathToMeter);
            }
        }

        commitClean(switchId, modification, "meter");
    }

    /**
     * @param switchId
     * @param modification
     * @param elementName name of cleaned element
     */
    private static void commitClean(NodeId switchId,
            DataModification<InstanceIdentifier<?>, DataObject> modification, String elementName) {
        Future<RpcResult<TransactionStatus>> commitFuture = modification.commit();
        try {
            RpcResult<TransactionStatus> result = commitFuture.get();
            TransactionStatus status = result.getResult();
            LOG.debug("Status of {} Removed Transaction: {}", elementName, status);
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Failed to remove {} configuration for switch {} due to {}", 
                    elementName, switchId, e.getMessage(), e);
        }
    }

}
