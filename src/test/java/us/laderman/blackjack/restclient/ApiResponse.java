package us.laderman.blackjack.restclient;

/**
 * Wrapper for API responses containing status code, data, and error
 * information.
 */
public class ApiResponse<T> {
    private final int statusCode;
    private final T data;
    private final String errorMessage;
    private final boolean success;
    private final HttpRequestDetails requestDetails;

    public ApiResponse(int statusCode, T data, String errorMessage, boolean success) {
        this(statusCode, data, errorMessage, success, null);
    }

    public ApiResponse(int statusCode, T data, String errorMessage, boolean success,
            HttpRequestDetails requestDetails) {
        this.statusCode = statusCode;
        this.data = data;
        this.errorMessage = errorMessage;
        this.success = success;
        this.requestDetails = requestDetails;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public T getData() {
        return data;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public HttpRequestDetails getRequestDetails() {
        return requestDetails;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "statusCode=" + statusCode +
                ", data=" + data +
                ", errorMessage='" + errorMessage + '\'' +
                ", success=" + success +
                '}';
    }
}
