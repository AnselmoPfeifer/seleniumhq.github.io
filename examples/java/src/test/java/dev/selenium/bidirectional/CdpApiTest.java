package dev.selenium.bidirectional;

import com.google.common.collect.ImmutableMap;
import dev.selenium.BaseTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Base64;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.devtools.v117.network.Network;
import org.openqa.selenium.devtools.v117.network.model.Headers;
import org.openqa.selenium.devtools.v117.performance.Performance;
import org.openqa.selenium.devtools.v117.performance.model.Metric;
import org.openqa.selenium.devtools.v117.runtime.Runtime;

public class CdpApiTest extends BaseTest {
  DevTools devTools;

  @BeforeEach
  public void createSession() {
    driver = new ChromeDriver();
  }

  @Test
  public void setCookie() {
    devTools = ((HasDevTools) driver).getDevTools();
    devTools.createSession();

    devTools.send(
        Network.setCookie(
            "cheese",
            "gouda",
            Optional.empty(),
            Optional.of("www.selenium.dev"),
            Optional.empty(),
            Optional.of(true),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty()));

    driver.get("https://www.selenium.dev");
    Cookie cheese = driver.manage().getCookieNamed("cheese");
    Assertions.assertEquals("gouda", cheese.getValue());
  }

  @Test
  public void performanceMetrics() {
    driver.get("https://www.selenium.dev/selenium/web/frameset.html");

    devTools = ((HasDevTools) driver).getDevTools();
    devTools.createSession();

    devTools.send(Performance.enable(Optional.empty()));

    List<Metric> metricList = devTools.send(Performance.getMetrics());

    Map<String, Number> metrics = new HashMap<>();
    for (Metric metric : metricList) {
      metrics.put(metric.getName(), metric.getValue());
    }

    Assertions.assertTrue(metrics.get("DevToolsCommandDuration").doubleValue() > 0);
    Assertions.assertEquals(12, metrics.get("Frames").intValue());
  }

  @Test
  public void basicAuth() {
    devTools = ((HasDevTools) driver).getDevTools();
    devTools.createSession();

    devTools.send(Network.enable(Optional.of(100000), Optional.of(100000), Optional.of(100000)));

    String encodedAuth = Base64.getEncoder().encodeToString("admin:admin".getBytes());
    Map<String, Object> headers = ImmutableMap.of("Authorization", "Basic " + encodedAuth);

    devTools.send(Network.setExtraHTTPHeaders(new Headers(headers)));

    driver.get("https://the-internet.herokuapp.com/basic_auth");

    Assertions.assertEquals(
        "Congratulations! You must have the proper credentials.",
        driver.findElement(By.tagName("p")).getText());
  }

  @Test
  public void consoleLogs() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);

    DevTools devTools = ((HasDevTools) driver).getDevTools();
    devTools.createSession();
    devTools.send(Runtime.enable());

    List<String> messages = new ArrayList<>();
    devTools.addListener(
        Runtime.consoleAPICalled(),
        event -> {
          String value = (String) event.getArgs().get(0).getValue().orElse("");
          messages.add(value);
          latch.countDown();
        });

    driver.get("https://www.selenium.dev/selenium/web/xhtmlTest.html");
    ((JavascriptExecutor) driver).executeScript("console.log('I love cheese')");
    latch.await();
    Assertions.assertEquals("I love cheese", messages.get(0));
  }
}
