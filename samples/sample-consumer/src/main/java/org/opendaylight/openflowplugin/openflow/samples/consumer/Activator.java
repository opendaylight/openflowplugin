package org.opendaylight.openflowplugin.openflow.samples.consumer;

import org.opendaylight.controller.sal.binding.api.AbstractBindingAwareConsumer;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractBindingAwareConsumer {

    SimpleDropFirewall service;
    private SalFlowService flowService;
    private SimpleDropFirewallCli cliAdapter;
    
    
    @Override
    protected void startImpl(BundleContext context) {
        service = new SimpleDropFirewall();
        
        cliAdapter.setService(service);
    }
    
    @Override
    public void onSessionInitialized(ConsumerContext session) {
        service.setContext(session);
        flowService = session.getRpcService(SalFlowService.class);

        service.setFlowService(flowService);
        service.start();
    }

}
