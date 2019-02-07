/*
 * Copyright (c) 2019 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.southboundcli.cli;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.infrautils.diagstatus.internal.ClusterMemberInfoImpl;
import org.opendaylight.openflowjava.protocol.impl.util.HTTPClient;
import org.opendaylight.openflowjava.protocol.impl.util.JMXOperationsUtil;
import org.opendaylight.openflowplugin.applications.frm.ReconciliationJMXServiceMBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "openflow", name = "getreconciliationstate",
        description = "Print reconciliation state for all devices")
public class GetReconciliationStateProvider extends OsgiCommandSupport {

    @Option(name = "-d", description = "Node Id")
    String nodeId;

    private static final Logger LOG = LoggerFactory.getLogger(GetReconciliationStateProvider.class);
    private static final String JMX_OBJECT_NAME
            = "com.ericsson.sdncp.services.openflowplugin.frm:type=ReconciliationState";
    private static final String JMX_ATTRIBUTE_NAME = "acquireReconciliationStates";
    private ReconciliationJMXServiceMBean reconciliationJMXServiceMBean;

    public void setReconciliationJMXServiceMBean(final ReconciliationJMXServiceMBean reconciliationJMXServiceMBean) {
        this.reconciliationJMXServiceMBean = reconciliationJMXServiceMBean;
    }

    @Override
    protected Object doExecute() throws Exception {
        LOG.info("the getter is getting executed");
        List<String> result = new ArrayList<>();
        Map<String, String> reconciliationStates  = getClusterwideReconcilitionStates();
        if (nodeId == null) {
            if (!reconciliationStates.isEmpty()) {
                reconciliationStates.forEach((datapathId, reconciliationState) -> {
                    String status = String.format("%-17s %-50s", datapathId, reconciliationState);
                    result.add(status);
                });
                printReconciliationStates(result);
            } else {
                session.getConsole().println("Reconciliation data not available");
            }
        }
        else {
                //first checking reconciliation state locally
            String reconciliationState = reconciliationJMXServiceMBean.acquireReconciliationStates().get(nodeId);
            if (reconciliationState != null) {
                String status = String.format("%-17s %-50s", nodeId, reconciliationState);
                result.add(status);
                printReconciliationStates(result);
            } else {
                session.getConsole().println("Reconciliation data not available");
            }
        }
        return null;
    }

    private void printReconciliationStates(List<String> result) {
        session.getConsole().println(getHeaderOutput());
        session.getConsole().println(getLineSeparator());
        result.stream().forEach(p -> session.getConsole().println(p));
    }

    private String getHeaderOutput() {
        String header = String.format("%-17s %-25s %-25s", "DatapathId", "Reconciliation Status",
                "Reconciliation Time");
        return header;
    }

    private String getLineSeparator() {
        return "-------------------------------------------------------------------";
    }

    @SuppressWarnings("IllegalCatch")
    private Map<String,String> getClusterwideReconcilitionStates() {
        Map<String,String>  clusterwideReconcStates = new HashMap<>();
        ClusterMemberInfoImpl cluster = new ClusterMemberInfoImpl();
        List<String> clusterIPAddresses = cluster.getClusterMembers().stream()
                .map(s -> String.valueOf(s)).collect(Collectors.toList());
        if (!clusterIPAddresses.isEmpty()) {
            String selfAddress = cluster.getSelfAddress() != null ? cluster.getSelfAddress().toString() : ("localhost");
            for (String memberAddress : clusterIPAddresses) {
                try {
                    if (memberAddress.equals(selfAddress)) {
                        clusterwideReconcStates.putAll(getLocalStatusSummary());
                    } else {
                        clusterwideReconcStates.putAll(getRemoteReconciliationStates(memberAddress));
                    }
                } catch (Exception e) {
                    LOG.error("Exception while reaching Host {}", memberAddress, e);
                }
            }
        } else {
            LOG.info("Could not obtain cluster members or the cluster-command is being executed locally\n");
        }
        return clusterwideReconcStates;
    }

    @SuppressWarnings("IllegalCatch")
    private Map<String, String> getRemoteReconciliationStates(String ipAddress) {
        Map<String, String> jmxReconciliationStates = new HashMap<>();
        try {
            if (HTTPClient.isReachable(ipAddress)) {
                String rawJsonOutput = JMXOperationsUtil.execRemoteJMXOperation(JMX_OBJECT_NAME,
                        JMX_ATTRIBUTE_NAME, ipAddress, null);
                JsonElement rootObj = new JsonParser().parse(rawJsonOutput);
                String remoteJMXOperationResult = rootObj.getAsJsonObject().get("value").toString();
                Type type = new TypeToken<HashMap<String, String>>(){}.getType();
                jmxReconciliationStates.putAll(new Gson().fromJson(remoteJMXOperationResult, type));
            }
        } catch (Exception e) {
            LOG.error("Exception during reconciliation states from device with ip address {}", ipAddress, e);
        }
        return jmxReconciliationStates;
    }

    private Map<String,String> getLocalStatusSummary() {
        return reconciliationJMXServiceMBean.acquireReconciliationStates();
    }
}