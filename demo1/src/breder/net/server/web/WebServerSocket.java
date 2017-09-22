package breder.net.server.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import breder.util.Base64;
import breder.util.MD5;
import breder.util.Sha1;

public class WebServerSocket {

	private ServerSocket server;

	public WebServerSocket(int port) throws IOException {
		this.server = new ServerSocket(port);
	}

	public WebSocket accept() throws IOException {
		for (;;) {
			Socket socket = this.server.accept();
			WebSocketRequest request = this.getRequest(socket);
			if (acceptChrome(request)) {
				this.sendConnectChrome(socket, request);
				return new ChromeWebSocket(socket);
			} else if (acceptSafari(request)) {
				this.sendConnectSafari(socket, request);
				return new SafariWebSocket(socket);
			} else {
				socket.close();
			}
		}
	}

	protected boolean acceptChrome(WebSocketRequest request) {
		Map<String, String> headers = request.getHeaders();
		return headers.containsKey("Sec-WebSocket-Key");
	}

	protected boolean acceptSafari(WebSocketRequest request) {
		Map<String, String> headers = request.getHeaders();
		return headers.containsKey("Sec-WebSocket-Key1") && headers.containsKey("Sec-WebSocket-Key2");
	}

	private void sendConnectSafari(Socket socket, WebSocketRequest request) throws IOException {
		byte[] reqBytes = new byte[8];
		socket.getInputStream().read(reqBytes);
		OutputStream output = socket.getOutputStream();
		Map<String, String> headers = request.getHeaders();
		String[] keys = { headers.get("Sec-WebSocket-Key1"), headers.get("Sec-WebSocket-Key2") };
		StringBuilder[] numbers = { new StringBuilder(12), new StringBuilder(12) };
		int[] spacess = { 0, 0 };
		for (int k = 0; k < keys.length; k++) {
			String key = keys[k];
			StringBuilder number = numbers[k];
			for (int n = 0; n < key.length(); n++) {
				char c = key.charAt(n);
				if (Character.isDigit(c)) {
					number.append(c);
				} else if (Character.isWhitespace(c)) {
					spacess[k]++;
				}
			}
		}
		byte[][] bytes = new byte[2][4];
		for (int k = 0; k < keys.length; k++) {
			long value = Long.parseLong(numbers[k].toString());
			value = value / spacess[k];
			bytes[k][0] = (byte) (value >> 24);
			bytes[k][1] = (byte) ((value >> 16) & 0xFF);
			bytes[k][2] = (byte) ((value >> 8) & 0xFF);
			bytes[k][3] = (byte) (value & 0xFF);
		}
		byte[] key = new byte[16];
		System.arraycopy(bytes[0], 0, key, 0, 4);
		System.arraycopy(bytes[1], 0, key, 4, 4);
		System.arraycopy(reqBytes, 0, key, 8, 8);
		key = MD5.encode(key);
		output.write("HTTP/1.1 101 WebSocket Protocol Handshake\r\n".getBytes());
		output.write("Upgrade: Websocket\r\n".getBytes());
		output.write("Connection: Upgrade\r\n".getBytes());
		output.write(("Sec-WebSocket-Origin: " + request.getHeaders().get("Origin") + "\r\n").getBytes());
		output.write(("Sec-WebSocket-Location: ws://" + request.getHeaders().get("Host") + "/\r\n").getBytes());
		output.write(("\r\n").getBytes());
		output.write(key);
	}

	private void sendConnectChrome(Socket socket, WebSocketRequest request) throws IOException {
		OutputStream output = socket.getOutputStream();
		String accept = Base64.encode(Sha1.encode(request.getHeaders().get("Sec-WebSocket-Key") + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"));
		output.write("HTTP/1.1 101 Switching Protocols\r\n".getBytes());
		output.write("Upgrade: Websocket\r\n".getBytes());
		output.write("Connection: Upgrade\r\n".getBytes());
		output.write(("Sec-WebSocket-Accept: " + accept + "\r\n").getBytes());
		output.write("\r\n".getBytes());
	}

	private WebSocketRequest getRequest(Socket socket) throws IOException {
		InputStream input = socket.getInputStream();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		int state = 0;
		for (int n; ((n = input.read()) != -1);) {
			if (n == '\n' || n == '\r') {
				state++;
			} else {
				state = 0;
			}
			output.write(n);
			if (state == 4) {
				break;
			}
		}
		String request = new String(output.toByteArray());
		request = request.substring(0, request.length() - 4);
		String[] split = request.split("\r\n");
		String method = split[0].substring(0, split[0].indexOf(' '));
		String servlet = split[0].substring(method.length() + 1, split[0].indexOf(' ', method.length() + 1));
		String protocol = split[0].substring(split[0].lastIndexOf(' ') + 1);
		Map<String, String> headers = new HashMap<String, String>();
		for (int n = 1; n < split.length; n++) {
			String item = split[n];
			String key = item.substring(0, item.indexOf(':'));
			String value = item.substring(key.length() + 2);
			headers.put(key, value);
		}
		return new WebSocketRequest(method, servlet, protocol, headers);
	}

	// Safari - http://tools.ietf.org/html/draft-hixie-thewebsocketprotocol-76
	// Chrome - http://tools.ietf.org/html/rfc6455

}
