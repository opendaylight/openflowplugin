/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match;

import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * converting from MD-SAL match model into appropriate OF-API match model
 *
 * @param <E> type of converted match
 */
public interface MatchConvertor<E> extends Convertor<Match, E, VersionConvertorData> {

    @Override
    default Class<? extends DataContainer> getType() {
        return Match.class;
    }

    @Override
    default E convert(Match source, VersionConvertorData data) {
        return convert(source);
    }
    
    /**
     * @param source match input
     * @return converted match (into OF-API model)
     */
    E convert(Match source);
}
