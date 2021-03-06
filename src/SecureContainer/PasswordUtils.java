package SecureContainer;

import java.security.SecureRandom;
import java.util.Base64;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/*
Quetsa classe prende spunto dal seguente articolo:
https://dev.to/awwsmm/how-to-encrypt-a-password-in-java-42dh
 */
public class PasswordUtils {
    private static final SecureRandom RAND = new SecureRandom();
    private static final int salt_length = 512;
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 512;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA512";
    private static final String salt = PasswordUtils.generateSalt();

    /*
     * Genera una stringa casuale partendo da un array di byte lungo length
     * @requires length >= 1
     * @throws IllegalArgumentException se length < 1
     * @return Restituisce una stringa generata in modo casuale
     */
    private static String generateSalt (final int length) throws IllegalArgumentException{
        if (length < 1) throw new IllegalArgumentException("length must be > 0");

        byte[] salt = new byte[length];
        RAND.nextBytes(salt);

        return Base64.getEncoder().encodeToString(salt);
    }

    /*
     * Genera una stringa casuale
     * @return Restituisce una stringa generata in modo casuale
     */
    private static String generateSalt () {
        return generateSalt(salt_length);
    }

    /*
     * Produce hash di password utilizzando salt.
     * Se si verifica un errore durante la generazione della stringa hash viene restituito null
     * @requires password != null && salt != null && !password.isEmpty()
     * @throws NullPointerException se password = null || salt = null
     * @throws IllegalArgumentException se password.isEmpty()
     * @return Restituisce la stringa hash associata a password se nessun errore si verifica durante la sua generazione
     *         altrimenti restituisce null.
     */
    public static String hashPassword (String password) throws NullPointerException, IllegalArgumentException{
        if (password == null) throw new NullPointerException("password must be != null");
        if (salt == null) throw new NullPointerException("salt must be != null");
        if (password.isEmpty()) throw new IllegalArgumentException("password can't be empty!");

        char[] chars = password.toCharArray();
        byte[] bytes = salt.getBytes();

        PBEKeySpec spec = new PBEKeySpec(chars, bytes, ITERATIONS, KEY_LENGTH);

        Arrays.fill(chars, Character.MIN_VALUE);

        try {
            SecretKeyFactory fac = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] securePassword = fac.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(securePassword);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            System.err.println("Exception encountered in hashPassword()");
            return null;
        } finally {
            spec.clearPassword();
        }
    }

    /*
     * Verifica se l'hash di password è uguale a key
     * @requires password != null && key != null && salt != null && !password.isEmpty() && !key.isEmpty()
     * @throws NullPointerException se password = null || key = null || salt = null
     * @throws IllegalArgumentException se password.isEmpty() || key.isEmpty()
     * @return true se l'hash di password coincide con la stringa key
     */
    public static boolean verifyPassword (String password, String key) throws NullPointerException, IllegalArgumentException{
        if (password == null) throw new NullPointerException("password must be != null");
        if (key == null) throw new NullPointerException("key must be != null");
        if (salt == null) throw new NullPointerException("salt must be != null");
        if (password.isEmpty()) throw new IllegalArgumentException("password cant't be empty!");
        if (key.isEmpty()) throw new IllegalArgumentException("key cant't be empty!");

        String optEncrypted = hashPassword(password);
        if (optEncrypted == null) return false;
        return optEncrypted.equals(key);
    }
}
