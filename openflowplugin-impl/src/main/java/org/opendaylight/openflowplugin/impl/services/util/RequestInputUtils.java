/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services.util;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 26.3.2015.
 */
public final class RequestInputUtils {

    private RequestInputUtils() {
        throw new IllegalStateException("This class should not be instantiated.");
    }

    public static MultipartRequestInputBuilder createMultipartHeader(MultipartType multipart,
                                                               Long xid, Short version) {
        MultipartRequestInputBuilder mprInput = new MultipartRequestInputBuilder();
        mprInput.setType(multipart);
        mprInput.setVersion(version);
        mprInput.setXid(xid);
        mprInput.setFlags(new MultipartRequestFlags(false));
        return mprInput;
    }

}
