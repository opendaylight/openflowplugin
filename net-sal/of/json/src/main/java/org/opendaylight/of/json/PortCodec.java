/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.json;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.opendaylight.of.lib.msg.*;

import static org.opendaylight.of.json.CodecUtils.decodeMac;
import static org.opendaylight.of.lib.msg.PortFactory.createPort;

/**
 * A JSON codec capable of encoding and decoding {@link Port} objects.
 * 
 * @author Liem Nguyen
 */
public class PortCodec extends OfJsonCodec<Port> {
    private static final String PEER_FEATURES = "peer_features";
    private static final String SUPPORTED_FEATURES = "supported_features";
    private static final String ADVERTISED_FEATURES = "advertised_features";
    private static final String CURRENT_FEATURES = "current_features";
    private static final String STATE = "state";
    private static final String CONFIG = "config";
    private static final String MAX_SPEED = "max_speed";
    private static final String CURRENT_SPEED = "current_speed";
    private static final String MAC = "mac";
    private static final String NAME = "name";
    private static final String ID = "id";
    public static final String ROOT  = "port";
    public static final String ROOTS = "ports";

    public PortCodec() {
        super(ROOT, ROOTS);
    }

    @Override
    public ObjectNode encode(Port pojo) {
        ObjectNode port = objectNode();

        port.put(ID, pojo.getLogicalNumber());
        port.put(NAME, pojo.getName());
        port.put(MAC, pojo.getHwAddress().toString());
        if (pojo.getCurrentSpeed() > 0L)
            port.put(CURRENT_SPEED, pojo.getCurrentSpeed());
        if (pojo.getMaxSpeed() > 0L)
            port.put(MAX_SPEED, pojo.getMaxSpeed());
        port.put(CONFIG, fromEnums(pojo.getConfig()));
        port.put(STATE, fromEnums(pojo.getState()));        
        port.put(CURRENT_FEATURES, fromEnums(pojo.getCurrent()));
        port.put(ADVERTISED_FEATURES, fromEnums(pojo.getAdvertised()));
        port.put(SUPPORTED_FEATURES, fromEnums(pojo.getSupported()));
        port.put(PEER_FEATURES, fromEnums(pojo.getPeer()));

        return port;
    }

    @Override
    public Port decode(ObjectNode node) {
        MutablePort port = createPort(version(node));
        
        port.portNumber(Port.getBigPortNumber(node.get(ID).textValue()));
        port.name(node.get(NAME).textValue());
        port.hwAddress(decodeMac(node.get(MAC)));
        if (node.get(CURRENT_SPEED) != null)
            port.currentSpeed(node.get(CURRENT_SPEED).longValue());
        if (node.get(MAX_SPEED) != null)
            port.maxSpeed(node.get(MAX_SPEED).longValue());
        port.config(toEnums((ArrayNode) node.get(CONFIG),
                                              PortConfig.class));
        port.state(toEnums((ArrayNode) node.get(STATE),
                                            PortState.class));
        port.current(toEnums((ArrayNode) node
            .get(CURRENT_FEATURES), PortFeature.class));
        port.advertised(toEnums((ArrayNode) node
            .get(ADVERTISED_FEATURES), PortFeature.class));
        port.supported(toEnums((ArrayNode) node
            .get(SUPPORTED_FEATURES), PortFeature.class));
        port.peer(toEnums((ArrayNode) node
            .get(PEER_FEATURES), PortFeature.class));

        return (Port) port.toImmutable();
    }

}
