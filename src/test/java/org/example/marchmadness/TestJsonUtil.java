package org.example.marchmadness;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class TestJsonUtil {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private TestJsonUtil() {
    }

    public static JsonNode parseJson(String json) throws Exception {
        return OBJECT_MAPPER.readTree(json);
    }
}
