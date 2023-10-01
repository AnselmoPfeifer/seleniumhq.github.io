package dev.selenium.bidirectional;

import com.google.common.io.Resources;
import dev.selenium.BaseTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Credentials;
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
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class BidiApiTest extends BaseTest {

  @BeforeEach
  public void createSession() {
    driver = new ChromeDriver();
  }

  @Test
  public void basicAuthentication() {
    Predicate<URI> uriPredicate = uri -> uri.toString().contains("herokuapp.com");
    Supplier<Credentials> authentication = UsernameAndPassword.of("admin", "admin");
    ((HasAuthentication) driver).register(uriPredicate, authentication);

    driver.get("https://the-internet.herokuapp.com/basic_auth");
    driver.findElement(By.tagName("p")).isDisplayed();
    String successMessage = "Congratulations! You must have the proper credentials.";
    WebElement elementMessage = driver.findElement(By.tagName("p"));
    Assertions.assertEquals(successMessage, elementMessage.getText());
  }

  @Test
  public void pinScript() throws IOException {
    driver.get("https://www.selenium.dev/selenium/web/javascriptPage.html");

    URL resource = getClass().getResource("/org/openqa/selenium/remote/isDisplayed.js");
    String function = Resources.toString(Objects.requireNonNull(resource), StandardCharsets.UTF_8);
    String format = "/* isDisplayed */return (%s).apply(null, arguments);";
    String isDisplayedScript = String.format(format, function);

    JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
    ScriptKey displayedKey = jsExecutor.pin(isDisplayedScript);

    WebElement hidden = driver.findElement(By.id("hiddenlink"));
    WebElement visible = driver.findElement(By.id("visibleSubElement"));

    Object isVisibleDisplayed = jsExecutor.executeScript(displayedKey, visible);
    Object isHiddenDisplayed = jsExecutor.executeScript(displayedKey, hidden);

    Assertions.assertTrue((Boolean) isVisibleDisplayed);
    Assertions.assertFalse((Boolean) isHiddenDisplayed);
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
