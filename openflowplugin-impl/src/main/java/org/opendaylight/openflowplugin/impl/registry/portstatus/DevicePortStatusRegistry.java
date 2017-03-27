package org.opendaylight.openflowplugin.impl.registry.portstatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import org.opendaylight.openflowplugin.api.openflow.registry.CommonDeviceRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatus;

public class DevicePortStatusRegistry implements CommonDeviceRegistry<PortStatus> {
    private final List<PortStatus> registry = Collections.synchronizedList(new ArrayList<>());
    private final List<PortStatus> marks = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void store(final PortStatus portStatus) {
        registry.add(portStatus);
    }

    @Override
    public void addMark(final PortStatus portStatus) {
        marks.add(portStatus);
    }

    @Override
    public boolean hasMark(final PortStatus portStatus) {
        return marks.contains(portStatus);
    }

    @Override
    public void processMarks() {
        registry.removeAll(marks);
        marks.clear();
    }

    @Override
    public void forEach(final Consumer<PortStatus> consumer) {
        registry.forEach(consumer);
    }

    @Override
    public int size() {
        return registry.size();
    }

    @Override
    public void close() {
        marks.clear();
        registry.clear();
    }
}
