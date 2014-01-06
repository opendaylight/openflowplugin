/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common;

import java.math.BigInteger;


/**
 * converting from MD-SAL model into appropriate OF-API model
 * @param <FROM>  type of source
 * @param <TO>  type of result
 */
public interface Convertor<FROM, TO> {
    
    /**
     * @param source
     * @return converted match (into OF-API model)
     */
    TO convert(FROM source,BigInteger datapathid);
}
