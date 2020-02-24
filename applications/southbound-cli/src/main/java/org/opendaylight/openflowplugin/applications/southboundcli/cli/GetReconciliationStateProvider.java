/*
 * Copyright (c) 2019 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.southboundcli.cli;
import com.google.common.net.InetAddresses;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.reflect.Type;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.infrautils.diagstatus.ClusterMemberInfo;
import org.opendaylight.infrautils.diagstatus.shell.HttpClientService;
import org.opendaylight.infrautils.diagstatus.shell.HttpRequest;
import org.opendaylight.infrautils.diagstatus.shell.HttpResponse;
import org.opendaylight.openflowplugin.applications.frm.ReconciliationJMXServiceMBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "openflow", name = "getreconciliationstate",
        description = "Print reconciliation state for all devices")
public class GetReconciliationStateProvider extends OsgiCommandSupport {

    @Option(name = "-d", description = "Node Id")
    String nodeId;

    private static final Logger LOG = LoggerFactory.getLogger(GetReconciliationStateProvider.class);
    private static final String URL_PREFIX = "http://";
    private static final String URL_SEPARATOR = "/";
    private static final String URL_SEPARATOR_COLON = ":";
    private static final String HTTP_OPERATION = "GET";
    private static final String HTTP_JOL_OKIA_BASE_URI = "/jolokia/exec/";
    private static final int HTTP_TIMEOUT = 5000;
    private static final String JMX_OBJECT_NAME
            = "org.opendaylight.openflowplugin.frm:type=ReconciliationState";
    private static final String JMX_ATTRIBUTE_NAME = "acquireReconciliationStates";
    private static final String JMX_REST_HTTP_AUTH_UNAME_PWD = "admin:admin";
    private ReconciliationJMXServiceMBean reconciliationJMXServiceMBean;
    private ClusterMemberInfo clusterMemberInfoProvider;
    private HttpClientService httpClientService;


    public GetReconciliationStateProvider(final ReconciliationJMXServiceMBean reconciliationJMXServiceMBean,
                                          final ClusterMemberInfo clusterMemberInfoProvider,
                                          final HttpClientService httpClientService) {
        this.reconciliationJMXServiceMBean = reconciliationJMXServiceMBean;
        this.clusterMemberInfoProvider = clusterMemberInfoProvider;
        this.httpClientService = httpClientService;
    }

    @Override
    protected Object doExecute() throws Exception {
        LOG.info("The getter is getting executed");
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
            String reconciliationState = getReconciliationStateForNode();
            if (reconciliationState != null) {
                String status = String.format("%-17s %-50s", nodeId, reconciliationState);
                result.add(status);
                printReconciliationStates(result);
            } else {
                session.getConsole().println("Reconciliation data not available for the specified node");
            }
        }
        return null;
    }

    private String getReconciliationStateForNode() {
        //first checking reconciliation state locally
        String reconciliationState = reconciliationJMXServiceMBean.acquireReconciliationStates().get(nodeId);
        if (reconciliationState == null) {
            //checking reconciliation state in the cluster
            reconciliationState = getClusterwideReconcilitionStates().get(nodeId);
        }
        return reconciliationState;
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
        List<String> clusterIPAddresses = clusterMemberInfoProvider.getClusterMembers().stream()
                .map(s -> String.valueOf(s)).collect(Collectors.toList());
        LOG.debug("The ip address of nodes in the cluster : {}", clusterIPAddresses);
        if (!clusterIPAddresses.isEmpty()) {
            String selfAddress = clusterMemberInfoProvider.getSelfAddress() != null
                    ? clusterMemberInfoProvider.getSelfAddress().toString() : ("localhost");
            LOG.trace("The ip address of local node is {}", selfAddress);
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
            String getReconcilationRemoteResponse = invokeRemoteRestOperation(ipAddress);
            if (getReconcilationRemoteResponse != null) {
                JsonElement rootObj = new JsonParser().parse(getReconcilationRemoteResponse);
                String remoteJMXOperationResult = rootObj.getAsJsonObject().get("value").toString();
                Type type = new TypeToken<HashMap<String, String>>() {
                }.getType();
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

    @SuppressFBWarnings("DM_DEFAULT_ENCODING")
    private String invokeRemoteRestOperation(String ipAddress) throws Exception {
        String restUrl = buildRemoteReconcilationUrl(ipAddress);
        LOG.info("invokeRemoteReconcilationState() REST URL: {}", restUrl);
        HttpRequest request = new HttpRequest();
        request.setUri(restUrl);
        request.setMethod(HTTP_OPERATION);
        request.setTimeout(HTTP_TIMEOUT);
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        List<String> header = new ArrayList<>();
        String authString = JMX_REST_HTTP_AUTH_UNAME_PWD;
        byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
        String authStringEnc = new String(authEncBytes);
        header.add("Basic " + authStringEnc);
        headers.put("Authorization", header);
        header.add("application/json");
        headers.put("Accept", header);
        request.setHeaders(headers);
        request.setContentType("application/json");
        LOG.debug("sending http request for accessing remote reconcilation");
        HttpResponse response = httpClientService.sendRequest(request);
        // Response code for success should be 200
        Integer httpResponseCode = response.getStatus();
        LOG.debug("http response received for remote reconcilation {}", httpResponseCode);
        String respStr = response.getBody();
        if (httpResponseCode > 299) {
            LOG.error("Non-200 http response code received {} for URL {}", httpResponseCode, restUrl);
            if (respStr == null || respStr.isEmpty()) {
                return "Service Status Retrieval failed. HTTP Response Code : " + httpResponseCode + "\n";
            }
        }
        LOG.trace("HTTP Response is - {} for URL {}", respStr, restUrl);

        return respStr;
    }


    String buildRemoteReconcilationUrl(String host) {
        String targetHostAsString;
        InetAddress hostInetAddress = InetAddresses.forString(host);
        if (hostInetAddress instanceof Inet6Address) {
            targetHostAsString = '[' + hostInetAddress.getHostAddress() + ']';
        } else {
            targetHostAsString = hostInetAddress.getHostAddress();
        }
        return new StringBuilder().append(URL_PREFIX).append(targetHostAsString).append(URL_SEPARATOR_COLON)
                .append(httpClientService.getHttpPort()).append(HTTP_JOL_OKIA_BASE_URI).append(JMX_OBJECT_NAME)
                .append(URL_SEPARATOR).append(JMX_ATTRIBUTE_NAME).toString();
    }
}