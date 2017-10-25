/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core;

/**
 * Stores names of handlers used in pipeline.
 *
 * @author michal.polkorab
 */
public enum PipelineHandlers {

    /**
     * Detects switch idle state
     */
    IDLE_HANDLER,
    /**
     * Component for handling TLS frames
     */
    SSL_HANDLER,
    /**
     * Decodes incoming messages into message frames
     */
    OF_FRAME_DECODER,
    /**
     * Detects version of incoming OpenFlow Protocol message
     */
    OF_VERSION_DETECTOR,
    /**
     * Transforms OpenFlow Protocol byte messages into POJOs
     */
    OF_DECODER,
    /**
     * Transforms POJOs into OpenFlow Protocol byte messages
     */
    OF_ENCODER,
    /**
     * Delegates translated POJOs into MessageConsumer
     */
    DELEGATING_INBOUND_HANDLER,
    /**
     * Performs configurable efficient flushing
     */
    CHANNEL_OUTBOUND_QUEUE_MANAGER,
    /**
     * Decodes incoming messages into message frames
     * and filters them based on version supported
     */
    OF_DATAGRAMPACKET_HANDLER,
    /**
     * Transforms OpenFlow Protocol datagram messages into POJOs
     */
    OF_DATAGRAMPACKET_DECODER,
    /**
     * Transforms POJOs into OpenFlow Protocol datagrams
     */
    OF_DATAGRAMPACKET_ENCODER
}