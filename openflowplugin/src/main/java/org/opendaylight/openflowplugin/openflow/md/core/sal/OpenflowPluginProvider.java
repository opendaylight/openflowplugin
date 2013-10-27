package org.opendaylight.openflowplugin.openflow.md.core.sal;

import java.util.Collection;
import java.util.Collections;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.osgi.framework.BundleContext;

public class OpenflowPluginProvider implements BindingAwareProvider {

    private BindingAwareBroker broker;

    private BundleContext context;

    public BundleContext getContext() {
        return context;
    }

    public void setContext(BundleContext context) {
        this.context = context;
    }

    SalRegistrationManager registrationManager = new SalRegistrationManager();

    @Override
    public void onSessionInitiated(ProviderContext session) {
        registrationManager.onSessionInitiated(session);
    }

    @Override
    public void onSessionInitialized(ConsumerContext session) {
        // NOOP
    }

    @Override
    public Collection<? extends ProviderFunctionality> getFunctionality() {
        return Collections.emptySet();
    }

    public java.util.Collection<? extends RpcService> getImplementations() {
        return Collections.emptySet();
    }

    public BindingAwareBroker getBroker() {
        return broker;
    }

    public void setBroker(BindingAwareBroker broker) {
        this.broker = broker;
        broker.registerProvider(this, context);
    };

    public void unsetBroker(BindingAwareBroker broker) {
        this.broker = null;
    };
}
