package org.opendaylight.openflowplugin.extension.onf.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.message.service.rev151020.SalExperimenterMessageService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.message.service.rev151020.SendExperimenterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev161201.ControlBundleInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev161201.ControlBundleInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev161201.SalBundleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev161201.send.experimenter.input.experimenter.message.of.choice.BundleControlBuilder;

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