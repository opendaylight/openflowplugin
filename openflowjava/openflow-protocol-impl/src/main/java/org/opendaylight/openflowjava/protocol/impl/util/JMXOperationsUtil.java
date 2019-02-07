/*
 * Copyright (c) 2019 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import org.apache.commons.codec.binary.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by efiijjp on 10/19/2014.
 */
@SuppressWarnings("IllegalCatch")
public class JMXOperationsUtil {

    private static final Logger LOG = LoggerFactory.getLogger(JMXOperationsUtil.class);
    private static final String JMX_REST_HTTP_OPERATION = "GET";
    //private static final String JMX_REST_HTTP_JOLOKIA_BASE_URI = "/controller/nb/v2/jolokia/exec/";
    private static final String JMX_REST_HTTP_JOL_OKIA_BASE_URI = "/jolokia/exec/";
    private static final String JMX_REST_HTTP_AUTH_UNAME_PWD = "admin:admin";
    private static final String JMX_REST_HTTP_PORT = "8181";
    private static final int TIME_OUT = 5000;

    protected JMXOperationsUtil(){
    }

    public static String invokeLocalJMXOperation(String objectName, String operationToExec) {
        LOG.debug("invokeLocalJMXOperation - ObjectName : {} , operation : {}", objectName, operationToExec);
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        LOG.debug("invokeLocalJMXOperation - MBS instance retrieved ! ");
        Object result = null;
        String resultStr = "JMX ERROR";
        if (mbs != null) {
            try {
                result = mbs.invoke(new ObjectName(objectName), operationToExec, null, null);
                if (result != null) {
                    resultStr = (String)result;
                    LOG.debug("invokeLocalJMXOperation - result is {}", resultStr);
                }
                else {
                    LOG.debug("invokeLocalJMXOperation - result is NULL");
                }
            }
            catch (MalformedObjectNameException monEx) {
                LOG.error("Error accesing Local MBean {} and Service {}", objectName, operationToExec, monEx);
                resultStr = "CRITICAL EXCEPTION : Malformed Object Name Exception";
            }
            catch (MBeanException mbEx) {
                LOG.error("Error accesing Local MBean {} and Service {}", objectName, operationToExec, mbEx);
                resultStr = "CRITICAL EXCEPTION : MBean Exception";
            }
            catch (InstanceNotFoundException infEx) {
                resultStr = "Diag Status Service Down";
            }
            catch (ReflectionException rEx) {
                LOG.error("Error accesing Local MBean {} and Service {}", objectName, operationToExec, rEx);
                resultStr = "CRITICAL EXCEPTION : Reflection Exception";
            }
        }
        return resultStr;
    }

    @SuppressFBWarnings("DM_DEFAULT_ENCODING")
    public static String invokeRemoteJMXOperationREST(String host, String mbeanName,
                                                      String operation) throws Exception {
        int httpResponseCode = 400;
        String restUrl = buildRemoteJMXReSTUrl(host, mbeanName, operation);
        String respStr = "ERROR accessing JMX URL - " + restUrl;
        LOG.info("invokeRemoteJMXOperationREST : REST JMX URL - {}", restUrl);
        try {
            HTTPRequest request = new HTTPRequest();
            request.setUri(restUrl);
            request.setMethod(JMX_REST_HTTP_OPERATION);
            request.setTimeout(TIME_OUT);
            // To compensate for respond
            // within default timeout during
            // IT so setting an indefinite
            // timeout till the issue is
            // sorted out

            Map<String, List<String>> headers = new HashMap<String, List<String>>();
            String authString = JMX_REST_HTTP_AUTH_UNAME_PWD;
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
            String authStringEnc = new String(authEncBytes);
            List<String> header = new ArrayList<String>();
            header.add("Basic " + authStringEnc);
            headers.put("Authorization", header);
            header = new ArrayList<String>();
            header.add("application/json");
            headers.put("Accept", header);
            request.setHeaders(headers);
            request.setContentType("application/json");
            LOG.debug("invokeRemoteJMXOperationREST : sending request ... ");
            HTTPResponse response = HTTPClient.sendRequest(request);
            LOG.debug("invokeRemoteJMXOperationREST : response received ... ");
            // Response code for success should be 2xx
            httpResponseCode = response.getStatus();
            LOG.debug("invokeRemoteJMXOperationREST : HTTP Response code is - {}", httpResponseCode);
            if (httpResponseCode > 299) {
                LOG.error("invokeRemoteJMXOperationREST : Non-200 HTTP Response code is  {} for URL {}",
                        httpResponseCode, restUrl);
                return respStr + " HTTP Response Code : " + Integer.toString(httpResponseCode);
            }
            LOG.debug("invokeRemoteJMXOperationREST : HTTP Response is - {} for URL {}", response.getEntity(), restUrl);
            respStr = response.getEntity();
        } catch (Exception e) {
            LOG.error("Error accesing URL {}", restUrl, e);
            throw e;
        }
        return respStr;
    }

    @SuppressFBWarnings("DM_DEFAULT_ENCODING")
    public static String invokeRemoteJMXOperationREST(String host, String mbeanName, String operation,
                                                      List<String> operationArgs) throws Exception {
        // initialize response code to indicate error
        int httpResponseCode = 400;
        String restUrl = buildRemoteJMXReSTUrlWithMultipleArg(host, mbeanName, operation, operationArgs);
        String respStr = "ERROR accessing JMX URL - " + restUrl;
        LOG.info("invokeRemoteJMXOperationRESTWithArg : REST JMX URL - {}", restUrl);
        try {
            HTTPRequest request = new HTTPRequest();
            request.setUri(restUrl);
            request.setMethod(JMX_REST_HTTP_OPERATION);
            request.setTimeout(TIME_OUT);
            // To compensate for respond
            // within default timeout during
            // IT so setting an indefinite
            // timeout till the issue is
            // sorted out
            Map<String, List<String>> headers = new HashMap<String, List<String>>();
            String authString = JMX_REST_HTTP_AUTH_UNAME_PWD;
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
            String authStringEnc = new String(authEncBytes);
            List<String> header = new ArrayList<String>();
            header.add("Basic " + authStringEnc);
            headers.put("Authorization", header);
            header = new ArrayList<String>();
            header.add("application/json");
            headers.put("Accept", header);
            request.setHeaders(headers);
            request.setContentType("application/json");
            LOG.debug("invokeRemoteJMXOperationRESTWithArg : sending request ... ");
            HTTPResponse response = HTTPClient.sendRequest(request);
            LOG.debug("invokeRemoteJMXOperationREST : response received ... ");
            // Response code for success should be 2xx
            httpResponseCode = response.getStatus();
            LOG.debug("invokeRemoteJMXOperationRESTWithArg : HTTP Response code is - {}", httpResponseCode);
            if (httpResponseCode > 299) {
                LOG.error("invokeRemoteJMXOperationRESTWithArg : Non-200 HTTP Response code is  {} for URL {}",
                        httpResponseCode, restUrl);
                return respStr + " HTTP Response Code : " + Integer.toString(httpResponseCode);
            }
            LOG.debug("invokeRemoteJMXOperationRESTWithArg : HTTP Response is - {} for URL {}",
                    response.getEntity(), restUrl);
            respStr = response.getEntity();
        } catch (Exception e) {
            LOG.error("Error accesing URL {}", restUrl, e);
            throw e;
        }
        return respStr;
    }

    private static String buildRemoteJMXReSTUrl(String host, String mbeanName, String operation) {
        return "http://" + host + ":" + JMX_REST_HTTP_PORT + JMX_REST_HTTP_JOL_OKIA_BASE_URI + mbeanName + "/"
                + operation;
    }

    private static String buildRemoteJMXReSTUrlWithMultipleArg(String host, String mbeanName, String operation,
                                                               List<String> args) {
        StringBuilder jmxUrl = new StringBuilder();
        jmxUrl.append("http://" + host + ":" + JMX_REST_HTTP_PORT + JMX_REST_HTTP_JOL_OKIA_BASE_URI + mbeanName + "/"
                + operation);
        for (String str : args) {
            jmxUrl.append("/").append(str);
        }
        return jmxUrl.toString();
    }

    public static String execRemoteJMXOperation(String jmxServiceObjectName, String invokeCommand, String ipAddress,
                                                List args) throws Exception {
        String operationResult;
        try {
            LOG.debug("runClusterwideCommand : Peer cluster address : {}", ipAddress);
            if (args != null && !args.isEmpty()) {
                operationResult = JMXOperationsUtil.invokeRemoteJMXOperationREST(ipAddress, jmxServiceObjectName,
                        invokeCommand, args);
            }
            else {
                operationResult = JMXOperationsUtil.invokeRemoteJMXOperationREST(ipAddress, jmxServiceObjectName,
                        invokeCommand);
            }
            LOG.debug("runClusterwideCommand : Remote JMX Response successful: {}", operationResult);
        }
        catch (Exception ex) {
            LOG.error("runClusterwideCommand : Error Invoking REST on Remote Node ", ex);
            throw ex;
        }
        return operationResult;
    }
}
