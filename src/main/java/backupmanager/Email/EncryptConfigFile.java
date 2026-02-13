package backupmanager.Email;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class EncryptConfigFile {
    private static final String SECRET_KEY = "BManagerSbureria";

    public static void main(String[] args) throws Exception {
        byte[] plaintext = Files.readAllBytes(Paths.get("config.txt"));

        SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] encrypted = cipher.doFinal(plaintext);
        String encoded = Base64.getEncoder().encodeToString(encrypted);

        Files.write(Paths.get("config.enc"), encoded.getBytes(StandardCharsets.UTF_8));
        System.out.println("File config.enc created succesfully");
    }
}
