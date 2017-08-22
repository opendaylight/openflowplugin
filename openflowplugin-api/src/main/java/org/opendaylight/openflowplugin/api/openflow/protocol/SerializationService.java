package org.opendaylight.openflowplugin.api.openflow.protocol;

import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;

public interface SerializationService {
    /**
     * Injects new serializers and deserializers into provided switch connection provider.
     * @param provider OpenFlowJava switch connection provider
     */
    void injectSerializers(SwitchConnectionProvider provider);

    /**
     * Reverts injected serializers and deserializers into their original state.
     * @param provider OpenFlowJava switch connection provider
     */
    void revertSerializers(SwitchConnectionProvider provider);
}