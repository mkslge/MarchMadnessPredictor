package org.example.marchmadness;

import org.example.marchmadness.generators.BracketGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ApplicationContextTests {

    private static final int DEFAULT_TEST_YEAR = 2024;

    @Test
    @DisplayName("Context loads and bracket generator returns a champion")
    void contextLoads() {
        BracketGenerator generator = new BracketGenerator(DEFAULT_TEST_YEAR);
        assertNotNull(generator.getBracket());
        assertNotNull(generator.getBracket().getChampion());
    }
}
