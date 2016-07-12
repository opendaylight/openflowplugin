/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match;

import java.util.Map;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertReactor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertReactorConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorKey;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ResultInjector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;

/**
 *
 */
public class MatchReactor extends ConvertReactor<Match> {

    private static final MatchReactor INSTANCE = new MatchReactor();

    private MatchReactor() {
        //NOOP
    }

    /**
     * @return singleton
     */
    public static MatchReactor getInstance() {
        return INSTANCE;
    }

    @Override
    protected void initMappings(final Map<Short, ConvertReactorConvertor<Match, ?>> conversions,
                                final Map<ConvertorKey, ResultInjector<?, ?>> injections) {
        MatchReactorMappingFactory.addMatchConvertors(conversions);
        MatchReactorMappingFactory.addMatchIjectors(injections);
    }
}
