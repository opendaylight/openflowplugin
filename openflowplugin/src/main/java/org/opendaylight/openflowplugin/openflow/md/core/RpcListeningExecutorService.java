/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.opendaylight.openflowplugin.openflow.md.core.sal.OFRpcTask;
import org.opendaylight.openflowplugin.api.openflow.statistics.MessageSpy;
import org.opendaylight.yangtools.yang.binding.DataContainer;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

/**
 * 
 */
public class RpcListeningExecutorService implements ListeningExecutorService {
    
    private MessageSpy<DataContainer> messageSpy;
    private ListeningExecutorService executorServiceDelegate;
    private DataContainer notSupportedTask = new NoDataContainerTask();
    
    /**
     * @param executorService executor service
     */
    public RpcListeningExecutorService(ListeningExecutorService executorService) {
        this.executorServiceDelegate = executorService;
    }
    
    /**
     * @param messageSpy the messageSpy to set
     */
    public void setMessageSpy(MessageSpy<DataContainer> messageSpy) {
        this.messageSpy = messageSpy;
    }
    
    @Override
    public void shutdown() {
        executorServiceDelegate.shutdown();
    }

    @Override
    public <T> ListenableFuture<T> submit(Callable<T> task) {
        ListenableFuture<T> resultFuture = executorServiceDelegate.submit(task);
        
        boolean covered = false;
        if (task instanceof OFRpcTask<?, ?>) {
            if (((OFRpcTask<?, ?>) task).getInput() instanceof DataContainer) {
                messageSpy.spyMessage((DataContainer) ((OFRpcTask<?, ?>) task).getInput(), 
                        MessageSpy.STATISTIC_GROUP.TO_SWITCH_ENQUEUED_SUCCESS);
                covered = true;
            }
        } 
        
        if (! covered) {
            messageSpy.spyMessage(notSupportedTask, MessageSpy.STATISTIC_GROUP.TO_SWITCH_ENQUEUED_FAILED);
        }
        
        return resultFuture;
    }

    @Override
    public ListenableFuture<?> submit(Runnable task) {
        throw new IllegalAccessError("not supported");
    }

    @Override
    public <T> ListenableFuture<T> submit(Runnable task, T result) {
        throw new IllegalAccessError("not supported");
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
            throws InterruptedException {
        return executorServiceDelegate.invokeAll(tasks);
    }

    @Override
    public <T> List<Future<T>> invokeAll(
            Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException {
        return executorServiceDelegate.invokeAll(tasks, timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
        throw new IllegalAccessError("not supported");
    }

    @Override
    public List<Runnable> shutdownNow() {
        return executorServiceDelegate.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return executorServiceDelegate.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return executorServiceDelegate.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit)
            throws InterruptedException {
        return executorServiceDelegate.awaitTermination(timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
            throws InterruptedException, ExecutionException {
        throw new IllegalAccessError("not supported");
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
            long timeout, TimeUnit unit) throws InterruptedException,
            ExecutionException, TimeoutException {
        throw new IllegalAccessError("not supported");
    }
    
    protected static class NoDataContainerTask implements DataContainer {
        @Override
        public Class<? extends DataContainer> getImplementedInterface() {
            return null;
        }
    }

}
