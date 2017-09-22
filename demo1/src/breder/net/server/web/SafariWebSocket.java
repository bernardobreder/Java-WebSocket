package breder.net.server.web;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SafariWebSocket implements WebSocket {

	private final Socket socket;

	private final InputStream input;

	private final OutputStream output;

	SafariWebSocket(Socket socket) throws IOException {
		this.socket = socket;
		this.input = socket.getInputStream();
		this.output = socket.getOutputStream();
	}

	public String readString() throws IOException {
		InputStream input = this.socket.getInputStream();
		int c = read();
		if (c == 0) {
			StringBuilder sb = new StringBuilder();
			for (; ((c = input.read()) != 0xFF);) {
				if (c <= 0x7F) {
					sb.append((char) c);
				} else if ((c >> 5) == 0x6) {
					int i2 = read();
					sb.append((char) (((c & 0x1F) << 6) + (i2 & 0x3F)));
				} else {
					int i2 = read();
					int i3 = read();
					sb.append((char) (((c & 0xF) << 12) + ((i2 & 0x3F) << 6) + (i3 & 0x3F)));
				}
			}
			return sb.toString();
		} else {
			throw new RuntimeException();
		}
	}

	private int read() throws IOException {
		int c = input.read();
		if (c < 0) {
			throw new EOFException();
		}
		return c;
	}

	public byte[] readBytes() throws IOException {
		throw new RuntimeException();
	}

	public void writeString(String text) throws IOException {
		output.write(0x00);
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
		output.write(0xFF);
	}

	public void close() throws IOException {
		this.socket.close();
	}

}
