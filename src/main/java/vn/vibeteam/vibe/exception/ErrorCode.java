package vn.vibeteam.vibe.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    USER_NOT_EXISTED(404, "User not existed", HttpStatus.NOT_FOUND),
    USERNAME_ALREADY_EXISTS(400, "Username already exists", HttpStatus.BAD_REQUEST),

    INVALID_CREDENTIALS(401, "Invalid credentials", HttpStatus.UNAUTHORIZED),
    INTERNAL_SERVER_ERROR(500, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    ;

    private int code;
    private String message;
    private HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

}
