package sg.edu.nus.iss.voucher.feed.workflow.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import sg.edu.nus.iss.voucher.feed.workflow.utility.EncryptionUtils;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class EncryptionUtilsTest {

    @InjectMocks
    private EncryptionUtils encryptionUtils;

    @Autowired
    private String aesSecretKey = "0123456789ABCDEF0123456789ABCDEF"; 

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(encryptionUtils, "aesSecretKey", aesSecretKey);
    }

    @Test
    public void testEncryptAndDecrypt() throws Exception {
        String originalString = "TestString";

        String encryptedString = encryptionUtils.encrypt(originalString);
        
        String decryptedString = encryptionUtils.decrypt(encryptedString);

        assertEquals(originalString, decryptedString);
    }

    @Test
    public void testDecryptWithInvalidKey() {
        String invalidEncryptedString = "INVALID_STRING";

        assertThrows(Exception.class, () -> {
            encryptionUtils.decrypt(invalidEncryptedString);
        });
    }
}
