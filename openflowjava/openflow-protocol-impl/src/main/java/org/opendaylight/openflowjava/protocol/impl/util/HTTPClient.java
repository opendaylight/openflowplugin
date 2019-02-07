/*
 * Copyright (c) 2019 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.opendaylight.openflowjava.protocol.impl.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.DatatypeConverter;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("IllegalCatch")
public class HTTPClient {
    private static final Logger LOG = LoggerFactory.getLogger(HTTPClient.class);
    public static final int TIME_OUT = 20;
    public static final int PORT = 8181;
    public static final char URL_SEPAR_TOR = ':';
    public static final String PREFX = "http://";
    public static final String TEST_URL = "/apidoc/explorer/index.html";

    protected HTTPClient() {
    }

    public static HTTPResponse sendRequest(HTTPRequest request) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        if (httpclient == null) {
            throw new ClientProtocolException("Couldn't create an HTTP client");
        } else {
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(request.getTimeout())
                    .setConnectTimeout(request.getTimeout()).build();
            String method = request.getMethod();
            Object httprequest;
            if (method.equalsIgnoreCase("GET")) {
                httprequest = new HttpGet(request.getUri());
            } else {
                StringEntity sentEntity;
                if (method.equalsIgnoreCase("POST")) {
                    httprequest = new HttpPost(request.getUri());
                    if (request.getEntity() != null) {
                        sentEntity = new StringEntity(request.getEntity());
                        sentEntity.setContentType(request.getContentType());
                        ((HttpEntityEnclosingRequestBase)httprequest).setEntity(sentEntity);
                    }
                } else if (method.equalsIgnoreCase("PUT")) {
                    httprequest = new HttpPut(request.getUri());
                    if (request.getEntity() != null) {
                        sentEntity = new StringEntity(request.getEntity());
                        sentEntity.setContentType(request.getContentType());
                        ((HttpEntityEnclosingRequestBase)httprequest).setEntity(sentEntity);
                    }
                } else {
                    if (!method.equalsIgnoreCase("DELETE")) {
                        httpclient.close();
                        throw new IllegalArgumentException("This profile class only supports GET, POST,"
                                + " PUT, and DELETE methods");
                    }

                    httprequest = new HttpDelete(request.getUri());
                }
            }

            ((HttpRequestBase)httprequest).setConfig(requestConfig);
            Iterator headerIterator = request.getHeaders().keySet().iterator();

            while (headerIterator.hasNext()) {
                String header = (String)headerIterator.next();
                Iterator valueIterator = ((List)request.getHeaders().get(header)).iterator();

                while (valueIterator.hasNext()) {
                    ((HttpRequestBase)httprequest).addHeader(header, (String)valueIterator.next());
                }
            }

            CloseableHttpResponse response = httpclient.execute((HttpUriRequest)httprequest);

            HTTPResponse var22;
            try {
                HttpEntity receivedEntity = response.getEntity();
                int httpResponseCode = response.getStatusLine().getStatusCode();
                HTTPResponse ans = new HTTPResponse();
                HashMap<String, List<String>> headerMap = new HashMap();
                HeaderIterator it = response.headerIterator();

                while (it.hasNext()) {
                    Header header = it.nextHeader();
                    String name = header.getName();
                    String value = header.getValue();
                    if (headerMap.containsKey(name)) {
                        ((List)headerMap.get(name)).add(value);
                    } else {
                        List<String> list = new ArrayList();
                        list.add(value);
                        headerMap.put(name, list);
                    }
                }

                ans.setHeaders(headerMap);
                if (httpResponseCode <= 299) {
                    ans.setStatus(response.getStatusLine().getStatusCode());
                    if (receivedEntity != null) {
                        ans.setEntity(EntityUtils.toString(receivedEntity));
                    } else {
                        ans.setEntity(null);
                    }

                    var22 = ans;
                    return var22;
                }

                ans.setStatus(httpResponseCode);
                ans.setEntity(response.getStatusLine().getReasonPhrase());
                var22 = ans;
            } finally {
                response.close();
            }

            return var22;
        }
    }

    @SuppressFBWarnings("DM_DEFAULT_ENCODING")
    public static boolean isReachable(String targetUrl) {
        try {
            targetUrl = PREFX + targetUrl + URL_SEPAR_TOR + PORT + TEST_URL;
            LOG.info("Target URI:: {}", targetUrl);
            HttpURLConnection httpUrlConnection = (HttpURLConnection)(new URL(targetUrl)).openConnection();
            String userpass = "admin:admin";
            String basicAuth = "Basic " + DatatypeConverter.printBase64Binary(userpass.getBytes());
            httpUrlConnection.setRequestProperty("Authorization", basicAuth);
            httpUrlConnection.getInputStream();
            httpUrlConnection.setConnectTimeout(TIME_OUT);
            LOG.info("Execution completed....");
            int responseCode = httpUrlConnection.getResponseCode();
            LOG.info("Response code: {}", responseCode);
            return responseCode == 200;
        } catch (UnknownHostException var6) {
            return false;
        } catch (Exception var7) {
            LOG.debug("the exception is ",var7);
            return false;
        }
    }
}

