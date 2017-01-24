/**
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.multipart;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;

public class MultipartReplyFlowStatsDeserializerTest extends AbstractMultipartDeserializerTest{

    @Test
    public void testDeserialize() throws Exception {

    }

    @Override
    protected int getType() {
        return MultipartType.OFPMPFLOW.getIntValue();
    }
}