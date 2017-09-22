import java.io.EOFException;
import java.io.IOException;

import breder.net.server.web.WebServerSocket;
import breder.net.server.web.WebSocket;

public class WebServerSocketTest {

	public static void main(String[] args) throws IOException {
		WebServerSocket server = new WebServerSocket(9090);
		for (;;) {
			try {
				WebSocket socket = server.accept();
				try {
					long timer = System.currentTimeMillis();
					for (int n = 0; n <= 64 * 1024; n++) {
						String text = socket.readString();
						socket.writeString(n + "[" + text + "]");
						if (n % 1024 == 0) {
							System.out.println(n / 1024);
						}
					}
					System.out.println(System.currentTimeMillis() - timer);
				} catch (EOFException e) {
				} finally {
					socket.close();
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

}
