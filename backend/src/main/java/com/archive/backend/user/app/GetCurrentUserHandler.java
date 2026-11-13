package com.archive.backend.user.app;

import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.SignInRequiredException;
import com.archive.backend.user.domain.UserId;
import com.archive.backend.user.read.CurrentUser;
import com.archive.backend.user.read.UserReadRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetCurrentUserHandler {

    private final UserReadRepository repository;

    /**
     * The user of the access token; nobody for a public visitor. A visitor whose browser holds a session to refresh is
     * told to refresh it first.
     */
    @Transactional(readOnly = true)
    public Optional<CurrentUser> handle(GetCurrentUserQuery q) {
        UserId id = q.actor().id();
        if (id == null) {
            if (q.refresh() != null) {
                throw new SignInRequiredException();
            }
            return Optional.empty();
        }
        return repository.findCurrent(id.value()).map(user -> user.withAbilities(abilities(q.actor())));
    }

    /** What the actor may do beyond reading, by the codes the frontend knows (docs/features/accounts.md). */
    private static List<String> abilities(Actor actor) {
        List<String> abilities = new ArrayList<>();
        if (actor.mayAddEntries()) {
            abilities.add("add-entries");
        }
        if (actor.mayKeepCategories()) {
            abilities.add("keep-categories");
        }
        return List.copyOf(abilities);
    }
}
