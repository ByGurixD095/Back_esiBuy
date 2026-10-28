package esi.grupo5.esiBuy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"MONGODB_URI=mongodb://localhost:27017/esibuy-test",
		"app.password-reset-url=http://localhost/reset?token=",
		"jwt.secret=0123456789012345678901234567890123456789012345678901234567890123",
		"jwt.expiration=60000",
		"jwt.refresh.expiration=120000",
		"mail.username=test@example.com",
		"mail.password=test-password"
})
class EsiBuyApplicationTests {

	@Test
	void contextLoads() {
	}

}
