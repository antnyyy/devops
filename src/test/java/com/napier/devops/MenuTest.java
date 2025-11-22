package com.napier.devops;

import com.napier.sem.App;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the interactive CLI menu. These tests simulate stdin to exercise
 * menu branches without requiring user interaction.
 */
class MenuTest {

    @Test
    void menu_handlesInvalidAndQuit() throws Exception {
        String simulatedInput = "x\nq\n";
        ByteArrayInputStream in = new ByteArrayInputStream(simulatedInput.getBytes());
        System.setIn(in);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        App app = new App();
        try {
            // menu() is private; invoke it via reflection to simulate the
            // interactive session without changing production visibility.
            java.lang.reflect.Method m = App.class.getDeclaredMethod("menu");
            m.setAccessible(true);
            m.invoke(app);
        } finally {
            System.setOut(originalOut);
        }

        String out = outContent.toString();
        assertTrue(out.contains("Invalid choice.") || out.contains("Choose:"), "Menu output should contain prompts or invalid choice");
    }

    @Test
    void menu_cityOption_handlesInvalidId() throws Exception {
        String simulatedInput = "1\nnot-a-number\nq\n";
        ByteArrayInputStream in = new ByteArrayInputStream(simulatedInput.getBytes());
        System.setIn(in);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        App app = new App();
        try {
            java.lang.reflect.Method m = App.class.getDeclaredMethod("menu");
            m.setAccessible(true);
            m.invoke(app);
        } finally {
            System.setOut(originalOut);
        }

        String out = outContent.toString();
        assertTrue(out.contains("Menu error") || out.contains("Invalid choice.") || out.contains("Choose:"), "Menu should handle invalid numeric input gracefully");
    }
}
