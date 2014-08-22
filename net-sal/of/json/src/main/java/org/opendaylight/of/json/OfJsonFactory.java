/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.json;

import org.opendaylight.of.controller.ControllerStats;
import org.opendaylight.of.controller.MetaFlow;
import org.opendaylight.of.controller.pkt.SplMetric;
import org.opendaylight.of.lib.dt.DataPathInfo;
import org.opendaylight.of.lib.instr.Action;
import org.opendaylight.of.lib.instr.Instruction;
import org.opendaylight.of.lib.match.MatchField;
import org.opendaylight.of.lib.mp.*;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.json.AbstractJsonFactory;
import org.opendaylight.util.json.JsonFactory;

/**
 * A JSON factory for all Openflow structures.  This contains all the codecs
 * needed to support JSON representation of Openflow data structures.
 *
 * @author Liem Nguyen
 */
public class OfJsonFactory extends AbstractJsonFactory {
    private static final OfJsonFactory factory = new OfJsonFactory();

    protected OfJsonFactory() {
        addCodecs(DataPathInfo.class, new DataPathInfoCodec());
        addCodecs(Port.class, new PortCodec());
        addCodecs(MatchField.class, new MatchFieldCodec());
        addCodecs(Action.class, new ActionCodec());
        addCodecs(Instruction.class, new InstructionCodec());
        addCodecs(MBodyFlowStats.class, new MBodyFlowStatsCodec());
        addCodecs(OfmFlowMod.class, new OfmFlowModCodec());
        addCodecs(MBodyMeterConfig.class, new MeterCodec());
        addCodecs(OfmGroupMod.class, new OfmGroupModCodec());
        addCodecs(MBodyGroupDescStats.class, new MBodyGroupDescStatsCodec());
        addCodecs(MBodyPortStats.class, new MBodyPortStatsCodec());
        addCodecs(OfmMeterMod.class, new OfmMeterModCodec());
        addCodecs(OfmExperimenter.class, new OfmExperimenterCodec());
        addCodecs(MBodyExperimenter.class, new MBodyExperimenterCodec());
        addCodecs(MBodyGroupStats.class, new MBodyGroupStatsCodec());
        addCodecs(MBodyMeterStats.class, new MBodyMeterStatsCodec());
        addCodecs(MBodyMeterFeatures.class, new MBodyMeterFeaturesCodec());
        addCodecs(MBodyGroupFeatures.class, new MBodyGroupFeaturesCodec());
        addCodecs(ControllerStats.class, new ControllerStatsCodec());
        addCodecs(SplMetric.class, new SplMetricCodec());
        addCodecs(MetaFlow.class, new MetaFlowCodec());
        // Add more codecs here
    }

    public static JsonFactory instance() {
        return factory;
    }
}
