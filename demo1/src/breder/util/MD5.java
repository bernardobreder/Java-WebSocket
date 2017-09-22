package breder.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {

	public static byte[] encode(byte[] bytes) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(bytes, 0, bytes.length);
			return md.digest();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

}
