/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx.api;

import org.opendaylight.yangtools.yang.common.Uint32;

public final class NiciraConstants {

    public static final Uint32 NX_VENDOR_ID = Uint32.valueOf(0x00002320L).intern();

    public static final Uint32 NX_NSH_VENDOR_ID = Uint32.valueOf(0x005AD650L).intern();

    private NiciraConstants() {

    }
}
