package com.mediconnect.util;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtil {
    private PasswordUtil() { }
    public static String hash(char[] password) { return BCrypt.hashpw(new String(password), BCrypt.gensalt(12)); }
    public static boolean verify(char[] password, String hash) { return hash != null && BCrypt.checkpw(new String(password), hash); }
}
