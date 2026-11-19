package com.ptit.studentmgmt.crypto;

import com.ptit.studentmgmt.server.crypto.DESCryptoService;

public class DESCryptoServiceTest {
    public static void main(String[] args) throws Exception {
        DESCryptoService crypto = new DESCryptoService();

        // Test 1: Tiếng Việt có dấu
        String originalName = "Nguyễn Văn Hoàng Nam";
        String encryptedName = crypto.encrypt(originalName);
        String decryptedName = crypto.decrypt(encryptedName);

        assert !originalName.equals(encryptedName) : "Ciphertext không được giống Plaintext!";
        assert originalName.equals(decryptedName) : "Giải mã phải trả về đúng chuỗi ban đầu!";

        // Test 2: Điểm số thực
        String originalScore = "8.75";
        String encryptedScore = crypto.encrypt(originalScore);
        String decryptedScore = crypto.decrypt(encryptedScore);
        assert originalScore.equals(decryptedScore) : "Điểm số sau giải mã phải nguyên vẹn!";

        System.out.println(">>> [PASS] Tất cả các bài test cho module DESCryptoService đều thành công 100%!");
        System.out.println("    Bản rõ gốc: " + originalName);
        System.out.println("    Mã hóa DES Base64: " + encryptedName);
        System.out.println("    Giải mã về: " + decryptedName);
    }
}
