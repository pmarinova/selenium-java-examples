package pm.selenium.examples.browsermob_proxy;

import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
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
			proxy.newHar("google.com");

			chrome.navigate().to("https://www.google.com/");

			System.out.println("Waiting for network traffic to stop...");
			proxy.waitForQuiescence(1, 5, TimeUnit.SECONDS);

			proxy.getHar().getLog().getEntries().forEach((entry) -> {
				System.out.println(String.format("[%s] \"%s %s %s\" %d %d",
						new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z").format(entry.getStartedDateTime()),
						entry.getRequest().getMethod(), 
						entry.getRequest().getUrl(),
						entry.getRequest().getHttpVersion(), 
						entry.getResponse().getStatus(),
						entry.getResponse().getBodySize()));
			});

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

}
