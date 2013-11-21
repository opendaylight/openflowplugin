/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.session;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.openflow.md.core.sal.ModelDrivenSwitchImpl;
import org.opendaylight.openflowplugin.openflow.md.core.sal.SalRegistrationManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.Port;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatus;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * @author mirehak
 */
public class SessionContextOFImpl implements SessionContext {

    private GetFeaturesOutput features;
    private ConnectionConductor primaryConductor;
    private ConcurrentHashMap<SwitchConnectionDistinguisher, ConnectionConductor> auxiliaryConductors;
    private boolean valid;
    private SwitchConnectionDistinguisher sessionKey;
    private IMessageDispatchService mdService;
    private final AtomicLong xid;
    private final Map<Long, Port> physicalPorts;
    private final Map<Long, Boolean> portBandwidth;

    /**
     * default ctor
     */
    public SessionContextOFImpl() {
        auxiliaryConductors = new ConcurrentHashMap<>();
        mdService = new MessageDispatchServiceImpl(this);
        xid = new AtomicLong();
        this.physicalPorts = new HashMap<Long, Port>();
        this.portBandwidth = new HashMap<Long, Boolean>();
    }

    @Override
    public ConnectionConductor getPrimaryConductor() {
        return primaryConductor;
    }

    @Override
    public ConnectionConductor getAuxiliaryConductor(
            SwitchConnectionDistinguisher auxiliaryKey) {
        return auxiliaryConductors.get(auxiliaryKey);
    }

    @Override
    public void addAuxiliaryConductor(
            SwitchConnectionDistinguisher auxiliaryKey,
            ConnectionConductor conductor) {
        auxiliaryConductors.put(auxiliaryKey, conductor);
    }

    @Override
    public Set<Entry<SwitchConnectionDistinguisher, ConnectionConductor>> getAuxiliaryConductors() {
        return Collections.unmodifiableSet(auxiliaryConductors.entrySet());
    }

    @Override
    public GetFeaturesOutput getFeatures() {
        return features;
    }

    /**
     * @param features
     *            the features to set
     */
    public void setFeatures(GetFeaturesOutput features) {
        this.features = features;
    }

    /**
     * @param primaryConductor
     *            the primaryConductor to set
     */
    public void setPrimaryConductor(ConnectionConductor primaryConductor) {
        this.primaryConductor = primaryConductor;
    }

    @Override
    public ConnectionConductor removeAuxiliaryConductor(
            SwitchConnectionDistinguisher connectionCookie) {
        return auxiliaryConductors.remove(connectionCookie);
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    /**
     * @param sessionKey the sessionKey to set
     */
    public void setSessionKey(SwitchConnectionDistinguisher sessionKey) {
        this.sessionKey = sessionKey;
    }

    @Override
    public SwitchConnectionDistinguisher getSessionKey() {
        return sessionKey;
    }

    @Override
    public IMessageDispatchService getMessageDispatchService() {
        return mdService;
    }

    @Override
    public Long getNextXid() {
        return xid.incrementAndGet();
    }

    @Override
    public Map<Long, Port> getPhysicalPorts() {
        return this.physicalPorts;
    }

    @Override
    public Set<Long> getPorts() {
        return this.physicalPorts.keySet();
    }

    @Override
    public Port getPhysicalPort(Long portNumber) {
        return this.physicalPorts.get(portNumber);
    }

    @Override
    public Boolean getPortBandwidth(Long portNumber) {
        return this.portBandwidth.get(portNumber);
    }

    @Override
    public boolean isPortEnabled(long portNumber) {
        return isPortEnabled(physicalPorts.get(portNumber));
    }

    @Override
    public boolean isPortEnabled(Port port) {
        if (port == null) {
            return false;
        }
        if (port.getConfig().isPortDown()) {
            return false;
        }
        if (port.getState().isLinkDown()) {
            return false;
        }
        if (port.getState().isBlocked()) {
            return false;
        }
        return true;
    }

    @Override
    public List<Port> getEnabledPorts() {
        List<Port> result = new ArrayList<Port>();
        synchronized (this.physicalPorts) {
            for (Port port : physicalPorts.values()) {
                if (isPortEnabled(port)) {
                    result.add(port);
                }
            }
        }
        return result;
    }

    @Override
    public void processPortStatusMsg(PortStatus msg) {
        Port port = msg;
        if (msg.getReason().getIntValue() == 2) {
            updatePhysicalPort(port);
        } else if (msg.getReason().getIntValue() == 0) {
            updatePhysicalPort(port);
        } else if (msg.getReason().getIntValue() == 1) {
            deletePhysicalPort(port);
        }

    }

    private void updatePhysicalPort(Port port) {
        Long portNumber = port.getPortNo();
        physicalPorts.put(portNumber, port);

        NotificationProviderService nps = OFSessionUtil.getSessionManager().getNotificationProviderService();

        SessionContext context = primaryConductor.getSessionContext();
        GetFeaturesOutput features = context.getFeatures();
        BigInteger datapathId = features.getDatapathId();
        InstanceIdentifier<Node> identifier = SalRegistrationManager.identifierFromDatapathId(datapathId);
        NodeRef nodeRef = new NodeRef(identifier);
        NodeId nodeId = SalRegistrationManager.nodeIdFromDatapathId(datapathId);


        ModelDrivenSwitchImpl ofSwitch = new ModelDrivenSwitchImpl(nodeId, identifier, context);
        salSwitches.put(identifier, ofSwitch);
        ofSwitch.register(providerContext);


        // TODO: need to get

        // TODO: this makes no damn sense unless you mean does it have a bandwidth
        portBandwidth
                .put(portNumber,
                        ( (port.getCurrentFeatures().is_100gbFd())
                          |(port.getCurrentFeatures().is_100mbFd()) | (port.getCurrentFeatures().is_100mbHd())
                          | (port.getCurrentFeatures().is_10gbFd()) | (port.getCurrentFeatures().is_10mbFd())
                          | (port.getCurrentFeatures().is_10mbHd()) | (port.getCurrentFeatures().is_1gbFd())
                          | (port.getCurrentFeatures().is_1gbHd()) | (port.getCurrentFeatures().is_1tbFd())
                          | (port.getCurrentFeatures().is_40gbFd()) | (port.getCurrentFeatures().isAutoneg())
                          | (port.getCurrentFeatures().isCopper()) | (port.getCurrentFeatures().isFiber())
                          | (port.getCurrentFeatures().isOther()) | (port.getCurrentFeatures().isPause())
                          | (port.getCurrentFeatures().isPauseAsym()) ) );
    }

    private void deletePhysicalPort(Port port) {
        Long portNumber = port.getPortNo();
        physicalPorts.remove(portNumber);
        portBandwidth.remove(portNumber);
    }
}
