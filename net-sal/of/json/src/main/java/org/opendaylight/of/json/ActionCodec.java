/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.GroupId;
import org.opendaylight.of.lib.dt.QueueId;
import org.opendaylight.of.lib.instr.*;
import org.opendaylight.of.lib.match.MFieldBasic;
import org.opendaylight.of.lib.match.MatchField;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.EthernetType;

import java.util.Iterator;
import java.util.Map;

import static org.opendaylight.of.json.CodecUtils.*;
import static org.opendaylight.of.lib.CommonUtils.hex;
import static org.opendaylight.of.lib.CommonUtils.parseHexInt;
import static org.opendaylight.of.lib.instr.ActionFactory.createAction;
import static org.opendaylight.util.ByteUtils.hexWithPrefix;
import static org.opendaylight.util.ByteUtils.parseHexWithPrefix;
import static org.opendaylight.util.JSONUtils.fromKey;
import static org.opendaylight.util.JSONUtils.toKey;

/**
 * A JSON codec capable of encoding and decoding {@link Action} objects.
 * 
 * @author Shaila Shree
 * @author Simon Hunt
 */
public class ActionCodec extends OfJsonCodec<Action> {

    // unit test access
    static final String ROOTS = "flow_actions";
    static final String ROOT = "flow_action";

    private static final String PORT = "port";
    private static final String DATA = "data";

    private volatile MatchFieldCodec mfc;

    protected ActionCodec() {
        super(ROOT, ROOTS);
    }

    @Override
    public ObjectNode encode(Action act) {
        ObjectNode node = null;
        ActionType type = act.getActionType();
        String name = toKey(type);

        switch (type) {
            case OUTPUT:
                node = encodePort(name, ((ActOutput) act).getPort(),
                        act.getVersion());
                break;

            case COPY_TTL_OUT:
            case COPY_TTL_IN:
            case DEC_MPLS_TTL:
            case POP_VLAN:
            case POP_PBB:
            case DEC_NW_TTL:
                node = encodeName(name);
                break;

            case SET_MPLS_TTL:
                node = encodeTtl(name, ((ActSetMplsTtl) act).getTtl());
                break;

            case PUSH_VLAN:
                node = encodeEth(name, ((ActPushVlan) act).getEthernetType());
                break;

            case PUSH_MPLS:
                node = encodeEth(name, ((ActPushMpls) act).getEthernetType());
                break;

            case POP_MPLS:
                node = encodeEth(name, ((ActPopMpls) act).getEthernetType());
                break;

            case SET_QUEUE:
                ActSetQueue q = (ActSetQueue) act;
                node = encodeQueue(name, q.getQueueId(), q.getPort(),
                        act.getVersion());
                break;

            case GROUP:
                node = encodeGroup(name, ((ActGroup) act).getGroupId());
                break;

            case SET_NW_TTL:
                node = encodeTtl(name, ((ActSetNwTtl) act).getTtl());
                break;

            case SET_FIELD:
                node = encodeSetField(name, act);
                break;

            case PUSH_PBB:
                node = encodeEth(name, ((ActPushPbb) act).getEthernetType());
                break;

            case EXPERIMENTER:
                node = encodeExp(name, (ActExperimenter) act);
                break;

            default:
                break;
        }
        return node;
    }

    private ObjectNode encodePort(String name, BigPortNumber port,
                                  ProtocolVersion pv) {
        return encodeBigPort(objectNode(), name, port, pv);
    }

    private ObjectNode encodeName(String name) {
        return objectNode().put(name, true);
    }

    private ObjectNode encodeTtl(String name, int ttl) {
        return objectNode().put(name, ttl);
    }

    private ObjectNode encodeEth(String name, EthernetType type) {
        return objectNode().put(name, encodeEthType(type));
    }

    private ObjectNode encodeQueue(String name, QueueId qid,
                                   BigPortNumber port, ProtocolVersion pv) {
        ObjectNode node = objectNode().put(name, encodeQueueId(qid));
        if (port != null)
            encodeBigPort(node, PORT, port, pv);
        return node;
    }

    private ObjectNode encodeGroup(String name, GroupId gid) {
        return objectNode().put(name, encodeGroupId(gid));
    }

    private ObjectNode encodeSetField(String name, Action action) {
        MatchField mf = ((ActSetField) action).getField();
        ObjectNode node = objectNode();
        node.put(name, getMatchFieldCodec().encode(mf));
        return node;
    }

    private ObjectNode encodeExp(String name, ActExperimenter exp) {
        return objectNode().put(name, hex(exp.getExpId().encodedId()))
                .put(DATA, hexWithPrefix(exp.getData()));
    }

    @Override
    public Action decode(ObjectNode node) {
        String field = null;
        JsonNode version = null;
        JsonNode port = null;
        JsonNode data = null;
        JsonNode value = null;

        Iterator<Map.Entry<String, JsonNode>> nodes = node.fields();

        while (nodes.hasNext()) {
            Map.Entry<String, JsonNode> entry = nodes.next();
            if (entry.getKey().equals(VERSION)) {
                version = entry.getValue();
            } else if (entry.getKey().equals(PORT)) {
                port = entry.getValue();
            } else if (entry.getKey().equals(DATA)) {
                data = entry.getValue();
            } else {
                field = entry.getKey();
                value = entry.getValue();
            }
        }

        ProtocolVersion pv = decodeProtocolVersion(version);
        ActionType type = fromKey(ActionType.class, field);
        Action action = null;

        switch (type) {
            case OUTPUT:
                action = decodePort(pv, type, value);
                break;

            case COPY_TTL_OUT:
            case COPY_TTL_IN:
            case DEC_MPLS_TTL:
            case POP_VLAN:
            case DEC_NW_TTL:
            case POP_PBB:
                action = decodeName(pv, type);
                break;

            case SET_MPLS_TTL:
            case SET_NW_TTL:
                action = decodeTtl(pv, type, value);
                break;

            case PUSH_VLAN:
            case PUSH_MPLS:
            case POP_MPLS:
            case PUSH_PBB:
                action = decodeEth(pv, type, value);
                break;

            case SET_QUEUE:
                action = decodeQueue(pv, type, value, port);
                break;

            case GROUP:
                action = decodeGroup(pv, type, value);
                break;

            case SET_FIELD:
                action = decodeSetField(pv, type, value);
                break;

            case EXPERIMENTER:
                action = decodeExp(pv, type, value, data);
                break;

            default:
                break;
        }
        return action;
    }

    private Action decodePort(ProtocolVersion pv, ActionType type,
                              JsonNode value) {
        return createAction(pv, type, decodeBigPort(value));
    }

    private Action decodeName(ProtocolVersion pv, ActionType type) {
        return createAction(pv, type);
    }

    private Action decodeEth(ProtocolVersion pv, ActionType type,
                             JsonNode value) {
        return createAction(pv, type, decodeEthType(value));
    }

    private Action decodeTtl(ProtocolVersion pv, ActionType type,
                             JsonNode value) {
        return createAction(pv, type, value.asInt());
    }

    private Action decodeQueue(ProtocolVersion pv, ActionType type,
                               JsonNode value, JsonNode port) {
        return port == null
            ? createAction(pv, type, decodeQueueId(value))
            : createAction(pv, type, decodeQueueId(value), decodeBigPort(port));
    }

    private Action decodeGroup(ProtocolVersion pv, ActionType type,
                               JsonNode value) {
        return createAction(pv, type, decodeGroupId(value));
    }

    private Action decodeSetField(ProtocolVersion pv, ActionType type,
                                  JsonNode value) {
        // add protocol version
        ObjectNode matchField = (ObjectNode) value;
        matchField.put(VERSION, encodeProtocolVersion(pv));
        return createAction(pv, type,
                (MFieldBasic) getMatchFieldCodec().decode(matchField));
    }

    private Action decodeExp(ProtocolVersion pv, ActionType type,
                             JsonNode value, JsonNode data) {
        return createAction(pv, type, parseHexInt(value.asText()),
                parseHexWithPrefix(data.asText()));
    }

    private MatchFieldCodec getMatchFieldCodec() {
        if (mfc == null)
            mfc = (MatchFieldCodec)
                    OfJsonFactory.instance().codec(MatchField.class);
        return mfc;
    }
}
