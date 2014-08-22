/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

import org.opendaylight.util.event.AbstractTypedEvent;

/**
 * Base implementation of a typed model event.
 *
 * @author Thomas Vachuska
 */
public abstract class AbstractModelEvent<M extends Model, T extends Enum<?>>
        extends AbstractTypedEvent<T> implements ModelEvent<M, T> {

    protected static final String E_NULL_SUBJECT = "Subject cannot be null";

    private final M subject;

    /**
     * Constructs a typed model event.
     *
     * @param subject the subject of the event
     * @param type    the type of the event
     */
    protected AbstractModelEvent(T type, M subject) {
        super(type);
        if (subject == null)
            throw new NullPointerException(E_NULL_SUBJECT);
        this.subject = subject;
    }


    @Override
    public M subject() {
        return subject;
    }

}
