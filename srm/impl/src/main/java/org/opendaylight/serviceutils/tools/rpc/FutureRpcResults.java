/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.serviceutils.tools.rpc;

import static org.opendaylight.yangtools.yang.common.ErrorType.APPLICATION;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import edu.umd.cs.findbugs.annotations.CheckReturnValue;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.OperationFailedException;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;

/**
 * Utility to simplify correctly handling transformation of Future of RpcResult to return.
 *
 * @author Michael Vorburger.ch
 */
@Beta
public final class FutureRpcResults {

    // NB: The FutureRpcResultsTest unit test for this util is in mdsalutil-testutils's src/test, not this project's

    // TODO Once matured in genius, this class could be proposed to org.opendaylight.yangtools.yang.common
    // (This was proposed in Oct on yangtools-dev list, but there little interest due to plans to change RpcResult.)

    private FutureRpcResults() {

    }

    /**
     * Create a Builder for a ListenableFuture to Future&lt;RpcResult&lt;O&gt;&gt; transformer. By default, the future
     * will log success or failure, with configurable log levels; the caller can also add handlers for success and/or
     * failure.
     *
     * @param logger the slf4j Logger of the caller
     * @param rpcMethodName Java method name (without "()") of the RPC operation, used for logging
     * @param input the RPC input DataObject of the caller (may be null)
     * @param callable the Callable (typically lambda) creating a ListenableFuture.  Note that the
     *        functional interface Callable's call() method declares throws Exception, so your lambda
     *        does not have to do any exception handling (specifically it does NOT have to catch and
     *        wrap any exception into a failed Future); this utility does that for you.
     *
     * @return a new FutureRpcResultBuilder
     */
    @CheckReturnValue
    @SuppressWarnings("InconsistentOverloads") // Error Prone is too strict here; we do want the Callable last
    public static <I, O> FutureRpcResultBuilder<I, O> fromListenableFuture(Logger logger, String rpcMethodName,
            @Nullable I input, Callable<ListenableFuture<O>> callable) {
        return new FutureRpcResultBuilder<>(logger, rpcMethodName, input, callable);
    }

    public enum LogLevel {
        ERROR, WARN, INFO, DEBUG, TRACE,
        /**
         * Note that when using LogLevel NONE for failures, then you should set a
         * {@link FutureRpcResultBuilder#onFailure(Consumer)} which does better logging,
         * or be 100% sure that all callers of the RPC check the returned Future RpcResult appropriately;
         * otherwise you will lose error messages.
         */
        NONE;
        @SuppressFBWarnings({"SLF4J_UNKNOWN_ARRAY","SLF4J_FORMAT_SHOULD_BE_CONST"})
        public void log(Logger logger, String format, Object... arguments) {
            switch (this) {
                case NONE:
                    break;
                case TRACE:
                    logger.trace(format, arguments);
                    break;
                case DEBUG:
                    logger.debug(format, arguments);
                    break;
                case INFO:
                    logger.info(format, arguments);
                    break;
                case WARN:
                    logger.warn(format, arguments);
                    break;
                default: // including ERROR
                    logger.error(format, arguments);
                    break;
            }
        }
    }

    @CheckReturnValue
    @SuppressWarnings("InconsistentOverloads") // Error Prone is too strict here; we do want the Callable last
    public static <I, O> FutureRpcResultBuilder<I, O> fromBuilder(Logger logger, String rpcMethodName,
            @Nullable I input, Callable<O> callable) {
        return fromListenableFuture(logger, rpcMethodName, input, () -> Futures.immediateFuture(callable.call()));
    }

    public static final class FutureRpcResultBuilder<I, O> {
        private static final Function<Throwable, String> DEFAULT_ERROR_MESSAGE_FUNCTION = Throwable::getMessage;
        private static final Consumer<Throwable> DEFAULT_ON_FAILURE = throwable -> { };

        private final Consumer<O> defaultOnSuccess = result -> { };

        // fixed (final) builder values
        private final Logger logger;
        private final String rpcMethodName;
        private final @Nullable I input;
        private final Callable<ListenableFuture<O>> callable;

        // optional builder values, which can be overridden by users
        private Function<Throwable, String> rpcErrorMessageFunction = DEFAULT_ERROR_MESSAGE_FUNCTION;
        private Consumer<O> onSuccessConsumer = defaultOnSuccess;
        private Consumer<Throwable> onFailureConsumer = DEFAULT_ON_FAILURE;

        // defaulted builder values, which can be overridden by users
        private LogLevel onEnterLogLevel = LogLevel.TRACE;
        private LogLevel onSuccessLogLevel = LogLevel.DEBUG;
        private LogLevel onFailureLogLevel = LogLevel.ERROR;

        private FutureRpcResultBuilder(Logger logger, String rpcMethodName, @Nullable I input,
                Callable<ListenableFuture<O>> callable) {
            this.logger = logger;
            this.rpcMethodName = rpcMethodName;
            this.input = input;
            this.callable = callable;
        }

        /**
         * Builds the Future RpcResult.
         *
         * @return Future RpcResult. Note that this will NEVER be a failed Future; any
         *         errors are reported as !{@link RpcResult#isSuccessful()}, with
         *         details in {@link RpcResult#getErrors()}, and not the Future itself.
         */
        @CheckReturnValue
        @SuppressWarnings("checkstyle:IllegalCatch")
        public ListenableFuture<RpcResult<O>> build() {
            SettableFuture<RpcResult<O>> futureRpcResult = SettableFuture.create();
            FutureCallback<O> callback = new FutureCallback<>() {
                @Override
                public void onSuccess(O result) {
                    onSuccessLogLevel.log(logger, "RPC {}() successful; input = {}, output = {}", rpcMethodName,
                            input, result);
                    onSuccessConsumer.accept(result);
                    futureRpcResult.set(RpcResultBuilder.success(result).build());
                }

                @Override
                public void onFailure(Throwable cause) {
                    onFailureLogLevel.log(logger, "RPC {}() failed; input = {}", rpcMethodName, input, cause);
                    onFailureConsumer.accept(cause);
                    RpcResultBuilder<O> rpcResultBuilder =  RpcResultBuilder.failed();
                    if (cause instanceof OperationFailedException) {
                        // NB: This looses (not not propagate) the cause, and only preserves the error list
                        // But we did log the cause above, so it can still be found.
                        rpcResultBuilder.withRpcErrors(((OperationFailedException) cause).getErrorList());
                    } else {
                        rpcResultBuilder.withError(APPLICATION, rpcErrorMessageFunction.apply(cause), cause);
                    }
                    futureRpcResult.set(rpcResultBuilder.build());
                }
            };
            try {
                onEnterLogLevel.log(logger, "RPC {}() entered; input = {}", rpcMethodName, input);
                Futures.addCallback(callable.call(), callback, MoreExecutors.directExecutor());
            } catch (Exception cause) {
                callback.onFailure(cause);
            }
            return futureRpcResult;
        }

        /**
         * Sets a custom on-failure action, for a given exception.
         */
        public FutureRpcResultBuilder<I,O> onFailure(Consumer<Throwable> newOnFailureConsumer) {
            if (onFailureConsumer != DEFAULT_ON_FAILURE) {
                throw new IllegalStateException("onFailure can only be set once");
            }
            this.onFailureConsumer = newOnFailureConsumer;
            return this;
        }

        /**
         * Sets a custom on-failure SLF4J logging level, in case of an exception. The log message mentions the RPC
         * method name, the provided input, the exception and its stack trace (depending on logger settings).
         * By default, it is {@code LOG.error}. Setting {@code NONE} will disable this logging.
         */
        public FutureRpcResultBuilder<I,O> onFailureLogLevel(LogLevel level) {
            this.onFailureLogLevel = level;
            return this;
        }

        /**
         * Sets a custom on-success SLF4J logging level. The log message mentions the RPC method name, the provided
         * input, and the resulting output.
         * By default, it is {@code LOG.debug}. Setting {@code NONE} will disable this logging.
         */
        public FutureRpcResultBuilder<I,O> onSuccessLogLevel(LogLevel level) {
            this.onSuccessLogLevel = level;
            return this;
        }

        /**
         * Sets a custom on-enter SLF4J logging level. The log message mentions the RPC method name and the provided
         * input.
         * By default, it is {@code LOG.trace}. Setting {@code NONE} will disable this logging.
         */
        public FutureRpcResultBuilder<I,O> onEnterLogLevel(LogLevel level) {
            this.onEnterLogLevel = level;
            return this;
        }

        /**
         * Set a custom {@link RpcError} message function, for a given exception.
         * By default, the message is just {@link Throwable#getMessage()}.
         */
        public FutureRpcResultBuilder<I,O> withRpcErrorMessage(Function<Throwable, String> newRpcErrorMessageFunction) {
            if (rpcErrorMessageFunction != DEFAULT_ERROR_MESSAGE_FUNCTION) {
                throw new IllegalStateException("rpcErrorMessage can only be set once");
            }
            this.rpcErrorMessageFunction = newRpcErrorMessageFunction;
            return this;
        }

        /**
         * Sets a custom on-success action, for a given output.
         */
        public FutureRpcResultBuilder<I,O> onSuccess(Consumer<O> newOnSuccessFunction) {
            if (onSuccessConsumer != defaultOnSuccess) {
                throw new IllegalStateException("onSuccess can only be set once");
            }
            this.onSuccessConsumer = newOnSuccessFunction;
            return this;
        }

    }
}
