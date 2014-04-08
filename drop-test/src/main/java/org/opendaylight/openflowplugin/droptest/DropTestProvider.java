/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.droptest;

import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.data.DataProviderService;
import org.opendaylight.openflowplugin.droptest.DropTestCommiter;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("all")
public class DropTestProvider implements AutoCloseable {
  private final static Logger LOG = new Function0<Logger>() {
    public Logger apply() {
      Logger _logger = LoggerFactory.getLogger(DropTestProvider.class);
      return _logger;
    }
  }.apply();
  
  private DataProviderService _dataService;
  
  public DataProviderService getDataService() {
    return this._dataService;
  }
  
  public void setDataService(final DataProviderService dataService) {
    this._dataService = dataService;
  }
  
  private NotificationProviderService _notificationService;
  
  public NotificationProviderService getNotificationService() {
    return this._notificationService;
  }
  
  public void setNotificationService(final NotificationProviderService notificationService) {
    this._notificationService = notificationService;
  }
  
  private final DropTestCommiter commiter = new Function0<DropTestCommiter>() {
    public DropTestCommiter apply() {
      DropTestCommiter _dropTestCommiter = new DropTestCommiter(DropTestProvider.this);
      return _dropTestCommiter;
    }
  }.apply();
  
  private Registration<NotificationListener> listenerRegistration;
  
  public void start() {
    NotificationProviderService _notificationService = this.getNotificationService();
    Registration<NotificationListener> _registerNotificationListener = _notificationService.registerNotificationListener(this.commiter);
    this.listenerRegistration = _registerNotificationListener;
    DropTestProvider.LOG.debug("DropTestProvider Started.");
  }
  
  public void close() {
    try {
      DropTestProvider.LOG.debug("DropTestProvider stopped.");
      if (this.listenerRegistration!=null) {
        this.listenerRegistration.close();
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
