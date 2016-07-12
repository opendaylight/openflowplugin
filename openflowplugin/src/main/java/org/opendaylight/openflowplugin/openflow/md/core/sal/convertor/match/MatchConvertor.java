/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match;

import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertReactorConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;

/**
 * converting from MD-SAL match model into appropriate OF-API match model
 *
 * @param <E> type of converted match
 */
public interface MatchConvertor<E> extends ConvertReactorConvertor<Match, E> {
    /**
     * @param source match input
     * @param convertorExecutor
     * @return converted match (into OF-API model)
     */
    E convert(Match source, ConvertorExecutor convertorExecutor);
}
