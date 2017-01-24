/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.onf.deserializer;

import io.netty.buffer.ByteBuf;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.extension.onf.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.ExperimenterIdError;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;

/**
 * Tests for {@link org.opendaylight.openflowplugin.extension.onf.deserializer.OnfExperimenterErrorFactory}.
 */
public class OnfExperimenterErrorFactoryTest {

    private final OFDeserializer<ErrorMessage> factory = new OnfExperimenterErrorFactory();

    @Test
    public void testVersion() {
        ByteBuf buffer = ByteBufUtils.buildBuf("ff ff 08 fc 00 00 00 01");
        ErrorMessage builtByFactory = factory.deserialize(buffer);
        ByteBufUtils.checkHeaderV13(builtByFactory);
    }

    @Test
    public void testDeserializeBase() {
        ByteBuf buffer = ByteBufUtils.buildBuf("ff ff 08 fc 4f 4e 46 00");
        ErrorMessage builtByFactory = factory.deserialize(buffer);
        Assert.assertEquals("Wrong type", EncodeConstants.EXPERIMENTER_VALUE, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong type string", "EXPERIMENTER", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong experimenter ID", EncodeConstants.ONF_EXPERIMENTER_ID,
                builtByFactory.getAugmentation(ExperimenterIdError.class).getExperimenter().getValue().intValue());
        Assert.assertNull("Data is not null", builtByFactory.getData());
    }

    @Test
    public void testDeserializeCodes() {
        ByteBuf buffer = ByteBufUtils.buildBuf("ff ff 08 fc 00 00 00 01");
        ErrorMessage builtByFactory = factory.deserialize(buffer);
        Assert.assertEquals("Wrong code", 2300, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong code string", "ONFERR_ET_UNKNOWN", builtByFactory.getCodeString());

        buffer = ByteBufUtils.buildBuf("ff ff 08 fd 00 00 00 01");
        builtByFactory = factory.deserialize(buffer);
        Assert.assertEquals("Wrong code", 2301, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong code string", "ONFERR_ET_EPERM", builtByFactory.getCodeString());

        buffer = ByteBufUtils.buildBuf("ff ff 08 fe 00 00 00 01");
        builtByFactory = factory.deserialize(buffer);
        Assert.assertEquals("Wrong code", 2302, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong code string", "ONFERR_ET_BAD_ID", builtByFactory.getCodeString());

        buffer = ByteBufUtils.buildBuf("ff ff 08 ff 00 00 00 01");
        builtByFactory = factory.deserialize(buffer);
        Assert.assertEquals("Wrong code", 2303, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong code string", "ONFERR_ET_BUNDLE_EXIST", builtByFactory.getCodeString());

        buffer = ByteBufUtils.buildBuf("ff ff 09 00 00 00 00 01");
        builtByFactory = factory.deserialize(buffer);
        Assert.assertEquals("Wrong code", 2304, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong code string", "ONFERR_ET_BUNDLE_CLOSED", builtByFactory.getCodeString());

        buffer = ByteBufUtils.buildBuf("ff ff 09 01 00 00 00 01");
        builtByFactory = factory.deserialize(buffer);
        Assert.assertEquals("Wrong code", 2305, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong code string", "ONFERR_ET_OUT_OF_BUNDLES", builtByFactory.getCodeString());

        buffer = ByteBufUtils.buildBuf("ff ff 09 02 00 00 00 01");
        builtByFactory = factory.deserialize(buffer);
        Assert.assertEquals("Wrong code", 2306, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong code string", "ONFERR_ET_BAD_TYPE", builtByFactory.getCodeString());

        buffer = ByteBufUtils.buildBuf("ff ff 09 03 00 00 00 01");
        builtByFactory = factory.deserialize(buffer);
        Assert.assertEquals("Wrong code", 2307, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong code string", "ONFERR_ET_BAD_FLAGS", builtByFactory.getCodeString());

        buffer = ByteBufUtils.buildBuf("ff ff 09 04 00 00 00 01");
        builtByFactory = factory.deserialize(buffer);
        Assert.assertEquals("Wrong code", 2308, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong code string", "ONFERR_ET_MSG_BAD_LEN", builtByFactory.getCodeString());

        buffer = ByteBufUtils.buildBuf("ff ff 09 05 00 00 00 01");
        builtByFactory = factory.deserialize(buffer);
        Assert.assertEquals("Wrong code", 2309, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong code string", "ONFERR_ET_MSG_BAD_XID", builtByFactory.getCodeString());

        buffer = ByteBufUtils.buildBuf("ff ff 09 06 00 00 00 01");
        builtByFactory = factory.deserialize(buffer);
        Assert.assertEquals("Wrong code", 2310, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong code string", "ONFERR_ET_MSG_UNSUP", builtByFactory.getCodeString());

        buffer = ByteBufUtils.buildBuf("ff ff 09 07 00 00 00 01");
        builtByFactory = factory.deserialize(buffer);
        Assert.assertEquals("Wrong code", 2311, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong code string", "ONFERR_ET_MSG_CONFLICT", builtByFactory.getCodeString());

        buffer = ByteBufUtils.buildBuf("ff ff 09 08 00 00 00 01");
        builtByFactory = factory.deserialize(buffer);
        Assert.assertEquals("Wrong code", 2312, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong code string", "ONFERR_ET_MSG_TOO_MANY", builtByFactory.getCodeString());

        buffer = ByteBufUtils.buildBuf("ff ff 09 09 00 00 00 01");
        builtByFactory = factory.deserialize(buffer);
        Assert.assertEquals("Wrong code", 2313, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong code string", "ONFERR_ET_MSG_FAILED", builtByFactory.getCodeString());

        buffer = ByteBufUtils.buildBuf("ff ff 09 0a 00 00 00 01");
        builtByFactory = factory.deserialize(buffer);
        Assert.assertEquals("Wrong code", 2314, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong code string", "ONFERR_ET_TIMEOUT", builtByFactory.getCodeString());

        buffer = ByteBufUtils.buildBuf("ff ff 09 0b 00 00 00 01");
        builtByFactory = factory.deserialize(buffer);
        Assert.assertEquals("Wrong code", 2315, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong code string", "ONFERR_ET_BUNDLE_IN_PROGRESS", builtByFactory.getCodeString());
    }

}