package fu.de200357.account;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

public class PasswordHasher {
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {}

    public static String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return HexFormat.of().formatHex(salt);
    }

    public static String hash(String salt, String rawPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(md.digest(rawPassword.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static boolean matches(String salt, String rawPassword, String expectedHash) {
        if (salt == null || rawPassword == null || expectedHash == null) return false;
        return hash(salt, rawPassword).equalsIgnoreCase(expectedHash);
    }
}
