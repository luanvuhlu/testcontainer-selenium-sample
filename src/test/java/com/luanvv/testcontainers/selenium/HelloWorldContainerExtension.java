package com.luanvv.testcontainers.selenium;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.utility.DockerImageName;

public class HelloWorldContainerExtension implements AfterAllCallback, BeforeAllCallback {


    public static Network NETWORK = Network.newNetwork();
    private static GenericContainer<?> HELLO_WORLD = new GenericContainer<>(
            DockerImageName.parse("testcontainers/helloworld:1.1.0")
    )
            .withNetwork(NETWORK)
            .withNetworkAliases("helloworld")
            .withExposedPorts(8080, 8081)
            .waitingFor(new HttpWaitStrategy());

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        System.out.println("HELLO_WORLD container is starting....");
        HELLO_WORLD.start();
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        System.out.println("HELLO_WORLD container is stopping....");
        HELLO_WORLD.stop();
    }


}
