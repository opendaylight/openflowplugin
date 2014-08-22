/*
 * (c) Copyright 2011 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

/**
 * Interface to delegate error handling.
 * <p>
 * Note: You generally don't catch Throwable. Throwable is the superclass to
 * Exception and Error. Errors are generally things which a normal application
 * wouldn't and shouldn't catch, so just use Exception unless you have a
 * specific reason to use Throwable.
 * 
 * @param <S> type of the source where the error was generated.
 * @param <E> type of the error descriptor.
 * @author Fabiel Zuniga
 */
public interface ErrorHandler<S, E> {

    /**
     * Method called to delegate error handling.
     * 
     * @param source source where the error was generated.
     * @param error error.
     */
    public void errorOccurred(S source, E error);
}
