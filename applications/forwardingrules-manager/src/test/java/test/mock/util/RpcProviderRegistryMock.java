package test.mock.util;

import java.util.HashMap;
import java.util.Map;
import org.mockito.Mockito;
import org.opendaylight.controller.md.sal.common.api.routing.RouteChangeListener;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.controller.sal.binding.api.rpc.RpcContextIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.SalGroupService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.SalTableService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.RpcService;

public class RpcProviderRegistryMock implements RpcProviderRegistry {

    private Map<Class<? extends RpcService>, RpcService> serviceMockCache;

    public RpcProviderRegistryMock() {
        serviceMockCache = new HashMap<>();
    }

    @Override
    public <T extends RpcService> BindingAwareBroker.RpcRegistration<T> addRpcImplementation(Class<T> serviceInterface, T implementation) throws IllegalStateException {
        return null;
    }

    @Override
    public <T extends RpcService> BindingAwareBroker.RoutedRpcRegistration<T> addRoutedRpcImplementation(Class<T> serviceInterface, T implementation) throws IllegalStateException {
        return null;
    }

    @Override
    public <L extends RouteChangeListener<RpcContextIdentifier, InstanceIdentifier<?>>> ListenerRegistration<L> registerRouteChangeListener(L listener) {
        return null;
    }

    @Override
    public <T extends RpcService> T getRpcService(Class<T> serviceInterface) {
        if (serviceInterface.equals(SalFlowService.class)) {
            return (T) new SalFlowServiceMock();
        } else if (serviceInterface.equals(SalGroupService.class)) {
            return (T) new SalGroupServiceMock();
        } else if (serviceInterface.equals(SalMeterService.class)) {
            return (T) new SalMeterServiceMock();
        } else if (serviceInterface.equals(SalTableService.class)) {
            return (T) new SalTableServiceMock();
        } else {
            return findOrCreateMockedService(serviceInterface);
        }
    }

    private <T extends RpcService> T findOrCreateMockedService(final Class<T> serviceInterface) {
        RpcService cachedService = serviceMockCache.get(serviceInterface);
        if (cachedService == null) {
            cachedService = Mockito.mock(serviceInterface);
            serviceMockCache.put(serviceInterface, cachedService);
        }
        return (T) cachedService;
    }


}
