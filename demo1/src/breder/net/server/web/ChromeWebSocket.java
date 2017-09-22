package breder.net.server.web;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ChromeWebSocket implements WebSocket {

	private final Socket socket;
	private final InputStream input;
	private final OutputStream output;

	ChromeWebSocket(Socket socket) throws IOException {
		this.socket = socket;
		this.input = socket.getInputStream();
		this.output = socket.getOutputStream();
	}

	public String readString() throws IOException {
		int c1 = read();
		boolean finalFragment = c1 >> 7 == 1;
		if (!finalFragment) {
			throw new RuntimeException();
		}
		int opcode = c1 & 0xF;
		if (opcode != 0x1) {
			throw new IOException("content not a string");
		}
		int c2 = read();
		int length = c2 & 0x7F;
		if (length <= 125) {
		} else if (length == 126) {
			int c3 = read();
			int c4 = read();
			length = c3 << 8 + c4;
		} else {
			int c3 = read();
			int c4 = read();
			int c5 = read();
			int c6 = read();
			int c7 = read();
			int c8 = read();
			int c9 = read();
			int c10 = read();
			length = (c3 << 56) + (c4 << 48) + (c5 << 40) + (c6 << 32) + (c7 << 24) + (c8 << 16) + (c9 << 8) + c10;
		}
		int[] mask = new int[4];
		for (int n = 0; n < mask.length; n++) {
			mask[n] = read();
		}
		StringBuilder sb = new StringBuilder(length);
		for (int n = 0; n < length; n++) {
			int i1 = read() ^ mask[n % 4];
			if (i1 <= 0x7F) {
				sb.append((char) i1);
			} else if ((i1 >> 5) == 0x6) {
				int i2 = read() ^ mask[++n % 4];
				sb.append((char) (((i1 & 0x1F) << 6) + (i2 & 0x3F)));
			} else {
				int i2 = read() ^ mask[++n % 4];
				int i3 = read() ^ mask[++n % 4];
				sb.append((char) (((i1 & 0xF) << 12) + ((i2 & 0x3F) << 6) + (i3 & 0x3F)));
			}
		}
		return sb.toString();
	}

	private int read() throws IOException {
		int c = input.read();
		if (c < 0) {
			throw new EOFException();
		}
		return c;
	}

	public byte[] readBytes() throws IOException {
		int c1 = read();
		boolean finalFragment = c1 >> 7 == 1;
		if (!finalFragment) {
			throw new RuntimeException();
		}
		int opcode = c1 & 0xF;
		if (opcode != 0x2) {
			throw new IOException("content not a binary");
		}
		int c2 = read();
		int length = c2 & 0x7F;
		if (length <= 125) {
		} else if (length == 126) {
			int c3 = read();
			int c4 = read();
			length = c3 << 8 + c4;
		} else {
			int c3 = read();
			int c4 = read();
			int c5 = read();
			int c6 = read();
			int c7 = read();
			int c8 = read();
			int c9 = read();
			int c10 = read();
			length = (c3 << 56) + (c4 << 48) + (c5 << 40) + (c6 << 32) + (c7 << 24) + (c8 << 16) + (c9 << 8) + c10;
		}
		int[] mask = new int[4];
		for (int n = 0; n < mask.length; n++) {
			mask[n] = read();
		}
		byte[] bytes = new byte[length];
		for (int i = 0; i < length; i++) {
			int j = i % 4;
			int c = read();
			c = c ^ mask[j];
			bytes[i] = (byte) (c ^ mask[j]);
		}
		return bytes;
	}

	public void writeString(String string) throws IOException {
		OutputStream output = this.socket.getOutputStream();
		output.write(0x81);
		byte[] bytes = toBytes(string);
		int length = bytes.length;
		if (length <= 125) {
			output.write((int) length);
		} else if (length <= 65535) {
			output.write(126);
			output.write((int) (length >> 8));
			output.write((int) (length & 0xFF));
		} else {
			long llength = bytes.length;
			output.write(127);
			output.write((int) (llength >> 56));
			output.write((int) ((llength >> 48) & 0xFF));
			output.write((int) ((llength >> 40) & 0xFF));
			output.write((int) ((llength >> 32) & 0xFF));
			output.write((int) ((llength >> 24) & 0xFF));
			output.write((int) ((llength >> 16) & 0xFF));
			output.write((int) ((llength >> 8) & 0xFF));
			output.write((int) (llength & 0xFF));
		}
		for (int n = 0; n < length; n++) {
			output.write(bytes[n]);
		}
	}

	public void close() throws IOException {
		output.write(0x88);
		output.write(0x0);
		this.socket.close();
	}

	private static byte[] toBytes(String text) {
		ByteArrayOutputStream output = new ByteArrayOutputStream(text.length() * 2);
		int length = text.length();
		for (int n = 0; n < length; n++) {
			char c = text.charAt(n);
			if (c <= 0x7F) {
				output.write(c);
			} else if (c <= 0x7FF) {
				output.write(((c >> 6) & 0x1F) + 0xC0);
				output.write((c & 0x3F) + 0x80);
			} else {
				output.write(((c >> 12) & 0xF) + 0xE0);
				output.write(((c >> 6) & 0x3F) + 0x80);
				output.write((c & 0x3F) + 0x80);
			}
		}
		return output.toByteArray();
	}

}
