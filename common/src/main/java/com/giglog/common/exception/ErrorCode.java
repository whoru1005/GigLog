package com.giglog.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Common
    INVALID_INPUT_VALUE(400, "C001", "잘못된 입력값입니다."),
    INTERNAL_SERVER_ERROR(500, "C002", "서버 내부 오류입니다."),
    NOT_FOUND(404, "C003", "리소스를 찾을 수 없습니다."),

    // Auth
    UNAUTHORIZED(401, "A001", "인증되지 않은 사용자입니다."),
    FORBIDDEN(403, "A002", "권한이 없습니다."),
    INVALID_TOKEN(401, "A003", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(401, "A004", "만료된 토큰입니다."),

    // User
    USER_NOT_FOUND(404, "U001", "사용자를 찾을 수 없습니다."),

    // Performance
    PERFORMANCE_NOT_FOUND(404, "P001", "공연 정보를 찾을 수 없습니다.");

    private final int status;
    private final String code;
    private final String message;
}
