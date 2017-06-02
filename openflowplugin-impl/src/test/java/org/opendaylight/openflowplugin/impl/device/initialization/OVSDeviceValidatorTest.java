/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device.initialization;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.multipart.reply.multipart.reply.body.MultipartReplyDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.multipart.reply.multipart.reply.body.MultipartReplyDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfig;

@RunWith(MockitoJUnitRunner.class)
public class OVSDeviceValidatorTest {

    private OVSDeviceValidator validator;

    @Mock
    private OpenflowProviderConfig config;

    @Before
    public void setUp() {
        validator = new OVSDeviceValidator();
        Mockito.when(config.isSwitchFeaturesMandatory()).thenReturn(true);
    }

    @Test
    public void validOVS203() throws Exception {
        MultipartReplyDesc description = new MultipartReplyDescBuilder()
                .setHardware("OPEN VSWITCH")
                .setSoftware("2.0.3")
                .build();
        Assert.assertTrue(validator.valid(description, config).getLeft());
    }

    @Test
    public void validFeaturesFalse() throws Exception {
        Mockito.when(config.isSwitchFeaturesMandatory()).thenReturn(false);
        MultipartReplyDesc description = new MultipartReplyDescBuilder()
                .setHardware("OPEN VSWITCH")
                .setSoftware("2.0.1")
                .build();
        Assert.assertTrue(validator.valid(description, config).getLeft());
    }

    @Test
    public void validNonOVS() throws Exception {
        MultipartReplyDesc description = new MultipartReplyDescBuilder()
                .setHardware("CLOSED VSWITCH")
                .setSoftware("2.0.1")
                .build();
        Assert.assertTrue(validator.valid(description, config).getLeft());
    }

    @Test
    public void nonValidOVS202() throws Exception {
        MultipartReplyDesc description = new MultipartReplyDescBuilder()
                .setHardware("OPEN VSWITCH")
                .setSoftware("2.0.2")
                .build();
        Assert.assertFalse(validator.valid(description, config).getLeft());
    }

    @Test
    public void nonValidOVS201() throws Exception {
        MultipartReplyDesc description = new MultipartReplyDescBuilder()
                .setHardware("OPEN VSWITCH")
                .setSoftware("2.0.1")
                .build();
        Assert.assertFalse(validator.valid(description, config).getLeft());
    }

    @Test
    public void nonValidOVS200() throws Exception {
        MultipartReplyDesc description = new MultipartReplyDescBuilder()
                .setHardware("OPEN VSWITCH")
                .setSoftware("2.0.0")
                .build();
        Assert.assertFalse(validator.valid(description, config).getLeft());
    }

    @Test
    public void nonValidOVS199() throws Exception {
        MultipartReplyDesc description = new MultipartReplyDescBuilder()
                .setHardware("OPEN VSWITCH")
                .setSoftware("1.9.9")
                .build();
        Assert.assertFalse(validator.valid(description, config).getLeft());
    }

    @Test
    public void validOVSWrongSoftwareNr() throws Exception {
        MultipartReplyDesc description = new MultipartReplyDescBuilder()
                .setHardware("OPEN VSWITCH")
                .setSoftware("2.0.b")
                .build();
        Assert.assertTrue(validator.valid(description, config).getLeft());
    }

}