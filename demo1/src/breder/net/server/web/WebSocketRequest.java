package breder.net.server.web;

import java.util.Map;

class WebSocketRequest {

	private String method;

	private String servlet;

	private String protocol;

	private Map<String, String> headers;

	public WebSocketRequest(String method, String servlet, String protocol, Map<String, String> headers) {
		super();
		this.method = method;
		this.servlet = servlet;
		this.protocol = protocol;
		this.headers = headers;
	}

	/**
	 * @return the method
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * @param method
	 *            the method to set
	 */
	public void setMethod(String method) {
		this.method = method;
	}

	/**
	 * @return the servlet
	 */
	public String getServlet() {
		return servlet;
	}

	/**
	 * @param servlet
	 *            the servlet to set
	 */
	public void setServlet(String servlet) {
		this.servlet = servlet;
	}

	/**
	 * @return the protocol
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * @param protocol
	 *            the protocol to set
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	/**
	 * @return the headers
	 */
	public Map<String, String> getHeaders() {
		return headers;
	}

	/**
	 * @param headers
	 *            the headers to set
	 */
	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

}
