/**
* Generated file

* Generated from: yang module name: openflow-switch-connection-provider-impl  yang module local name: openflow-switch-connection-provider-impl
* Generated by: org.opendaylight.controller.config.yangjmxgenerator.plugin.JMXGenerator
* Generated at: Fri Mar 28 17:50:58 PDT 2014
*
* Do not modify this file unless it is present under src/main directory
*/
package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow._switch.connection.provider.impl.rev140328;

import org.opendaylight.controller.config.api.DependencyResolver;
import org.osgi.framework.BundleContext;

/**
 * @deprecated Replaced by blueprint wiring
 */
@Deprecated
public class SwitchConnectionProviderModuleFactory extends AbstractSwitchConnectionProviderModuleFactory {
    @Override
    public SwitchConnectionProviderModule instantiateModule(String instanceName, DependencyResolver dependencyResolver,
            SwitchConnectionProviderModule oldModule, AutoCloseable oldInstance, BundleContext bundleContext) {
        SwitchConnectionProviderModule module = super.instantiateModule(instanceName, dependencyResolver, oldModule,
                oldInstance, bundleContext);
        module.setBundleContext(bundleContext);
        return module;
    }

    @Override
    public SwitchConnectionProviderModule instantiateModule(String instanceName, DependencyResolver dependencyResolver,
            BundleContext bundleContext) {
        SwitchConnectionProviderModule module = super.instantiateModule(instanceName, dependencyResolver, bundleContext);
        module.setBundleContext(bundleContext);
        return module;
    }
}
