package breder.net.server.web;

import java.io.IOException;

public interface WebSocket {
	
	public String readString() throws IOException;

	public byte[] readBytes() throws IOException;

	public void writeString(String string) throws IOException;

	public void close() throws IOException;

}
