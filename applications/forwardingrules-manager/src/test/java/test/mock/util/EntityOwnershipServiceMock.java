package test.mock.util;

import com.google.common.base.Optional;
import org.opendaylight.controller.md.sal.common.api.clustering.*;

import javax.annotation.Nonnull;

/**
 * Created by vishnoianil on 2/4/16.
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
        return null;
    }

    @Override
    public boolean isCandidateRegistered(@Nonnull Entity entity) {
        return false;
    }
}
