/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.flow;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorKey;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ResultInjector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFlagsV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;

public final class FlowFlagsInjector {
    private FlowFlagsInjector() {
    }

    @SuppressWarnings("unchecked")
    public static <F, T> void inject(Optional<F> source, T target, short version) {
        F sourceResult;
        if (source.isPresent()) {
            sourceResult = source.get();
        } else if (version == EncodeConstants.OF10_VERSION_ID) {
            sourceResult = (F) FlowFlagsV10Convertor.defaultResult();
        } else {
            sourceResult = (F) FlowFlagsConvertor.defaultResult();
        }

        final Map<ConvertorKey, ResultInjector<?, ?>> injectorMap = new HashMap<>();
        addInjectors(injectorMap);

        final ResultInjector<F, T> injection = (ResultInjector<F, T>) injectorMap
                .get(new ConvertorKey(version, target.getClass()));

        injection.inject(sourceResult, target);
    }

    private static void addInjectors(final Map<ConvertorKey, ResultInjector<?, ?>> injectionMapping) {
        // OF-1.3|FlowModFlags --> FlowModInputBuilder
        injectionMapping.put(new ConvertorKey(OFConstants.OFP_VERSION_1_3, FlowModInputBuilder.class),
            (ResultInjector<FlowModFlags, FlowModInputBuilder>)(value, target) -> target.setFlags(value));

        // OF-1.3|FlowModFlagsV10 --> FlowModInputBuilder
        injectionMapping.put(new ConvertorKey(OFConstants.OFP_VERSION_1_0, FlowModInputBuilder.class),
            (ResultInjector<FlowModFlagsV10, FlowModInputBuilder>)(value, target) -> target.setFlagsV10(value));
    }
}
