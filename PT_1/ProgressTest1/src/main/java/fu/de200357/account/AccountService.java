package fu.de200357.account;

import java.time.LocalDate;
import java.util.Optional;

public class AccountService {
    public static final int MAX_FAILED_ATTEMPTS = 5;
    public static final int PASSWORD_HISTORY_SIZE = 3;
    public static final int MIN_AGE = 18;

    public AccountService() {
        // TODO
    }

    public ResultCode register(String username, String email, String password,
                               String confirmPassword, LocalDate dateOfBirth, String phone) {
        throw new UnsupportedOperationException("TODO");
    }

    public ResultCode login(String username, String password) {
        throw new UnsupportedOperationException("TODO");
    }

    public ResultCode disableAccount(String username) {
        throw new UnsupportedOperationException("TODO");
    }

    public ResultCode unlockAccount(String username) {
        throw new UnsupportedOperationException("TODO");
    }

    public Optional findByUsername(String username) {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean isLocked(String username) {
        throw new UnsupportedOperationException("TODO");
    }
}
