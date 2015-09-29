/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match;

import java.math.BigInteger;

import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;

/**
 * converting from MD-SAL match model into appropriate OF-API match model
 * @param <E>  type of converted match
 */
public interface MatchConvertor<E> extends Convertor<Match, E> {
    
    /**
     * @param source match input
     * @param datapathid datapath id
     * @return converted match (into OF-API model)
     */
    @Override
    E convert(Match source,BigInteger datapathid);
}
