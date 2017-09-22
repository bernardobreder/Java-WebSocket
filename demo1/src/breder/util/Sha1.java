package breder.util;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sha1 {

	private static final Charset CHARSET = Charset.forName("utf-8");

	private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

	public static String encodeToHex(String text) {
		byte[] sha1hash = encode(text);
		StringBuilder sb = new StringBuilder(sha1hash.length * 2);
		for (byte b : sha1hash) {
			sb.append(HEX_CHARS[(b & 0xF0) >> 4]);
			sb.append(HEX_CHARS[b & 0x0F]);
		}
		return sb.toString();
	}

	public static byte[] encode(String text) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(text.getBytes(CHARSET), 0, text.length());
			return md.digest();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

}
