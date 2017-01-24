/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.onf.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.message.service.rev151020.SalExperimenterMessageService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.message.service.rev151020.SendExperimenterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.ControlBundleInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.ControlBundleInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.SalBundleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.BundleControlBuilder;

/**
 * Test for {@link org.opendaylight.openflowplugin.extension.onf.service.SalBundleServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SalBundleServiceImplTest {

    private SalBundleService service;
    @Mock
    private SalExperimenterMessageService experimenterMessageService;

    @Before
    public void setUp() {
        service = new SalBundleServiceImpl(experimenterMessageService);
    }

    @Test
    public void testControlBundle() {
        final ControlBundleInput input = new ControlBundleInputBuilder().build();
        final SendExperimenterInputBuilder experimenterInput = new SendExperimenterInputBuilder();
        experimenterInput.setExperimenterMessageOfChoice(new BundleControlBuilder(input).build());
        service.controlBundle(input);
        Mockito.verify(experimenterMessageService).sendExperimenter(experimenterInput.build());
    }

    @Test
    public void testAddBundleMessages() {
        // TODO
    }

}