package gemini.penguinrunner.io;

import java.util.*;

/**
 * Lightweight, zero-dependency, high-performance JSON parser and serializer.
 */
public class SimpleJson {

    public sealed interface JsonValue permits JsonObject, JsonArray, JsonString, JsonNumber, JsonBoolean, JsonNull {
        default JsonObject asObject() { return (JsonObject) this; }
        default JsonArray asArray() { return (JsonArray) this; }
        default String asString() { return ((JsonString) this).value(); }
        default double asDouble() { return ((JsonNumber) this).value(); }
        default int asInt() { return (int) asDouble(); }
        default boolean asBoolean() { return ((JsonBoolean) this).value(); }
        default boolean isNull() { return this instanceof JsonNull; }
    }

    public record JsonObject(Map<String, JsonValue> map) implements JsonValue {
        public JsonObject() { this(new LinkedHashMap<>()); }
        public JsonValue get(String key) { return map.get(key); }
        public boolean has(String key) { return map.containsKey(key); }
        public String getString(String key, String def) {
            JsonValue val = get(key);
            return (val instanceof JsonString s) ? s.value() : def;
        }
        public int getInt(String key, int def) {
            JsonValue val = get(key);
            return (val instanceof JsonNumber n) ? (int) n.value() : def;
        }
        public double getDouble(String key, double def) {
            JsonValue val = get(key);
            return (val instanceof JsonNumber n) ? n.value() : def;
        }
        public boolean getBoolean(String key, boolean def) {
            JsonValue val = get(key);
            return (val instanceof JsonBoolean b) ? b.value() : def;
        }
        public JsonArray getArray(String key) {
            JsonValue val = get(key);
            return (val instanceof JsonArray a) ? a : null;
        }
        public JsonObject getObject(String key) {
            JsonValue val = get(key);
            return (val instanceof JsonObject o) ? o : null;
        }
        public void put(String key, JsonValue val) { map.put(key, val); }
        public void put(String key, String val) { map.put(key, new JsonString(val)); }
        public void put(String key, double val) { map.put(key, new JsonNumber(val)); }
        public void put(String key, boolean val) { map.put(key, new JsonBoolean(val)); }
    }

    public record JsonArray(List<JsonValue> list) implements JsonValue, Iterable<JsonValue> {
        public JsonArray() { this(new ArrayList<>()); }
        public JsonValue get(int index) { return list.get(index); }
        public int size() { return list.size(); }
        public void add(JsonValue val) { list.add(val); }
        public void add(String val) { list.add(new JsonString(val)); }
        public void add(double val) { list.add(new JsonNumber(val)); }
        public void add(boolean val) { list.add(new JsonBoolean(val)); }
        @Override public Iterator<JsonValue> iterator() { return list.iterator(); }
    }

    public record JsonString(String value) implements JsonValue {}
    public record JsonNumber(double value) implements JsonValue {}
    public record JsonBoolean(boolean value) implements JsonValue {}
    public record JsonNull() implements JsonValue {}

    public static JsonValue parse(String json) {
        if (json == null) return new JsonNull();
        return new Parser(json.trim()).parseValue();
    }

    private static class Parser {
        private final String src;
        private int pos = 0;

        Parser(String src) { this.src = src; }

        private void skipWhitespace() {
            while (pos < src.length()) {
                char c = src.charAt(pos);
                if (c == ' ' || c == '\t' || c == '\n' || c == '\r') pos++;
                else if (c == '/' && pos + 1 < src.length() && src.charAt(pos + 1) == '/') {
                    while (pos < src.length() && src.charAt(pos) != '\n') pos++;
                } else break;
            }
        }

        private char peek() { skipWhitespace(); return pos < src.length() ? src.charAt(pos) : '\0'; }
        private char next() { skipWhitespace(); return pos < src.length() ? src.charAt(pos++) : '\0'; }

        JsonValue parseValue() {
            char c = peek();
            if (c == '{') return parseObject();
            if (c == '[') return parseArray();
            if (c == '"') return parseString();
            if (c == 't' || c == 'f') return parseBoolean();
            if (c == 'n') return parseNull();
            if (c == '-' || Character.isDigit(c)) return parseNumber();
            throw new IllegalArgumentException("Unexpected char at pos " + pos + ": '" + c + "'");
        }

        private JsonObject parseObject() {
            next(); // '{'
            JsonObject obj = new JsonObject();
            if (peek() == '}') { next(); return obj; }
            while (true) {
                if (peek() != '"') throw new IllegalArgumentException("Expected string key at " + pos);
                String key = parseString().value();
                if (next() != ':') throw new IllegalArgumentException("Expected ':' at " + pos);
                JsonValue val = parseValue();
                obj.put(key, val);
                char after = next();
                if (after == '}') break;
                if (after != ',') throw new IllegalArgumentException("Expected ',' or '}' at " + pos);
            }
            return obj;
        }

        private JsonArray parseArray() {
            next(); // '['
            JsonArray arr = new JsonArray();
            if (peek() == ']') { next(); return arr; }
            while (true) {
                arr.add(parseValue());
                char after = next();
                if (after == ']') break;
                if (after != ',') throw new IllegalArgumentException("Expected ',' or ']' at " + pos);
            }
            return arr;
        }

        private JsonString parseString() {
            next(); // '"'
            StringBuilder sb = new StringBuilder();
            while (pos < src.length()) {
                char c = src.charAt(pos++);
                if (c == '"') return new JsonString(sb.toString());
                if (c == '\\') {
                    if (pos >= src.length()) break;
                    char esc = src.charAt(pos++);
                    switch (esc) {
                        case '"' -> sb.append('"');
                        case '\\' -> sb.append('\\');
                        case '/' -> sb.append('/');
                        case 'b' -> sb.append('\b');
                        case 'f' -> sb.append('\f');
                        case 'n' -> sb.append('\n');
                        case 'r' -> sb.append('\r');
                        case 't' -> sb.append('\t');
                        case 'u' -> {
                            if (pos + 4 <= src.length()) {
                                String hex = src.substring(pos, pos + 4);
                                sb.append((char) Integer.parseInt(hex, 16));
                                pos += 4;
                            }
                        }
                        default -> sb.append(esc);
                    }
                } else {
                    sb.append(c);
                }
            }
            return new JsonString(sb.toString());
        }

        private JsonBoolean parseBoolean() {
            if (src.startsWith("true", pos)) { pos += 4; return new JsonBoolean(true); }
            if (src.startsWith("false", pos)) { pos += 5; return new JsonBoolean(false); }
            throw new IllegalArgumentException("Invalid boolean at " + pos);
        }

        private JsonNull parseNull() {
            if (src.startsWith("null", pos)) { pos += 4; return new JsonNull(); }
            throw new IllegalArgumentException("Invalid null at " + pos);
        }

        private JsonNumber parseNumber() {
            int start = pos;
            if (src.charAt(pos) == '-') pos++;
            while (pos < src.length() && (Character.isDigit(src.charAt(pos)) || src.charAt(pos) == '.' || src.charAt(pos) == 'e' || src.charAt(pos) == 'E' || src.charAt(pos) == '+' || src.charAt(pos) == '-')) {
                pos++;
            }
            return new JsonNumber(Double.parseDouble(src.substring(start, pos)));
        }
    }

    public static String stringify(JsonValue val) {
        StringBuilder sb = new StringBuilder();
        write(val, sb, 0);
        return sb.toString();
    }

    private static void write(JsonValue val, StringBuilder sb, int indent) {
        if (val instanceof JsonObject obj) {
            sb.append("{\n");
            int i = 0;
            for (Map.Entry<String, JsonValue> e : obj.map.entrySet()) {
                indent(sb, indent + 2);
                sb.append('"').append(escape(e.getKey())).append("\": ");
                write(e.getValue(), sb, indent + 2);
                if (++i < obj.map.size()) sb.append(",");
                sb.append("\n");
            }
            indent(sb, indent);
            sb.append("}");
        } else if (val instanceof JsonArray arr) {
            sb.append("[\n");
            for (int i = 0; i < arr.size(); i++) {
                indent(sb, indent + 2);
                write(arr.get(i), sb, indent + 2);
                if (i + 1 < arr.size()) sb.append(",");
                sb.append("\n");
            }
            indent(sb, indent);
            sb.append("]");
        } else if (val instanceof JsonString s) {
            sb.append('"').append(escape(s.value())).append('"');
        } else if (val instanceof JsonNumber n) {
            double d = n.value();
            if (d == (long) d) sb.append((long) d);
            else sb.append(d);
        } else if (val instanceof JsonBoolean b) {
            sb.append(b.value());
        } else {
            sb.append("null");
        }
    }

    private static void indent(StringBuilder sb, int count) {
        sb.append(" ".repeat(Math.max(0, count)));
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}

