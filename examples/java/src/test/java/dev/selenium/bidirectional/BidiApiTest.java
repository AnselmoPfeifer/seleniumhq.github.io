package dev.selenium.bidirectional;

import com.google.common.io.Resources;
import dev.selenium.BaseTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.HasAuthentication;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.ScriptKey;
import org.openqa.selenium.UsernameAndPassword;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Predicate;

public class BidiApiTest extends BaseTest {

  @Test
  public void basicAuth() {
    driver = new ChromeDriver();

    Predicate<URI> uriPredicate = uri -> uri.toString().contains("herokuapp.com");
    ((HasAuthentication) driver).register(uriPredicate, UsernameAndPassword.of("admin", "admin"));

    driver.get("https://the-internet.herokuapp.com/basic_auth");
    driver.findElement(By.tagName("p")).isDisplayed();
    Assertions.assertEquals(
        "Congratulations! You must have the proper credentials.",
        driver.findElement(By.tagName("p")).getText());
  }

  @Test
  public void pinScript() throws IOException {
    driver = new ChromeDriver();

    URL resource = getClass().getResource("/org/openqa/selenium/remote/isDisplayed.js");
    String function = Resources.toString(resource, StandardCharsets.UTF_8);
    String isDisplayed =
        String.format("/* isDisplayed */return (%s).apply(null, arguments);", function);

    JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
    ScriptKey displayedKey = jsExecutor.pin(isDisplayed);

    driver.get("https://www.selenium.dev/selenium/web/javascriptPage.html");

    WebElement hiddenLink = driver.findElement(By.id("hiddenlink"));
    WebElement shown = driver.findElement(By.id("visibleSubElement"));

    Assertions.assertFalse((Boolean) jsExecutor.executeScript(displayedKey, hiddenLink));
    Assertions.assertTrue((Boolean) jsExecutor.executeScript(displayedKey, shown));
  }

  @Test
  public void mutationObservation() {}

  @Test
  public void consoleLogs() {}

  @Test
  public void jsErrors() {}

  @Test
  public void interceptResponses() {}

  @Test
  public void interceptRequests() {}
}
