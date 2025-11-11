package com.napier.devops;

import com.napier.sem.App;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class AppTest
{
    static App app;

    @BeforeAll
    static void init()
    {
        app = new App();
    }

    @Test
    void dummyTest()
    {
        // Just a placeholder to confirm tests run
        System.out.println("App initialized successfully");
    }
}
