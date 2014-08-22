/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.controller.impl;

import org.opendaylight.of.controller.ControllerStats;
import org.opendaylight.of.controller.DataPathListener;
import org.opendaylight.of.controller.MessageListener;
import org.opendaylight.of.lib.OpenflowException;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.dt.DataPathInfo;
import org.opendaylight.of.lib.mp.MBodyDesc;
import org.opendaylight.of.lib.mp.MBodyPortStats;
import org.opendaylight.of.lib.mp.MBodyTableFeatures;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.net.BigPortNumber;

import java.util.List;
import java.util.Set;

/**
 * Adapter for {@link ListenerService}.
 *
 * @author Simon Hunt
 */
public class ListenerServiceAdapter implements ListenerService {
    @Override public void addDataPathListener(DataPathListener listener) { }
    @Override public void removeDataPathListener(DataPathListener listener) { }
    @Override public void addMessageListener(MessageListener listener, Set<MessageType> types) { }
    @Override public void removeMessageListener(MessageListener listener) { }
    @Override public Set<DataPathInfo> getAllDataPathInfo() { return null; }
    @Override public DataPathInfo getDataPathInfo(DataPathId dpid) { return null; }
    @Override public ProtocolVersion versionOf(DataPathId dpid) { return null; }
    @Override public ControllerStats getStats() { return null; }
    @Override public List<MBodyPortStats> getPortStats(DataPathId dpid) { return null; }
    @Override public MBodyPortStats getPortStats(DataPathId dpid, BigPortNumber portNumber) { return null; }
    @Override public MessageFuture enablePort(DataPathId dpid, BigPortNumber port, boolean enable) { return null; }
    @Override public void send(OpenflowMessage msg, DataPathId dpid, int auxId) throws OpenflowException { }
    @Override public void send(OpenflowMessage msg, DataPathId dpid) throws OpenflowException { }
    @Override public void send(List<OpenflowMessage> msgs, DataPathId dpid) throws OpenflowException { }
    @Override public void sendFuture(DataPathMessageFuture f, OpenflowMessage... msgs) throws OpenflowException { }
    @Override public void sendFuture(DataPathMessageFuture f) throws OpenflowException { }
    @Override public DataPathMessageFuture findFuture(OpenflowMessage msg, DataPathId dpid) { return null; }
    @Override public void failFuture(DataPathMessageFuture f, Throwable cause) { }
    @Override public void failFuture(DataPathMessageFuture f, OfmError msg) { }
    @Override public void successFuture(DataPathMessageFuture f, OpenflowMessage msg) { }
    @Override public void cancelFuture(DataPathMessageFuture f) { }
    @Override public void countDrop(int totalLen) { }
    @Override public List<MBodyTableFeatures.Array> getCachedTableFeatures(DataPathId dpid) { return null; }
    @Override public MBodyDesc getCachedDeviceDesc(DataPathId dpid) { return null; }
}
