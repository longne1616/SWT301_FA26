package fu.de200357;

import fu.de200357.account.AccountValidator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class AccountValidatorTest {
    // --- USERNAME ---
    @ParameterizedTest
    @ValueSource(strings = {"alice", "Alice_01", "Z____"})
    void isValidUsername_Valid(String username) {
        assertTrue(AccountValidator.isValidUsername(username));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ab_1", "1alice", "_alice", "ali ce", "alice!", "alice-01"})
    @NullAndEmptySource
    void isValidUsername_Invalid(String username) {
        assertFalse(AccountValidator.isValidUsername(username));
    }

    @ParameterizedTest(name = "Độ dài username {0} -> {1}")
    @MethodSource("usernameLengths")
    void isValidUsername_BoundaryLength(int length, boolean expected) {
        assertEquals(expected, AccountValidator.isValidUsername("a".repeat(length)));
    }

    static Stream usernameLengths() {
        return Stream.of(
                Arguments.of(4, false),
                Arguments.of(5, true),
                Arguments.of(6, true),
                Arguments.of(19, true),
                Arguments.of(20, true),
                Arguments.of(21, false)
        );
    }

    // --- EMAIL ---
    @ParameterizedTest
    @CsvSource({
            "user@example.com, true",
            "user.name@domain.co.uk, true",
            "user@domain, false",
            "@domain.com, false",
            "user@.com, false"
    })
    void isValidEmail_Partitions(String email, boolean expected) {
        assertEquals(expected, AccountValidator.isValidEmail(email));
    }

    @ParameterizedTest
    @MethodSource("emailBoundaryLengths")
    void isValidEmail_BoundaryLength(int localLength, boolean expected) {
        String email = "a".repeat(localLength) + "@g.com";
        assertEquals(expected, AccountValidator.isValidEmail(email));
    }

    static Stream emailBoundaryLengths() {
        return Stream.of(
                Arguments.of(93, true),  // 93 + 6 = 99 ký tự  -> true
                Arguments.of(94, true),  // 94 + 6 = 100 ký tự -> true (Biên 100)
                Arguments.of(95, false)  // 95 + 6 = 101 ký tự -> false (Vượt quá 100)
        );
    }

    // --- PASSWORD ---
    @ParameterizedTest(name = "[{index}] {3}")
    @CsvSource(delimiter = '|', value = {
            "Secret@123    | alice_01 | true  | Mật khẩu hợp lệ",
            "secret@123    | alice_01 | false | Thiếu chữ hoa",
            "SECRET@123    | alice_01 | false | Thiếu chữ thường",
            "Secretpass    | alice_01 | false | Thiếu ký tự đặc biệt và số",
            "'Secret @123' | alice_01 | false | Chứa khoảng trắng",
            "Xalice_01@1   | alice_01 | false | Chứa username",
            "Xalice_01@1   |          | true  | Username null -> bỏ qua kiểm tra username"
    })
    void isValidPassword_Partitions(String pw, String user, boolean expected, String desc) {
        assertEquals(expected, AccountValidator.isValidPassword(pw, user));
    }

    @ParameterizedTest
    @MethodSource("passwordBoundaryLengths")
    void isValidPassword_BoundaryLength(int length, boolean expected) {
        String base = "A1@a";
        String pw = base + "a".repeat(length - base.length());
        assertEquals(expected, AccountValidator.isValidPassword(pw, "user"));
    }

    static Stream passwordBoundaryLengths() {
        return Stream.of(
                Arguments.of(7, false),
                Arguments.of(8, true),
                Arguments.of(32, true),
                Arguments.of(33, false)
        );
    }

    // --- PHONE ---
    @ParameterizedTest
    @ValueSource(strings = {"0312345678", "0512345678", "0712345678", "0812345678", "0912345678"})
    void isValidPhone_Valid(String phone) {
        assertTrue(AccountValidator.isValidPhone(phone));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0123456789", "031234567", "03123456789", "abc1234567"})
    @NullAndEmptySource
    void isValidPhone_Invalid(String phone) {
        assertFalse(AccountValidator.isValidPhone(phone));
    }

    // --- AGE ---
    @ParameterizedTest(name = "Sinh {0}, ngày tính {1} -> {2} tuổi")
    @CsvSource({
            "2008-09-28, 2026-09-28, 18",
            "2008-09-29, 2026-09-28, 17",
            "2008-02-29, 2026-02-28, 17",
            "2008-02-29, 2026-03-01, 18"
    })
    void calculateAge_Boundaries(LocalDate dob, LocalDate today, int expected) {
        assertEquals(expected, AccountValidator.calculateAge(dob, today));
    }
}