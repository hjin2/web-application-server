package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

	private Socket connection;

	public RequestHandler(Socket connectionSocket) {
		this.connection = connectionSocket;
	}

	public void run() {
		log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
				connection.getPort());

		try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
			
			// index.html 응답하기
			BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			String str = br.readLine();
			log.debug("request line : {} ", str);

			StringTokenizer st = new StringTokenizer(str);
			String[] arr = new String[3];
			int idx = 0;
			while (st.hasMoreTokens()) {
				arr[idx] = st.nextToken();
				idx += 1;
			}
			String page = arr[1];

			if (str == null)
				return;

			// POST방식으로 회원가입하기
			int contentLength = 0;
			while (!str.equals("")) {
				str = br.readLine();
				if(str.contains("Content-Length"))
					contentLength = getContentLength(str);
				log.debug("header : {} ", str);
			}
			
			if("/user/create".equals(page)) {
				String body = IOUtils.readData(br, contentLength);
				Map<String, String> params = HttpRequestUtils.parseQueryString(body);
				User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
				log.debug("User : {} ", user);
				page="/index.html";
				DataOutputStream dos = new DataOutputStream(out);
				response302Header(dos,"/index.html");
			}
			DataOutputStream dos = new DataOutputStream(out);
			byte[] body = Files.readAllBytes(new File("./webapp" + page).toPath());
			response200Header(dos, body.length);
			responseBody(dos, body);
	
		

			// GET방식으로 회원가입하기
			/*
			if (page.startsWith("/user/create")) {
				st = new StringTokenizer(page, "&|=");
				String[] forms = new String[8];
				idx = 0;
				while (st.hasMoreTokens()) {
					forms[idx] = st.nextToken();
					idx++;
				}
				String tmp = "";
				for (int i = 0; i < forms[7].indexOf("%"); i++) {
					tmp += forms[7].charAt(i);
				}
				tmp += "@";
				for (int i = forms[7].indexOf("%") + 3; i < forms[7].length(); i++) {
					tmp += forms[7].charAt(i);
				}

				User user = new User(forms[1], forms[3], forms[5], tmp);
				log.debug("User : {} ", user);
			} else {
				DataOutputStream dos = new DataOutputStream(out);
				byte[] body = Files.readAllBytes(new File("./webapp" + page).toPath());
				response200Header(dos, body.length);
				responseBody(dos, body);

			}
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		*/

		
		}catch(IOException e) {
			log.error(e.getMessage());
		}
	}
		/*
			
		String url = page;
		if(url.startsWith("/user/create")) {
			int index = url.indexOf("?");
			String queryString = url.substring(index+1);
			Map<String, String> params = HttpRequestUtils.parseQueryString(queryString);
			User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
			log.debug("User : {} ", user);
		}else {
			DataOutputStream dos = new DataOutputStream(out);
			byte[] body = Files.readAllBytes(new File("./webapp" + page).toPath());
			response200Header(dos, body.length);
			responseBody(dos, body);
		}
		}catch(IOException e) {
			log.error(e.getMessage());
		}
	}
		*/	


	private void response302Header(DataOutputStream dos, String url) {
		try {
			dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
			dos.writeBytes("Location : " + url + "\r\n");
			dos.writeBytes("\r\n");
		}catch(IOException e) {
			log.error(e.getMessage());
		}
		
	}

	private int getContentLength(String str) {
		StringTokenizer st = new StringTokenizer(str);
		String tmp = st.nextToken();
		int length = Integer.parseInt(st.nextToken());
		return length;
	}

	private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
			dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void responseBody(DataOutputStream dos, byte[] body) {
		try {
			dos.write(body, 0, body.length);
			dos.flush();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
}
