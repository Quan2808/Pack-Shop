package com.packshop.api;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Container
    public static MSSQLServerContainer<?> sqlServerContainer = new MSSQLServerContainer<>(
            "mcr.microsoft.com/mssql/server:2019-latest")
            .withDatabaseName("PackShop2")
            .withUsername("sa")
            .withPassword("YourStrong@Passw0rd")
            .acceptLicense();

    static {
        sqlServerContainer.start();
    }
}