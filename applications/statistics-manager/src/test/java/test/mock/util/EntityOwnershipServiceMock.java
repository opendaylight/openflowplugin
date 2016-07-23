/*
 * Copyright (c) 2014, 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package test.mock.util;

import com.google.common.base.Optional;
import org.opendaylight.controller.md.sal.common.api.clustering.*;

import javax.annotation.Nonnull;

/**
 * Created by vishnoianil on 1/13/16.
 */
public class EntityOwnershipServiceMock implements EntityOwnershipService {
    @Override
    public EntityOwnershipCandidateRegistration registerCandidate(@Nonnull Entity entity) throws CandidateAlreadyRegisteredException {
        return null;
    }

    @Override
    public EntityOwnershipListenerRegistration registerListener(@Nonnull String entityType, @Nonnull EntityOwnershipListener listener) {
        return null;
    }

    @Override
    public Optional<EntityOwnershipState> getOwnershipState(@Nonnull Entity forEntity) {
        return Optional.of(new EntityOwnershipState(true,true));
    }

    @Override
    public boolean isCandidateRegistered(@Nonnull Entity entity) {
        return true;
    }
}
