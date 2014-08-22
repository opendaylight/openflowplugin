/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.event;

import org.opendaylight.util.TimeUtils;

/**
 * A base implementation of {@link TypedEvent}.
 * <p>
 * It is expected that concrete subclasses are designed to be immutable.
 *
 * @param <T> Event type class
 *
 * @author Simon Hunt
 */
public abstract class AbstractTypedEvent<T extends Enum<?>>
        implements TypedEvent<T> {

    private static final String E_NULL_TYPE = "Type cannot be null";

    private static final TimeUtils TIME = TimeUtils.getInstance();

    private final long ts = time().currentTimeMillis();
    private final T type;

    /**
     * Constructs a typed event.
     *
     * @param type the type of event
     */
    public AbstractTypedEvent(T type) {
        if (type == null)
            throw new NullPointerException(E_NULL_TYPE);
        this.type = type;
    }

    @Override
    public String toString() {
        return "{" + time().hhmmssnnn(ts) +
                " <" + getClass().getSimpleName() + "> " + type + "}";
    }

    @Override
    public long ts() {
        return ts;
    }

    @Override
    public T type() {
        return type;
    }

    /**
     * Returns the {@code TimeUtils} instance used to time stamp the event.
     *
     * @return the time utils instance
     *
     * @see TimeUtils
     */
    protected TimeUtils time() {
        return TIME;
    }
}
