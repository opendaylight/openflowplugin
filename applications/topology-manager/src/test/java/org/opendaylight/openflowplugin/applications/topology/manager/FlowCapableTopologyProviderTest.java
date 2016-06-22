/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.topology.manager;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.util.concurrent.Futures;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.BindingAwareService;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService.NotificationInterestListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.NotificationListener;

/**
 * Test for {@link FlowCapableTopologyProvider}.
 */
@RunWith(MockitoJUnitRunner.class)
public class FlowCapableTopologyProviderTest {

    private FlowCapableTopologyProvider provider;

    @Mock
    private BindingAwareBroker.ProviderContext providerContext;
    @Mock
    private DataBroker dataBroker;
    @Mock
    private NotificationProviderService notificationProviderService;
    @Mock
    private BindingAwareProvider bindingAwareProvider;
    @Mock
    private ClassToInstanceMap<BindingAwareService> serviceProvider;
    @Mock
    private BindingAwareService bindingAwareService;
    @Mock
    private Node mockNode;
    @Mock
    private ReadOnlyTransaction rTx;
    @Mock
    private ReadWriteTransaction wTx;

    @Before
    public void setUp() throws Exception {
        when(providerContext.getSALService(Matchers.<Class<? extends BindingAwareService>>any()))
        .thenAnswer(new Answer<BindingAwareService>() {
            @Override
            public BindingAwareService answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                if (arguments != null && arguments.length > 0 && arguments[0] != null) {
                    if(arguments[0].equals(DataBroker.class)) {
                        return dataBroker;
                    } else if(arguments[0].equals(NotificationProviderService.class)){
                        return notificationProviderService;
                    }
                }
                return null;
            }
        });

        doReturn(rTx).when(dataBroker).newReadOnlyTransaction();
        doReturn(wTx).when(dataBroker).newReadWriteTransaction();

        when(wTx.submit()).thenReturn(Futures.immediateCheckedFuture(null));

        OperationProcessor operationProcessor = new OperationProcessor(dataBroker);
        provider = new FlowCapableTopologyProvider(dataBroker, notificationProviderService, operationProcessor);
    }

    @Test
    public void testRun() throws Exception {
        when(rTx.read(Matchers.<LogicalDatastoreType>any(), Matchers.<InstanceIdentifier<DataObject>>any()))
                .thenReturn(Futures.immediateCheckedFuture(Optional.of(mockNode)));
        provider.start();
        verify(rTx).read(Matchers.<LogicalDatastoreType>any(), Matchers.<InstanceIdentifier<DataObject>>any());
    }

    @Test
    public void testRunWithoutTopology() throws Exception {
        when(rTx.read(Matchers.<LogicalDatastoreType>any(), Matchers.<InstanceIdentifier<DataObject>>any()))
                .thenReturn(Futures.immediateCheckedFuture(Optional.absent()));
        provider.start();
        verify(wTx).submit();
    }

    @Test
    public void testClose() throws Exception {
        when(rTx.read(Matchers.<LogicalDatastoreType>any(), Matchers.<InstanceIdentifier<DataObject>>any()))
                .thenReturn(Futures.immediateCheckedFuture(Optional.of(mockNode)));

        final ListenerRegistration<NotificationInterestListener> listenerRegistration = mock(ListenerRegistration.class);
        doReturn(listenerRegistration).when(notificationProviderService).registerNotificationListener(Matchers.<NotificationListener>any());

        provider.start();
        provider.close();

        verify(listenerRegistration).close();
    }

}