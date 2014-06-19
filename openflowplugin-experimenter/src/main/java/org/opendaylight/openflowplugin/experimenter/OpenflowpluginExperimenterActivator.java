package org.opendaylight.openflowplugin.experimenter;

import org.apache.felix.dm.Component;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.core.ComponentActivatorAbstractBase;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.openflowplugin.openflow.md.core.MDController;
import org.opendaylight.openflowplugin.openflow.md.core.sal.OpenflowPluginProvider;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* Openflow protocol plugin OpenflowpluginExperimenterActivator
*/
public class OpenflowpluginExperimenterActivator extends ComponentActivatorAbstractBase {
    protected static final Logger logger = LoggerFactory.getLogger(OpenflowpluginExperimenterActivator.class);

    private ExampleActionConverter exampleActionConverter;

    /**
     * Function called when the activator starts just after some initializations
     * are done by the ComponentActivatorAbstractBase.
     *
     */
    public void init() {
    }

    /**
     * Function called when the activator stops just before the cleanup done by
     * ComponentActivatorAbstractBase
     *
     */
    public void destroy() {
        super.destroy();
    }

    @Override
    public void start(BundleContext arg0) {
        super.start(arg0);

        exampleActionConverter = new ExampleActionConverter();
        exampleActionConverter.init();
    }

    /**
     * Function that is used to communicate to dependency manager the list of
     * known implementations for services inside a container
     *
     *
     * @return An array containing all the CLASS objects that will be
     *         instantiated in order to get an fully working implementation
     *         Object
     */
    public Object[] getImplementations() {
        Object[] res = {};
        return res;
    }

    /**
     * Function that is called when configuration of the dependencies is
     * required.
     *
     * @param c
     *            dependency manager Component object, used for configuring the
     *            dependencies exported and imported
     * @param imp
     *            Implementation class that is being configured, needed as long
     *            as the same routine can configure multiple implementations
     * @param containerName
     *            The containerName being configured, this allow also optional
     *            per-container different behavior if needed, usually should not
     *            be the case though.
     */
    public void configureInstance(Component c, Object imp, String containerName) {

    }

    /**
     * Function that is used to communicate to dependency manager the list of
     * known implementations for services that are container independent.
     *
     *
     * @return An array containing all the CLASS objects that will be
     *         instantiated in order to get an fully working implementation
     *         Object
     */
    public Object[] getGlobalImplementations() {
    	return null;
    }

    /**
     * Function that is called when configuration of the dependencies is
     * required.
     *
     * @param c
     *            dependency manager Component object, used for configuring the
     *            dependencies exported and imported
     * @param imp
     *            Implementation class that is being configured, needed as long
     *            as the same routine can configure multiple implementations
     */
    public void configureGlobalInstance(Component c, Object imp) {
    }

}

