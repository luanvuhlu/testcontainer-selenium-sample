package com.luanvv.testcontainers.selenium;

import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(HelloWorldContainerExtension.class)
public class BaseWebDriverContainer {

    protected static void doSimpleExplore(BrowserWebDriverContainer<?> rule) {
        RemoteWebDriver driver = setupDriverFromRule(rule);
        System.out.println("Selenium remote URL is: " + rule.getSeleniumAddress());
        System.out.println("VNC URL is: " + rule.getVncAddress());

        driver.get("http://helloworld:8080");
        WebElement title = driver.findElement(By.tagName("h1"));

        assertThat(title.getText().trim())
                .as("the index page contains the title 'Hello world'")
                .isEqualTo("Hello world");
    }

    protected void assertBrowserNameIs(BrowserWebDriverContainer<?> rule, String expectedName) {
        RemoteWebDriver driver = setupDriverFromRule(rule);
        String actual = driver.getCapabilities().getBrowserName();
        assertThat(actual).as(String.format("actual browser name is %s", actual)).isEqualTo(expectedName);
    }

    private static RemoteWebDriver setupDriverFromRule(BrowserWebDriverContainer<?> rule) {
        RemoteWebDriver driver = rule.getWebDriver();
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        return driver;
    }
}
