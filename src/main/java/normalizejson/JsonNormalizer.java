package main.java.normalizejson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import one.util.streamex.EntryStream;

import java.util.Map;
import java.util.regex.Pattern;

public class JsonNormalizer {
    private static final Pattern camelCasePattern = Pattern.compile("^([a-z]+)([A-Z][a-z]*)*$");
    private static final Pattern pascalCasePattern = Pattern.compile("^([A-Z][a-z]*)+$");
    private static final Pattern snakeCasePattern = Pattern.compile("^([a-z]+)(_[a-z]+)+$");
    private static final Pattern constCasePattern = Pattern.compile("^([A-Z]+)(_[A-Z]+)*$");

    private final ObjectMapper mapper;

    public JsonNormalizer() {
        mapper = new ObjectMapper();
        mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    @SuppressWarnings("unchecked")
    public String normalize(String json) throws JsonProcessingException {
        return mapper.writeValueAsString(normalize(mapper.readValue(json, Map.class)));
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> normalize(Map<String, Object> json) {
        return EntryStream.of(json)
                .mapKeys(this::normalizeKey)
                .mapValues(v -> v instanceof Map ? normalize((Map<String, Object>) v) : v)
                .toMap();
    }

    private String normalizeKey(String key) {
        if (camelCasePattern.asMatchPredicate().test(key)) {
            return key;
        }
        if (constCasePattern.asMatchPredicate().test(key)) {
            return normalizeKey(key.toLowerCase());
        }
        if (pascalCasePattern.asMatchPredicate().test(key)) {
            return key.substring(0, 1).toLowerCase() + key.substring(1);
        }
        if (snakeCasePattern.asMatchPredicate().test(key)) {
            int index = key.indexOf('_');
            while (index >= 0) {
                key = key.substring(0, index)
                        + key.substring(index + 1, index + 2).toUpperCase()
                        + key.substring(index + 2);
                index = key.indexOf('_');
            }
        }
        return key;
    }
}
