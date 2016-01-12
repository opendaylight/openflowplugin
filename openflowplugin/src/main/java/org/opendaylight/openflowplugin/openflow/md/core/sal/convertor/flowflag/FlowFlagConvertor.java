/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.flowflag;

import java.math.BigInteger;

import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;

/**
 * converting from MD-SAL match model into appropriate OF-API match model
 * @param <E>  type of converted match
 */
public interface FlowFlagConvertor<E> extends Convertor<FlowModFlags, E> {
    
    /**
     * @param source flow mode flags
     * @param datapathid  datapath id
     * @return converted match (into OF-API model)
     */
    @Override
    E convert(FlowModFlags source,BigInteger datapathid);
}
