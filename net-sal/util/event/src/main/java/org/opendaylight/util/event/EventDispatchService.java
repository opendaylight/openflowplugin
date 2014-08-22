/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.event;

/**
 * Abstraction of a service that combines event dispatch and event sink.
 *
 * @author Thomas Vachuska
 */
public interface EventDispatchService extends EventSinkBroker, EventDispatcher {

}
