package miniproject1.paymentmanagementsystem.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {
    private static final int SALT_ROUNDS = 12;

    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(SALT_ROUNDS));
    }

    public static boolean verifyPassword(String password, String hash) {
        return BCrypt.checkpw(password, hash);
    }
}

