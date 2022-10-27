package com.luanvv.testcontainers.selenium;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.handler.ResourceHandler;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testcontainers.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

public class LocalServerWebDriverContainerTest {

    public BrowserWebDriverContainer<?> chrome = new BrowserWebDriverContainer<>()
            .withAccessToHost(true)
            .withCapabilities(new ChromeOptions());

    private int localPort;

    @BeforeEach
    public void setupLocalServer() throws Exception {
        // Set up a local Jetty HTTP server
        Server server = new Server();
        server.addConnector(new SocketConnector());
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setResourceBase("src/test/resources/server");
        server.addHandler(resourceHandler);
        server.start();

        // The server will have a random port assigned, so capture that
        localPort = server.getConnectors()[0].getLocalPort();
        chrome.start();
    }

    @AfterEach
    public void cleanUp() {
        chrome.stop();
    }

    @Test
    public void testConnection() {
        // getWebDriver {
        RemoteWebDriver driver = chrome.getWebDriver();
        // }

        // Construct a URL that the browser container can access
        // getPage {
        Testcontainers.exposeHostPorts(localPort);
        driver.get("http://host.testcontainers.internal:" + localPort);
        // }

        String headingText = driver.findElement(By.cssSelector("h1")).getText().trim();

        assertThat(headingText)
                .as("The hardcoded success message was found on a page fetched from a local server")
                .isEqualTo("It worked");
    }
}