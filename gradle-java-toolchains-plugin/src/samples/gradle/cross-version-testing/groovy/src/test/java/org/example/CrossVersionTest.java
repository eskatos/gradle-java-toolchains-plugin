package org.example;

import org.junit.*;

public class CrossVersionTest {

    @Test
    public void test() {
        System.out.println("test:" + System.getProperty("java.version"));
    }
}
