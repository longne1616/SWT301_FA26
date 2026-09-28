
package fu.de200357;

import fu.de200357.account.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class AccountServiceTest {
    private AccountService service;

    private static final String USER = "alice_01";
    private static final String EMAIL = "alice@example.com";
    private static final String PASS = "Secret@123";
    private static final LocalDate DOB = LocalDate.now().minusYears(20);
    private static final String PHONE = "0912345678";

    @BeforeEach
    void setUp() {
        service = new AccountService();
    }

    @Nested
    class Register {

        @Test
        void register_Success_CreatesAccountCorrectly() {
            ResultCode result = service.register(
                    USER, EMAIL, PASS, PASS, DOB, PHONE
            );

            assertEquals(ResultCode.SUCCESS, result);

            Optional<Account> accOpt = service.findByUsername(USER);

            assertTrue(accOpt.isPresent());

            Account acc = accOpt.get();

            assertEquals(AccountStatus.ACTIVE, acc.getStatus());
            assertEquals(0, acc.getFailedAttempts());
            assertFalse(acc.isLocked());
            assertNotEquals(PASS, acc.getCurrentPasswordHash());
            assertEquals(EMAIL.toLowerCase(), acc.getEmail());
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("fu.de200357.AccountServiceTest#invalidRegisterInputs")
        void register_InvalidInput_ReturnsExpectedCode(
                String desc, String u, String e, String p, String c,
                LocalDate dob, String phone, ResultCode expected) {

            assertEquals(
                    expected,
                    service.register(u, e, p, c, dob, phone)
            );

            assertTrue(service.findByUsername(u).isEmpty());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void register_BlankUsername_ReturnsInvalidInput(
                String invalidUsername) {

            assertEquals(
                    ResultCode.INVALID_INPUT,
                    service.register(
                            invalidUsername, EMAIL, PASS, PASS, DOB, PHONE
                    )
            );
        }

        @ParameterizedTest
        @ValueSource(strings = {"alice_01", "ALICE_01"})
        void register_DuplicateUsernameCaseInsensitive_ReturnsDuplicateUsername(
                String duplicateUser) {

            service.register(USER, EMAIL, PASS, PASS, DOB, PHONE);

            assertEquals(
                    ResultCode.DUPLICATE_USERNAME,
                    service.register(
                            duplicateUser, "other@example.com",
                            PASS, PASS, DOB, PHONE
                    )
            );
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "alice@example.com",
                "ALICE@EXAMPLE.COM"
        })
        void register_DuplicateEmailCaseInsensitive_ReturnsDuplicateEmail(
                String duplicateEmail) {

            service.register(USER, EMAIL, PASS, PASS, DOB, PHONE);

            assertEquals(
                    ResultCode.DUPLICATE_EMAIL,
                    service.register(
                            "other_user", duplicateEmail,
                            PASS, PASS, DOB, PHONE
                    )
            );
        }

        @ParameterizedTest(name = "Tuổi tương đối: minusYears({0}) plusDays({1}) -> {2}")
        @CsvSource({
                "18, 0, SUCCESS",
                "18, 1, UNDERAGE",
                "0, 1, INVALID_INPUT"
        })
        void register_AgeBoundary(
                int yearsAgo, int plusDays, ResultCode expected) {

            LocalDate dob = LocalDate.now()
                    .minusYears(yearsAgo)
                    .plusDays(plusDays);

            assertEquals(
                    expected,
                    service.register(
                            "temp_user", "temp@example.com",
                            PASS, PASS, dob, null
                    )
            );
        }
    }

    static Stream<Arguments> invalidRegisterInputs() {
        return Stream.of(
                Arguments.of(
                        "Username sai định dạng",
                        "1alice", EMAIL, PASS, PASS, DOB, PHONE,
                        ResultCode.INVALID_USERNAME
                ),
                Arguments.of(
                        "Email sai định dạng",
                        USER, "bademail", PASS, PASS, DOB, PHONE,
                        ResultCode.INVALID_EMAIL
                ),
                Arguments.of(
                        "Mật khẩu yếu",
                        USER, EMAIL, "weak", "weak", DOB, PHONE,
                        ResultCode.WEAK_PASSWORD
                ),
                Arguments.of(
                        "Xác nhận mật khẩu không khớp",
                        USER, EMAIL, PASS, "Wrong@123", DOB, PHONE,
                        ResultCode.PASSWORD_MISMATCH
                ),
                Arguments.of(
                        "Chưa đủ tuổi",
                        USER, EMAIL, PASS, PASS,
                        LocalDate.now().minusYears(17), PHONE,
                        ResultCode.UNDERAGE
                ),
                Arguments.of(
                        "Số điện thoại không hợp lệ",
                        USER, EMAIL, PASS, PASS, DOB, "123",
                        ResultCode.INVALID_PHONE
                ),

                // Các test thứ tự ưu tiên
                Arguments.of(
                        "Ưu tiên: Username sai + Email sai -> INVALID_USERNAME",
                        "1alice", "bademail", PASS, PASS, DOB, PHONE,
                        ResultCode.INVALID_USERNAME
                ),
                Arguments.of(
                        "Ưu tiên: Email sai + MK yếu -> INVALID_EMAIL",
                        USER, "bademail", "weak", "weak", DOB, PHONE,
                        ResultCode.INVALID_EMAIL
                ),
                Arguments.of(
                        "Ưu tiên: MK yếu + confirm lệch -> WEAK_PASSWORD",
                        USER, EMAIL, "weak", "x", DOB, PHONE,
                        ResultCode.WEAK_PASSWORD
                )
        );

    }
    @Nested
    class Login {

        @BeforeEach
        void registerAccount() {
            service.register(USER, EMAIL, PASS, PASS, DOB, PHONE);
        }

        @Test
        void login_Success_ResetsFailedAttempts() {
            service.login(USER, "Wrong@123");
            assertEquals(1, service.findByUsername(USER).get().getFailedAttempts());

            ResultCode result = service.login(USER, PASS);
            assertEquals(ResultCode.SUCCESS, result);
            assertEquals(0, service.findByUsername(USER).get().getFailedAttempts());
        }

        @Test
        void login_NonExistentUser_ReturnsInvalidCredentials() {
            assertEquals(ResultCode.INVALID_CREDENTIALS, service.login("non_existent", PASS));
        }

        @ParameterizedTest
        @ValueSource(strings = {PASS, "Wrong@123"})
        void login_DisabledAccount_ReturnsAccountDisabled(String password) {
            service.disableAccount(USER);
            assertEquals(ResultCode.ACCOUNT_DISABLED, service.login(USER, password));
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 4})
        void login_FailedAttemptsUnderThreshold_IncrementsCounter(int attempts) {
            for (int i = 0; i < attempts - 1; i++) {
                service.login(USER, "Wrong@123");
            }
            ResultCode result = service.login(USER, "Wrong@123");

            assertEquals(ResultCode.INVALID_CREDENTIALS, result);
            assertEquals(attempts, service.findByUsername(USER).get().getFailedAttempts());
            assertFalse(service.isLocked(USER));
        }

        @Test
        void login_5thFailedAttempt_LocksAccount() {
            for (int i = 0; i < 4; i++) {
                service.login(USER, "Wrong@123");
            }
            ResultCode result = service.login(USER, "Wrong@123");

            assertEquals(ResultCode.ACCOUNT_LOCKED, result);
            assertTrue(service.isLocked(USER));
        }

        @ParameterizedTest
        @ValueSource(strings = {PASS, "Wrong@123"})
        void login_AlreadyLockedAccount_ReturnsAccountLockedWithoutIncrementingCounter(String password) {
            for (int i = 0; i < 5; i++) {
                service.login(USER, "Wrong@123");
            }
            int failedAttempts = service.findByUsername(USER).get().getFailedAttempts();

            assertEquals(ResultCode.ACCOUNT_LOCKED, service.login(USER, password));
            assertEquals(failedAttempts, service.findByUsername(USER).get().getFailedAttempts());
        }

        @ParameterizedTest(name = "[{index}] {0} lần sai -> đăng nhập mật khẩu đúng -> {1}")
        @CsvSource({
                "4, SUCCESS, false",
                "5, ACCOUNT_LOCKED, true"
        })
        void login_CorrectPasswordAfterNFailures(int failures, ResultCode expected, boolean locked) {
            for (int i = 0; i < failures; i++) {
                service.login(USER, "Wrong@123");
            }
            ResultCode result = service.login(USER, PASS);
            assertEquals(expected, result);
            assertEquals(locked, service.isLocked(USER));
        }

        @Test
        void login_AfterAdminUnlock_CounterRestartsAndCanLogin() {
            for (int i = 0; i < 5; i++) service.login(USER, "Wrong@123");
            assertEquals(ResultCode.SUCCESS, service.unlockAccount(USER));

            assertFalse(service.isLocked(USER));
            assertEquals(ResultCode.INVALID_CREDENTIALS, service.login(USER, "Wrong@123"));
            assertEquals(1, service.findByUsername(USER).get().getFailedAttempts());
            assertEquals(ResultCode.SUCCESS, service.login(USER, PASS));
        }

        @ParameterizedTest
        @ValueSource(strings = {"alice_01", "ALICE_01"})
        void login_UsernameCaseInsensitive(String loginUsername) {
            assertEquals(ResultCode.SUCCESS, service.login(loginUsername, PASS));
        }

        @Test
        void login_PasswordCaseSensitive_ReturnsInvalidCredentials() {
            assertEquals(ResultCode.INVALID_CREDENTIALS, service.login(USER, "secret@123"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        void login_NullOrEmptyInput_ReturnsInvalidInput(String invalidStr) {
            assertEquals(ResultCode.INVALID_INPUT, service.login(invalidStr, PASS));
            assertEquals(ResultCode.INVALID_INPUT, service.login(USER, invalidStr));
        }
    }
}