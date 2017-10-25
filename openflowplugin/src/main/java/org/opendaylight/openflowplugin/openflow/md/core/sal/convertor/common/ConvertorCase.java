/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common;

import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;

/**
 * The Convertor case used in {@link org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorProcessor}.
 *
 * @param <FROM> the source type
 * @param <TO>   the result type
 * @param <DATA> the data type
 */
public abstract class ConvertorCase<FROM, TO, DATA extends ConvertorData> {
    private final List<Short> supportedVersions;
    private final Class<FROM> type;
    private final boolean errorOnEmpty;

    /**
     * Instantiates a new Convertor case.
     *
     * @param type              the type
     * @param errorOnEmpty      the error on empty
     * @param supportedVersions the supported versions
     */
    protected ConvertorCase(Class<FROM> type, boolean errorOnEmpty, Short... supportedVersions) {
        this.type = type;
        this.errorOnEmpty = errorOnEmpty;
        this.supportedVersions = Arrays.asList(Preconditions.checkNotNull(supportedVersions));
    }

    /**
     * Process source and return result, what can be empty
     *
     *
     * @param source the source
     * @param data   the data
     * @param convertorExecutor convertor executor
     * @return the optional
     */
    public abstract Optional<TO> process(@Nonnull final FROM source, final DATA data, final ConvertorExecutor convertorExecutor);

    /**
     * Should {@link org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorProcessor}
     * throw error when result of process method is empty?
     *
     * @return the boolean
     */
    boolean isErrorOnEmpty() {
        return errorOnEmpty;
    }

    /**
     * Cast untyped source to type of this case and sends it to actual process method.
     *
     *
     * @param source the source
     * @param data   the data
     * @param convertorExecutor convertor executor
     * @return the optional
     */
    Optional<TO> processRaw(@Nonnull final Object source, final DATA data, final ConvertorExecutor convertorExecutor) {
        return process(getType().cast(source), data, convertorExecutor);
    }

    /**
     * Gets type of this convertor case.
     *
     * @return the type
     */
    Class<FROM> getType() {
        return type;
    }

    /**
     * Gets supported Openflow versions.
     *
     * @return the supported versions
     */
    List<Short> getSupportedVersions() {
        return supportedVersions;
    }
}
