package org.opendaylight.openflowplugin.applications.frm;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutput;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.RpcResult;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface NodeConfigurator {

        void enqueueJob(String key, Callable<Future<? extends RpcResult<?>>> mainWorker, Future<? extends RpcResult<?>> resultFuture);

}
