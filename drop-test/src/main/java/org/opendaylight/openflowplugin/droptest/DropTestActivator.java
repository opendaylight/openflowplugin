/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.droptest;

import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.opendaylight.controller.sal.binding.api.AbstractBindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.data.DataProviderService;
import org.opendaylight.openflowplugin.droptest.DropTestCommandProvider;
import org.opendaylight.openflowplugin.droptest.DropTestProvider;
import org.opendaylight.openflowplugin.droptest.DropTestRpcProvider;
import org.opendaylight.openflowplugin.outputtest.OutputTestCommandProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("all")
public class DropTestActivator extends AbstractBindingAwareProvider {
  private static Logger LOG = new Function0<Logger>() {
    public Logger apply() {
      Logger _logger = LoggerFactory.getLogger(DropTestActivator.class);
      return _logger;
    }
  }.apply();
  
  private static DropTestProvider provider = new Function0<DropTestProvider>() {
    public DropTestProvider apply() {
      DropTestProvider _dropTestProvider = new DropTestProvider();
      return _dropTestProvider;
    }
  }.apply();
  
  private static DropTestRpcProvider rpcProvider = new Function0<DropTestRpcProvider>() {
    public DropTestRpcProvider apply() {
      DropTestRpcProvider _dropTestRpcProvider = new DropTestRpcProvider();
      return _dropTestRpcProvider;
    }
  }.apply();
  
  private static DropTestCommandProvider cmdProvider;
  
  private static OutputTestCommandProvider outCmdProvider;
  
  public void onSessionInitiated(final ProviderContext session) {
    DropTestActivator.LOG.debug("Activator DropAllPack INIT");
    DataProviderService _sALService = session.<DataProviderService>getSALService(DataProviderService.class);
    DropTestActivator.provider.setDataService(_sALService);

    NotificationProviderService _sALService_1 = session.<NotificationProviderService>getSALService(NotificationProviderService.class);
    DropTestActivator.provider.setNotificationService(_sALService_1);

    DropTestActivator.cmdProvider.onSessionInitiated(session);

    NotificationProviderService _sALService_2 = session.<NotificationProviderService>getSALService(NotificationProviderService.class);
    DropTestActivator.rpcProvider.setNotificationService(_sALService_2);

    SalFlowService _rpcService = session.<SalFlowService>getRpcService(SalFlowService.class);
    DropTestActivator.rpcProvider.setFlowService(_rpcService);

    DropTestActivator.outCmdProvider.onSessionInitiated(session);

    DropTestActivator.LOG.debug("Activator DropAllPack END");
  }
  
  public void startImpl(final BundleContext ctx) {
    super.startImpl(ctx);
    DropTestCommandProvider _dropTestCommandProvider = new DropTestCommandProvider(ctx, DropTestActivator.provider, DropTestActivator.rpcProvider);
    DropTestActivator.cmdProvider = _dropTestCommandProvider;
    OutputTestCommandProvider _outputTestCommandProvider = new OutputTestCommandProvider(ctx);
    DropTestActivator.outCmdProvider = _outputTestCommandProvider;
  }
  
  protected void stopImpl(final BundleContext context) {
    DropTestActivator.provider.close();
  }
}
