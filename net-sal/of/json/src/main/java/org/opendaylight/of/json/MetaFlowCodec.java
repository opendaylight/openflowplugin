/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.json;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.opendaylight.of.controller.MetaFlow;
import org.opendaylight.of.lib.mp.MBodyFlowStats;
import org.opendaylight.util.json.AbstractJsonCodec;
import org.opendaylight.util.json.JsonCodec;

import java.util.List;

/**
 * A JSON codec able to encode (but not decode) {@link MetaFlow} entities.
 * Note that this codec delegates to
 * {@link org.opendaylight.of.json.MBodyFlowStatsCodec} and
 *
 * @author Simon Hunt
 */
public class MetaFlowCodec extends AbstractJsonCodec<MetaFlow>
        implements JsonCodec<MetaFlow> {

    private static final String E_NOT_SUPPORTED = "decoding not supported";

    private static final String ROOTS = "metaflows";
    private static final String ROOT = "metaflow";

    private static final String FLOWS = "flows";

    private volatile MBodyFlowStatsCodec fsc;

    /**
     * Creates a meta-flow codec.
     */
    protected MetaFlowCodec() {
        super(ROOT, ROOTS);
    }

    @Override
    public ObjectNode encode(MetaFlow metaFlow) {
        ObjectNode node = objectNode();
        node.put(FLOWS, encodeFlowList(metaFlow.flowStats()));
        return node;
    }

    @Override
    public MetaFlow decode(ObjectNode jsonNodes) {
        throw new UnsupportedOperationException(E_NOT_SUPPORTED);
    }

    private ArrayNode encodeFlowList(List<MBodyFlowStats> flowList) {
        return getFlowStatsCodec().encodeList(flowList);
    }

    private MBodyFlowStatsCodec getFlowStatsCodec() {
        OfJsonFactory factory = (OfJsonFactory) OfJsonFactory.instance();
        if (fsc == null)
            fsc = (MBodyFlowStatsCodec) factory.codec(MBodyFlowStats.class);
        return fsc;
    }

}
