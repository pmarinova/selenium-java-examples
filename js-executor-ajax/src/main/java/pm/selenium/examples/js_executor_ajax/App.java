package pm.selenium.examples.js_executor_ajax;

import static org.openqa.selenium.remote.http.HttpMethod.GET;
import static org.openqa.selenium.remote.http.HttpMethod.POST;

import java.util.Map;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.http.HttpMethod;

public class App {
	
	private final WebDriver driver;
	private final JavascriptExecutor jse;
	
	public static void main(String[] args) {
		new App(new ChromeDriver()).run();
	}
	
	public App(WebDriver driver) {
		this.driver = driver;
		this.jse = (JavascriptExecutor)driver;
	}
	
	public void run() {
		try {
			driver.navigate().to("https://httpbin.org");
			
			Map<?,?> response = null; 
			
			// GET request with fetch()
			response = fetch(GET, "/get", Map.of("param1", "value1", "param2", "value2"));
			System.out.println(response);
			
			// POST request with fetch()
			response = fetch(POST, "/post", Map.of("param1", "value1", "param2", "value2"));
			System.out.println(response);
			
		} finally {
			this.driver.quit();
		}
	}
	
	private Map<?,?> fetch(HttpMethod method, String url, Map<String, String> params) {
		var script = """
			const method = arguments[0];
			const url = arguments[1];
			const params = arguments[2];
			
			const queryParams = (method === 'GET') ? 
			  '?' + Object.keys(params).map(k => `${encodeURIComponent(k)}=${encodeURIComponent(params[k])}`).join('&') : '';
			
			const options = {
			  method,
			  body: (method === 'POST') ? JSON.stringify(params) : undefined
			};
			
			const response = await fetch(url + queryParams, options);
			const json = await response.json();
			return json;
		""";
		
		return (Map<?,?>)jse.executeScript(script, method.name(), url, params);
	}
}
