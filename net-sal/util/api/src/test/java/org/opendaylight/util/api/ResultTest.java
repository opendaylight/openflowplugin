/*
 * (C) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.api;

import junit.framework.Assert;

import org.junit.Test;

import org.opendaylight.util.api.Result.CompletionState;
import org.opendaylight.util.junit.SerializabilityTester;

/**
 * {@link Result} tests.
 * 
 * @author Fabiel Zuniga
 */
public class ResultTest {

    @Test
    public void testSuccessResult() {
        Integer resultData = Integer.valueOf(0);
        Result<Integer, String, CancellationCause> result = Result
            .createSuccessResult(resultData);

        Assert.assertEquals("Invalid completion state", CompletionState.SUCCESS, result.getCompletionState());
        Assert.assertEquals("Invalid result data", resultData, result.getResultData());
        Assert.assertNull("Invalid error descriptor", result.getError());
        Assert.assertNull("Invalid cancellation cause",
                          result.getCancellationCause());

        Result<Void, String, CancellationCause> noDataResult = Result
            .createSuccessResult();

        Assert.assertEquals("Invalid completion state", CompletionState.SUCCESS, noDataResult.getCompletionState());
        Assert.assertNull("Invalid result data", noDataResult.getResultData());
        Assert.assertNull("Invalid error descriptor", noDataResult.getError());
        Assert.assertNull("Invalid cancellation cause",
                          result.getCancellationCause());
    }

    @Test
    public void testFailureResult() {
        String errorDescriptor = "Error description";
        Result<Void, String, CancellationCause> result = Result
            .createFailureResult(errorDescriptor);

        Assert.assertEquals("Invalid completion state", CompletionState.FAILURE, result.getCompletionState());
        Assert.assertNull("Invalid result data", result.getResultData());
        Assert.assertEquals("Invalid error descriptor", errorDescriptor, result.getError());
        Assert.assertNull("Invalid cancellation cause",
                          result.getCancellationCause());
    }

    @Test
    public void testCanceledResult() {
        Result<Void, String, CancellationCause> result = Result
            .createCanceledResult(CancellationCause.CAUSE_1);

        Assert.assertEquals("Invalid completion state",
                            CompletionState.CANCELLED,
                            result.getCompletionState());
        Assert.assertNull("Invalid result data", result.getResultData());
        Assert.assertNull("Invalid error descriptor", result.getError());
        Assert.assertEquals("Invalid cancellation cause",
                            CancellationCause.CAUSE_1,
                            result.getCancellationCause());
    }

    @Test
    public void testSerialization() {
        SerializabilityTester.testSerialization(Result
            .<Void, Void, Void> createSuccessResult());
        SerializabilityTester.testSerialization(Result
            .<Void, Void, Void> createSuccessResult(null));
        SerializabilityTester.testSerialization(Result
            .<Void, Void, Void> createFailureResult(null));
        SerializabilityTester.testSerialization(Result
                .<String, String, CancellationCause> createSuccessResult("My result data"));
        SerializabilityTester.testSerialization(Result
                .<String, String, CancellationCause> createFailureResult("My error description"));
        SerializabilityTester.testSerialization(Result
                .<String, String, CancellationCause> createCanceledResult(CancellationCause.CAUSE_1));
    }

    private static enum CancellationCause {
        CAUSE_1;
    }
}
