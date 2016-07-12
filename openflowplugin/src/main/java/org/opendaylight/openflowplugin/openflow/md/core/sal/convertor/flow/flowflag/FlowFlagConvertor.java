/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.flow.flowflag;

import java.util.Collection;
import java.util.Collections;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * converting from MD-SAL match model into appropriate OF-API match model
 *
 * @param <E> type of converted match
 */
public interface FlowFlagConvertor<E> extends Convertor<FlowModFlags, E, VersionConvertorData> {

    @Override
    default Collection<Class<? extends DataContainer>> getTypes() {
        return Collections.singleton(DataContainer.class);
    }

    @Override
    default E convert(FlowModFlags source, VersionConvertorData data) {
        return convert(source);
    }
    
    /**
     * @param source flow mode flags
     * @return converted match (into OF-API model)
     */
    E convert(FlowModFlags source);
}
