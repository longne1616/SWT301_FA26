
package fu.de200357.account;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class AccountService {
    public static final int MAX_FAILED_ATTEMPTS = 5;
    public static final int PASSWORD_HISTORY_SIZE = 3;
    public static final int MIN_AGE = 18;

    private final Map<String, Account> accounts = new HashMap<>();
    private final Map<String, String> emailToUsernameMap = new HashMap<>();

    public AccountService() {}

    public ResultCode register(String username, String email, String password,
                               String confirmPassword, LocalDate dateOfBirth, String phone) {
        LocalDate today = LocalDate.now();

        // REG-01: Thông tin bắt buộc
        if (isBlank(username) || isBlank(email) || isBlank(password) || isBlank(confirmPassword)
                || dateOfBirth == null || dateOfBirth.isAfter(today)) {
            return ResultCode.INVALID_INPUT;
        }

        // REG-02: Định dạng username
        if (!AccountValidator.isValidUsername(username)) {
            return ResultCode.INVALID_USERNAME;
        }

        // REG-04: Định dạng email
        if (!AccountValidator.isValidEmail(email)) {
            return ResultCode.INVALID_EMAIL;
        }

        // REG-06: Mật khẩu yếu
        if (!AccountValidator.isValidPassword(password, username)) {
            return ResultCode.WEAK_PASSWORD;
        }

        // REG-07: Khớp mật khẩu
        if (!password.equals(confirmPassword)) {
            return ResultCode.PASSWORD_MISMATCH;
        }

        // REG-08: Độ tuổi tối thiểu
        if (AccountValidator.calculateAge(dateOfBirth, today) < MIN_AGE) {
            return ResultCode.UNDERAGE;
        }

        // REG-09: Số điện thoại
        if (phone != null && !phone.isEmpty()) {
            if (!AccountValidator.isValidPhone(phone)) {
                return ResultCode.INVALID_PHONE;
            }
        }

        String userKey = key(username);
        String emailKey = key(email);

        // REG-03: Trùng username
        if (accounts.containsKey(userKey)) {
            return ResultCode.DUPLICATE_USERNAME;
        }

        // REG-05: Trùng email
        if (emailToUsernameMap.containsKey(emailKey)) {
            return ResultCode.DUPLICATE_EMAIL;
        }

        // REG-10: Đăng ký thành công
        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hash(salt, password);

        Account account = new Account(
                username, emailKey, dateOfBirth, phone, salt, hash
        );

        accounts.put(userKey, account);
        emailToUsernameMap.put(emailKey, userKey);

        return ResultCode.SUCCESS;
    }

    // LOGIN
    public ResultCode login(String username, String password) {
        if (isBlank(username) || isBlank(password)) {
            return ResultCode.INVALID_INPUT;
        }

        Account acc = accounts.get(key(username));

        if (acc == null) {
            return ResultCode.INVALID_CREDENTIALS;
        }

        if (acc.getStatus() == AccountStatus.DISABLED) {
            return ResultCode.ACCOUNT_DISABLED;
        }

        if (acc.isLocked()) {
            return ResultCode.ACCOUNT_LOCKED;
        }

        if (!PasswordHasher.matches(
                acc.getSalt(), password, acc.getCurrentPasswordHash())) {

            acc.incrementFailedAttempts();

            if (acc.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
                acc.lock();
                return ResultCode.ACCOUNT_LOCKED;
            }

            return ResultCode.INVALID_CREDENTIALS;
        }

        acc.resetFailedAttempts();
        return ResultCode.SUCCESS;
    }

    // DISABLE ACCOUNT
    public ResultCode disableAccount(String username) {
        Optional<Account> accOpt = findByUsername(username);

        if (accOpt.isEmpty()) {
            return ResultCode.USER_NOT_FOUND;
        }

        accOpt.get().setStatus(AccountStatus.DISABLED);

        return ResultCode.SUCCESS;
    }

    // UNLOCK ACCOUNT
    public ResultCode unlockAccount(String username) {
        Optional<Account> accOpt = findByUsername(username);

        if (accOpt.isEmpty()) {
            return ResultCode.USER_NOT_FOUND;
        }

        accOpt.get().unlock();

        return ResultCode.SUCCESS;
    }

    // FIND ACCOUNT
    public Optional<Account> findByUsername(String username) {
        if (isBlank(username)) {
            return Optional.empty();
        }

        return Optional.ofNullable(accounts.get(key(username)));
    }

    // CHECK LOCK STATUS
    public boolean isLocked(String username) {
        Optional<Account> acc = findByUsername(username);

        return acc.map(Account::isLocked).orElse(false);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String key(String s) {
        return s.toLowerCase(Locale.ROOT);
    }
}