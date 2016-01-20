/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.notification.supplier.impl.helper;

import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 *
 */
public class TestChangeEventBuildHelper {

    private TestChangeEventBuildHelper() {
        throw new UnsupportedOperationException("Test utility class");
    }

    public static AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> createTestDataEvent(
            final Map<InstanceIdentifier<?>, DataObject> createdData,
            final Map<InstanceIdentifier<?>, DataObject> updatedData,
            final Set<InstanceIdentifier<?>> removedData) {
        return new AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject>() {

            @Override
            public DataObject getUpdatedSubtree() {
                return mock(DataObject.class);
            }

            @Override
            public Map<InstanceIdentifier<?>, DataObject> getUpdatedData() {
                if (updatedData != null) {
                    return Collections.unmodifiableMap(updatedData);
                } else {
                    return Collections.emptyMap();
                }
            }

            @Override
            public Set<InstanceIdentifier<?>> getRemovedPaths() {
                if (removedData != null) {
                    return Collections.unmodifiableSet(removedData);
                } else {
                    return Collections.emptySet();
                }
            }

            @Override
            public DataObject getOriginalSubtree() {
                return mock(DataObject.class);
            }

            @Override
            public Map<InstanceIdentifier<?>, DataObject> getOriginalData() {
                return Collections.emptyMap();
            }

            @Override
            public Map<InstanceIdentifier<?>, DataObject> getCreatedData() {
                if (createdData != null) {
                    return Collections.unmodifiableMap(createdData);
                } else {
                    return Collections.emptyMap();
                }
            }
        };
    }

    public static AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> createEmptyTestDataEvent() {
        return new AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject>() {

            @Override
            public DataObject getUpdatedSubtree() {
                return mock(DataObject.class);
            }

            @Override
            public Map<InstanceIdentifier<?>, DataObject> getUpdatedData() {
                return Collections.emptyMap();
            }

            @Override
            public Set<InstanceIdentifier<?>> getRemovedPaths() {
                return Collections.emptySet();
            }

            @Override
            public DataObject getOriginalSubtree() {
                return mock(DataObject.class);
            }

            @Override
            public Map<InstanceIdentifier<?>, DataObject> getOriginalData() {
                return Collections.emptyMap();
            }

            @Override
            public Map<InstanceIdentifier<?>, DataObject> getCreatedData() {
                return Collections.emptyMap();
            }
        };
    }

    public static AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> createNullTestDataEvent() {
        return new AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject>() {

            @Override
            public DataObject getUpdatedSubtree() {
                return null;
            }

            @Override
            public Map<InstanceIdentifier<?>, DataObject> getUpdatedData() {
                return null;
            }

            @Override
            public Set<InstanceIdentifier<?>> getRemovedPaths() {
                return null;
            }

            @Override
            public DataObject getOriginalSubtree() {
                return null;
            }

            @Override
            public Map<InstanceIdentifier<?>, DataObject> getOriginalData() {
                return null;
            }

            @Override
            public Map<InstanceIdentifier<?>, DataObject> getCreatedData() {
                return null;
            }
        };
    }
}

