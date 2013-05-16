/* Copyright (c) 2012 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.arcticweb.site.proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * Servlet to proxy AisView JSON requests
 */
public class JsonProxyServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	private static final Logger LOG = Logger.getLogger(JsonProxyServlet.class);

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public JsonProxyServlet() {
		super();
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		
		//String baseUrl = "http://linux06.fomfrv.dk:8081/aisview/rest";
		String baseUrl = "http://localhost:8092/aisview/rest";
		String url = baseUrl;
		if (request.getPathInfo() != null) {
			url += request.getPathInfo();
		}
		if (request.getQueryString() != null) {
			url += "?" + request.getQueryString(); 
		}
		
		LOG.debug("JSOPN proxy request for service: " + url);
		
		String output;
		try {
			output = requestUrl(url);
			response.setStatus(HttpServletResponse.SC_OK);			
		} catch (IOException e) {
			output = "{\"error\":true}";
			response.setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT);
		}
		out.print(output);
	}

	private String requestUrl(String urlStr) throws IOException {
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("GET");

		if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
			throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
		}

		BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

		StringBuilder sb = new StringBuilder();
		String output;
		while ((output = br.readLine()) != null) {
			sb.append(output);
		}
		conn.disconnect();
		return sb.toString();
	}

}
