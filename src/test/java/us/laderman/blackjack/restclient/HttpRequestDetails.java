package us.laderman.blackjack.restclient;

import java.util.HashMap;
import java.util.Map;

/**
 * Captures full HTTP request details for logging and display.
 */
public class HttpRequestDetails {
    private final String method;
    private final String url;
    private final String body;
    private final Map<String, String> headers;
    private final long timestamp;

    public HttpRequestDetails(String method, String url, String body, Map<String, String> headers) {
        this.method = method;
        this.url = url;
        this.body = body;
        this.headers = new HashMap<>(headers);
        this.timestamp = System.currentTimeMillis();
    }

    public HttpRequestDetails(String method, String url) {
        this(method, url, null, new HashMap<>());
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return new HashMap<>(headers);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getFullRequestString() {
        StringBuilder sb = new StringBuilder();
        sb.append(method).append(" ").append(url).append("\n");

        if (!headers.isEmpty()) {
            sb.append("Headers:\n");
            headers.forEach((key, value) -> sb.append("  ").append(key).append(": ").append(value).append("\n"));
        }

        if (body != null && !body.isEmpty()) {
            sb.append("Body:\n");
            sb.append("  ").append(body);
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return getFullRequestString();
    }
}
