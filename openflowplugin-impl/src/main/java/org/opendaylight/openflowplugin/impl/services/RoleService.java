package org.opendaylight.openflowplugin.impl.services;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.math.BigInteger;
import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.common.RoleChangeException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ControllerRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleOutputBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by kramesha on 8/24/15.
 */
public class RoleService extends AbstractSimpleService<RoleRequestInputBuilder, RoleRequestOutput> {
    private static final Logger LOG = LoggerFactory.getLogger(RoleService.class);

    private final DeviceContext deviceContext;

    protected RoleService(final RequestContextStack requestContextStack, final DeviceContext deviceContext, final Class<RoleRequestOutput> clazz) {
        super(requestContextStack, deviceContext, clazz);
        this.deviceContext = deviceContext;
    }

    @Override
    protected OfHeader buildRequest(Xid xid, RoleRequestInputBuilder input) {
        input.setXid(xid.getValue());
        return input.build();
    }

    public Future<BigInteger> getGenerationIdFromDevice(Short version) throws RoleChangeException {
        // send a dummy no-change role request to get the generation-id of the switch
        final RoleRequestInputBuilder roleRequestInputBuilder = new RoleRequestInputBuilder();
        roleRequestInputBuilder.setRole(toOFJavaRole(OfpRole.NOCHANGE));
        roleRequestInputBuilder.setVersion(version);
        roleRequestInputBuilder.setGenerationId(BigInteger.ZERO);

        final SettableFuture<BigInteger> finalFuture = SettableFuture.create();
        ListenableFuture<RpcResult<RoleRequestOutput>> genIdListenableFuture = handleServiceCall(roleRequestInputBuilder);
        Futures.addCallback(genIdListenableFuture, new FutureCallback<RpcResult<RoleRequestOutput>>() {
            @Override
            public void onSuccess(RpcResult<RoleRequestOutput> roleRequestOutputRpcResult) {
                if (roleRequestOutputRpcResult.isSuccessful()) {
                    RoleRequestOutput roleRequestOutput = roleRequestOutputRpcResult.getResult();
                    finalFuture.set(roleRequestOutput.getGenerationId());
                } else {
                    LOG.error("getGenerationIdFromDevice RPC error " +
                            roleRequestOutputRpcResult.getErrors().iterator().next().getInfo());
                    finalFuture.set(BigInteger.valueOf(-1));
                }

            }

            @Override
            public void onFailure(Throwable throwable) {
                finalFuture.set(BigInteger.valueOf(-1));
            }
        });
        return finalFuture;
    }


    public Future<SetRoleOutput> submitRoleChange(OfpRole ofpRole, Short version, BigInteger generationId) {
        RoleRequestInputBuilder roleRequestInputBuilder = new RoleRequestInputBuilder();
        roleRequestInputBuilder.setRole(toOFJavaRole(ofpRole));
        roleRequestInputBuilder.setVersion(version);
        roleRequestInputBuilder.setGenerationId(generationId);

        ListenableFuture<RpcResult<RoleRequestOutput>> roleListenableFuture = handleServiceCall(roleRequestInputBuilder);

        final SettableFuture<SetRoleOutput> finalFuture = SettableFuture.create();
        Futures.addCallback(roleListenableFuture, new FutureCallback<RpcResult<RoleRequestOutput>>() {
            @Override
            public void onSuccess(RpcResult<RoleRequestOutput> roleRequestOutputRpcResult) {
                RoleRequestOutput roleRequestOutput = roleRequestOutputRpcResult.getResult();
                SetRoleOutputBuilder setRoleOutputBuilder = new SetRoleOutputBuilder();
                setRoleOutputBuilder.setTransactionId(new TransactionId(BigInteger.valueOf(roleRequestOutput.getXid())));
               finalFuture.set(setRoleOutputBuilder.build());
            }

            @Override
            public void onFailure(Throwable throwable) {
                finalFuture.set(null);
            }
        });
        return finalFuture;
    }

    private static ControllerRole toOFJavaRole(OfpRole role) {
        ControllerRole ofJavaRole = null;
        switch (role) {
            case BECOMEMASTER:
                ofJavaRole = ControllerRole.OFPCRROLEMASTER;
                break;
            case BECOMESLAVE:
                ofJavaRole = ControllerRole.OFPCRROLESLAVE;
                break;
            case NOCHANGE:
                ofJavaRole = ControllerRole.OFPCRROLENOCHANGE;
                break;
            default:
                // no intention
                LOG.warn("given role is not supported by protocol roles: {}", role);
                break;
        }
        return ofJavaRole;
    }


}
