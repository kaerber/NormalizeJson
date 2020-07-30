package main.test.nomalizejson;


import com.fasterxml.jackson.core.JsonProcessingException;
import main.java.normalizejson.JsonNormalizer;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonNormalizerTest {
    private static final String expected = "{'test':{'camelCase':40,'inner':{'const':'still_working','constCase':'true','snakeCase':false},'pascalCase':50}}".replace('\'', '"');
    private static final String source = "{'test':{'camelCase':40, 'PascalCase':50, 'Inner':{'CONST_CASE':'true', 'snake_case':false, 'CONST':'still_working'}}}".replace('\'', '"');

    private JsonNormalizer normalizer;

    @Before
    public void setUp() {
        normalizer = new JsonNormalizer();
    }

    @Test
    public void camelCaseTest() {
        var normalized = normalizer.normalize(testJson("camelCase"));
        assertHasRequiredKey(normalized, "camelCase");
    }

    @Test
    public void pascalCaseTest() {
        var normalized = normalizer.normalize(testJson("PascalCase"));
        assertHasRequiredKey(normalized, "pascalCase");
    }

    @Test
    public void snakeCaseTest() {
        var normalized = normalizer.normalize(testJson("snake_case"));
        assertHasRequiredKey(normalized, "snakeCase");
    }

    @Test
    public void snakeCaseWitOneLastLetterTest() {
        var normalized = normalizer.normalize(testJson("static_x"));
        assertHasRequiredKey(normalized, "staticX");
    }

    @Test
    public void constCaseTest() {
        var normalized = normalizer.normalize(testJson("CONST_CASE"));
        assertHasRequiredKey(normalized, "constCase");
    }

    @Test
    public void constShortCaseTest() {
        var normalized = normalizer.normalize(testJson("CONST"));
        assertHasRequiredKey(normalized, "const");
    }

    @Test
    public void acceptanceTest() throws JsonProcessingException {
        assertThat(normalizer.normalize(source)).isEqualTo(expected);
    }

    private static Map<String, Object> testJson(String key) {
        return Map.of("Test", Map.of(key, 50));
    }

    private static void assertHasRequiredKey(Map<String, Object> json, String key) {
        @SuppressWarnings("unchecked")
        var inner = (Map<String, Object>) json.get("test");
        assertThat(inner.keySet()).contains(key);
        assertThat(inner.get(key)).isEqualTo(50);
    }
}
