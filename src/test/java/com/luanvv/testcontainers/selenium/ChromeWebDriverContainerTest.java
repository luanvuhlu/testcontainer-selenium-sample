package com.luanvv.testcontainers.selenium;

import org.junit.jupiter.api.*;
import org.openqa.selenium.chrome.ChromeOptions;

import static com.luanvv.testcontainers.selenium.HelloWorldContainerExtension.NETWORK;

public class ChromeWebDriverContainerTest extends BaseWebDriverContainer {

//    @Rule
    public BrowserWebDriverContainer<?> chrome = new BrowserWebDriverContainer<>()
            .withCapabilities(new ChromeOptions())
            .withNetwork(NETWORK);

    @BeforeEach
    public void checkBrowserIsIndeedChrome() {
        chrome.start();
        assertBrowserNameIs(chrome, "chrome");
    }

    @AfterEach
    public void cleanUp() {
        chrome.stop();
    }


    @Test
    public void simpleExploreTest() {
        doSimpleExplore(chrome);
    }
}
