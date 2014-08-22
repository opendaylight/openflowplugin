/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.api;

import java.io.Serializable;

/**
 * Result.
 * 
 * @param <D> type of the result data.
 * @param <E> type of the error.
 * @param <C> type of the cancellation cause.
 * @author Fabiel Zuniga
 */
public class Result<D, E, C> implements Serializable {
    private static final long serialVersionUID = -9087549841686227569L;

    private final CompletionState completionState;
    private final D resultData;
    private final E error;
    private final C cancellationCause;

    private Result(CompletionState completionState, D resultData, E error,
                   C cancellationCause) {
        this.completionState = completionState;
        this.error = error;
        this.resultData = resultData;
        this.cancellationCause = cancellationCause;
    }

    /**
     * Creates a success result with no result data.
     * 
     * @return a success result.
     */
    public static <D, E, C> Result<D, E, C> createSuccessResult() {
        return new Result<D, E, C>(CompletionState.SUCCESS, null, null, null);
    }

    /**
     * Creates a success result.
     * 
     * @param resultData result data.
     * @return a success result.
     */
    public static <D, E, C> Result<D, E, C> createSuccessResult(D resultData) {
        return new Result<D, E, C>(CompletionState.SUCCESS, resultData, null,
                                   null);
    }

    /**
     * Creates a failure result.
     * 
     * @param error error.
     * @return a failure result.
     */
    public static <D, E, C> Result<D, E, C> createFailureResult(E error) {
        return new Result<D, E, C>(CompletionState.FAILURE, null, error, null);
    }

    /**
     * Creates a canceled result.
     * 
     * @param cause cancellation cause.
     * @return a result for an operation that was canceled.
     */
    public static <D, E, C> Result<D, E, C> createCanceledResult(C cause) {
        return new Result<D, E, C>(CompletionState.CANCELLED, null, null, cause);
    }

    /**
     * Gets the completion state.
     * 
     * @return the completion state.
     */
    public CompletionState getCompletionState() {
        return this.completionState;
    }

    /**
     * Gets the result data.
     * 
     * @return the result data.
     */
    public D getResultData() {
        return this.resultData;
    }

    /**
     * Gets the error.
     * 
     * @return the error.
     */
    public E getError() {
        return this.error;
    }

    /**
     * Gets the cancellation cause.
     * 
     * @return the cancellation cause.
     */
    public C getCancellationCause() {
        return this.cancellationCause;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(this.getClass().getSimpleName());
        str.append("[completionState=");
        str.append(this.completionState);
        str.append(", resultData=");
        str.append(this.resultData);
        str.append(", error=");
        str.append(this.error);
        str.append(", cancellationCause=");
        str.append(this.cancellationCause);
        str.append(']');
        return str.toString();
    }

    /**
     * Completion State.
     */
    public enum CompletionState {
        /** Success */
        SUCCESS,
        /** Failure */
        FAILURE,
        /** Non executed: Interrupted, cancelled, aborted, etc. */
        CANCELLED
    }
}
