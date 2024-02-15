package pm.selenium.examples.browsermob_proxy;

import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.proxy.CaptureType;

public class App {

	static {
		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "error");
	}

	public static void main(String[] args) {

		BrowserMobProxyServer proxy = null;
		ChromeDriver chrome = null;

		try {
			proxy = new BrowserMobProxyServer();
			proxy.start();
			System.out.println("Proxy started at port " + proxy.getPort());

			var options = new ChromeOptions();
			options.setProxy(ClientUtil.createSeleniumProxy(proxy));
			chrome = new ChromeDriver(options);
			System.out.println("Chrome started");

			proxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT);
			proxy.newHar("test");
			
			chrome.navigate().to("https://resttesttest.com/");
			chrome.findElement(By.id("urlvalue")).clear();
			chrome.findElement(By.id("urlvalue")).sendKeys("https://httpbin.org/json");
			chrome.findElement(By.id("submitajax")).click();		
			proxy.waitForQuiescence(1, 5, TimeUnit.SECONDS);
			
			var entry = proxy.getHar().getLog().getEntries().stream()
				.filter((e) -> e.getRequest().getUrl().equals("https://httpbin.org/json"))
				.findFirst().get();
			
			System.out.println(toString(entry));
			System.out.println(entry.getResponse().getContent().getText());

		} finally {
			if (chrome != null) {
				chrome.quit();
				System.out.println("Chrome stopped");
			}
			if (proxy != null) {
				proxy.stop();
				System.out.println("Proxy stopped");
			}
		}
	}
	
	private static String toString(HarEntry e) {
		return String.format("[%s] \"%s %s %s\" %d %d",
			new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z").format(e.getStartedDateTime()),
			e.getRequest().getMethod(), 
			e.getRequest().getUrl(),
			e.getRequest().getHttpVersion(), 
			e.getResponse().getStatus(),
			e.getResponse().getBodySize());
	}

}
