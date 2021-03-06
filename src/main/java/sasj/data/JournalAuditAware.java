package sasj.data;

import sasj.data.user.User;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

// TODO make it work
public class JournalAuditAware implements AuditorAware<User> {
    private static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User currentUser) {
        JournalAuditAware.currentUser = currentUser;
    }

    @Override
    public Optional<User> getCurrentAuditor() {
        return Optional.ofNullable(currentUser);
    }
}
