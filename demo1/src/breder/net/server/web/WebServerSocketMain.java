package breder.net.server.web;

import java.io.EOFException;
import java.io.IOException;
import java.sql.SQLException;

public class WebServerSocketMain {

	public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
		// Class.forName("com.mysql.jdbc.Driver");
		// String prefix = "jdbc:mysql://localhost/vitrinii";
		// Connection c = DriverManager.getConnection(prefix, "root",
		// "comvitrinii");
		// c.setAutoCommit(false);
		WebServerSocket server = new WebServerSocket(9090);
		for (;;) {
			try {
				WebSocket socket = server.accept();
				try {
					long timer = System.currentTimeMillis();
					for (int n = 0; n <= 128; n++) {
						String text = socket.readString();
						socket.writeString(n + "[" + text + "]");
					}
					System.out.println("Total: " + (System.currentTimeMillis() - timer) + " milisec");
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
