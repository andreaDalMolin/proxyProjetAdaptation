package security;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import static consts.Const.DEBUG_MODE;

public class AES {
    public static final int GCM_IV_LENGTH = 12;
    public static final int GCM_TAG_LENGTH = 16;

    public static String encrypt(String privateString, SecretKey skey) throws Exception {
//        if (DEBUG_MODE) return privateString;
        byte[] iv = new byte[GCM_IV_LENGTH];
        (new SecureRandom()).nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec ivSpec = new GCMParameterSpec(GCM_TAG_LENGTH * Byte.SIZE, iv);
        cipher.init(Cipher.ENCRYPT_MODE, skey, ivSpec);

        byte[] ciphertext = cipher.doFinal(privateString.getBytes("UTF8"));
        byte[] encrypted = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, encrypted, 0, iv.length);
        System.arraycopy(ciphertext, 0, encrypted, iv.length, ciphertext.length);
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decrypt(String encrypted, SecretKey skey) throws Exception {
//        if (DEBUG_MODE) return encrypted;
        byte[] decoded = Base64.getDecoder().decode(encrypted);

        byte[] iv = Arrays.copyOfRange(decoded, 0, GCM_IV_LENGTH);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec ivSpec = new GCMParameterSpec(GCM_TAG_LENGTH * Byte.SIZE, iv);
        cipher.init(Cipher.DECRYPT_MODE, skey, ivSpec);

        byte[] ciphertext = cipher.doFinal(decoded, GCM_IV_LENGTH, decoded.length - GCM_IV_LENGTH);
        return new String(ciphertext, StandardCharsets.UTF_8);
    }
}