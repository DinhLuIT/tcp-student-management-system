package com.ptit.studentmgmt.server.crypto;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Dịch vụ Mật mã học DES (Data Encryption Standard).
 * - Thuật toán: DES (Khóa đối xứng 64-bit / 56-bit hiệu dụng).
 * - Chế độ: CBC (Cipher Block Chaining) kết hợp Vector khởi tạo IV 8 bytes.
 * - Cơ chế đệm: PKCS5Padding.
 * - Định dạng lưu trữ: Base64 String (an toàn cho DB & Network).
 */
public class DESCryptoService {
    private static final String ALGORITHM = "DES";
    private static final String TRANSFORMATION = "DES/CBC/PKCS5Padding";

    // Khóa đối xứng 8 bytes và IV 8 bytes cố định trên Server
    private static final byte[] DEFAULT_KEY_BYTES = "P@ssw0rd".getBytes(StandardCharsets.UTF_8);
    private static final byte[] DEFAULT_IV_BYTES  = "12345678".getBytes(StandardCharsets.UTF_8);

    private final SecretKey secretKey;
    private final IvParameterSpec ivSpec;

    public DESCryptoService() {
        this(DEFAULT_KEY_BYTES, DEFAULT_IV_BYTES);
    }

    public DESCryptoService(byte[] keyBytes, byte[] ivBytes) {
        if (keyBytes.length != 8) {
            throw new IllegalArgumentException("Khóa DES bắt buộc phải có độ dài chính xác 8 bytes (64 bits)!");
        }
        if (ivBytes.length != 8) {
            throw new IllegalArgumentException("Vector khởi tạo IV bắt buộc phải có độ dài chính xác 8 bytes!");
        }
        this.secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
        this.ivSpec = new IvParameterSpec(ivBytes);
    }

    /**
     * Mã hóa văn bản rõ (Plaintext) thành chuỗi Ciphertext định dạng Base64
     */
    public synchronized String encrypt(String plainText) throws Exception {
        if (plainText == null) return null;
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * Giải mã chuỗi Ciphertext Base64 về văn bản rõ (Plaintext) ban đầu
     */
    public synchronized String decrypt(String base64CipherText) throws Exception {
        if (base64CipherText == null || base64CipherText.trim().isEmpty()) return "";
        byte[] cipherBytes = Base64.getDecoder().decode(base64CipherText);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
        byte[] decryptedBytes = cipher.doFinal(cipherBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}
