package http;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.junit.Test;


public class HttpRequestTest {
	private String testDirectory = "./src/test/resources/";
	
	@Test
	public void request_GET() throws FileNotFoundException {
		InputStream in = new FileInputStream(new File(testDirectory+"Http_GET.txt"));
		HttpRequest request = new HttpRequest(in);
		
		assertEquals(HttpMethod.GET, request.getMethod());
		assertEquals("/user/create", request.getPath());
		assertEquals("keep-alive", request.getHeader("Connection"));
		assertEquals("javajigi", request.getParameter("userId"));
		
	}

}
