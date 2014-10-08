/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.cof;

import org.opendaylight.openflowjava.cof.api.CiscoExtensionCodecRegistrator;
import org.opendaylight.openflowjava.cof.codec.action.NextHopCodec;
import org.opendaylight.openflowjava.cof.codec.action.VrfCodec;

import com.google.common.base.Preconditions;

/**
 * registration and unregistration provider
 */
public class CiscoExtensionsRegistrator implements AutoCloseable {

    private final CiscoExtensionCodecRegistrator registrator;

    /**
     * @param registrator 
     * @param providers cannot be null
     */
    public CiscoExtensionsRegistrator(CiscoExtensionCodecRegistrator registrator) {
        Preconditions.checkNotNull(registrator);
        this.registrator = registrator;
    }

    /**
     * register actions and matches
     */
    public void registerCiscoExtensions() {
        registrator.registerActionDeserializer(VrfCodec.DESERIALIZER_KEY, VrfCodec.getInstance());
        registrator.registerActionSerializer(VrfCodec.SERIALIZER_KEY, VrfCodec.getInstance());
        registrator.registerActionDeserializer(VrfCodec.DESERIALIZER_KEY_10, VrfCodec.getInstance());
        registrator.registerActionSerializer(VrfCodec.SERIALIZER_KEY_10, VrfCodec.getInstance());
        
        registrator.registerActionDeserializer(NextHopCodec.DESERIALIZER_KEY, NextHopCodec.getInstance());
        registrator.registerActionSerializer(NextHopCodec.SERIALIZER_KEY, NextHopCodec.getInstance());
        registrator.registerActionDeserializer(NextHopCodec.DESERIALIZER_KEY_10, NextHopCodec.getInstance());
        registrator.registerActionSerializer(NextHopCodec.SERIALIZER_KEY_10, NextHopCodec.getInstance());
    }

    /**
     * unregister actions and matches
     */
    private void unregisterCiscoExtensions() {
        registrator.unregisterActionDeserializer(VrfCodec.DESERIALIZER_KEY);
        registrator.unregisterActionSerializer(VrfCodec.SERIALIZER_KEY);
        registrator.unregisterActionDeserializer(VrfCodec.DESERIALIZER_KEY_10);
        registrator.unregisterActionSerializer(VrfCodec.SERIALIZER_KEY_10);
        
        registrator.unregisterActionDeserializer(NextHopCodec.DESERIALIZER_KEY);
        registrator.unregisterActionSerializer(NextHopCodec.SERIALIZER_KEY);
        registrator.unregisterActionDeserializer(NextHopCodec.DESERIALIZER_KEY_10);
        registrator.unregisterActionSerializer(NextHopCodec.SERIALIZER_KEY_10);
    }

    @Override
    public void close() throws Exception {
        unregisterCiscoExtensions();
    }

}
