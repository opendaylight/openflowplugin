package org.opendaylight.openflowplugin.openflow.md.core.sal;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.osgi.framework.console.CommandProvider;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.openflowplugin.openflow.md.core.IMDController;
import org.opendaylight.openflowplugin.openflow.md.core.IMDMessageListener;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowRemoved;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* Represents the openflow plugin component that maps the incoming OF Messages to the corresponding MD-SAL
* DTO object and publishes the notification to functional modules  above MDSAL.
*/

public class MessageHandlerImpl  implements BindingAwareProvider, IMDMessageListener{
	
	private static final Logger log = LoggerFactory.getLogger(MessageHandlerImpl.class);
	
    private ProviderContext providerContext;
    private NotificationProviderService publishService;
    private IMDController mdController ;
    
    public void setMDController(IMDController core) {
        this.mdController = core;
    }

    public void unsetMDController(IMDController core) {
        if (this.mdController == core) {
            this.mdController = null;
        }
    }
    
    /**
     * Function called by the dependency manager when all the required
     * dependencies are satisfied
     *
     */
    void init() {
        this.mdController.addMessageListener(FlowRemoved.class, this);

        // TO DO
        //this.mdController.addMessageListener(ErrorMessage.class, this);
        
        registerWithOSGIConsole();
    }
    
    private void registerWithOSGIConsole() {
        BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass())
                .getBundleContext();
        bundleContext.registerService(CommandProvider.class.getName(), this,
                null);
    }
    
    
	@Override
	public void onSessionInitiated(ProviderContext session) {
		// TODO Auto-generated method stub
		this.providerContext = session;
        this.publishService = session.getSALService(NotificationProviderService.class);
	}

	@Override
	public Collection<? extends ProviderFunctionality> getFunctionality() {
		return Collections.emptySet();
	}

	@Override
	public Collection<? extends RpcService> getImplementations() {
		return Collections.emptySet();
	}

	@Override
	public void onSessionInitialized(ConsumerContext arg0) {
		//NOTHING
	}
	
    public NotificationProviderService getPublishService() {
        return publishService;
    }

    public void setPublishService(NotificationProviderService publishService) {
        this.publishService = publishService;
    }

    public ProviderContext getProviderContext() {
        return providerContext;
    }

	@Override
	public void receive(SwitchConnectionDistinguisher cookie,
			SessionContext sw, DataObject msg) {
		// TODO Convert the incoming FlowRemovedMessage to FlowRemoved 
		// and publish it to MD-SAL
		
		
	}
}
