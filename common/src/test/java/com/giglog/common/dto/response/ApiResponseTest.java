package com.giglog.common.dto.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("success() - data와 기본 메시지로 성공 응답을 생성한다")
    void success_withData_returnsSuccessResponse() {
        ApiResponse<String> response = ApiResponse.success("테스트 데이터");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("성공");
        assertThat(response.getData()).isEqualTo("테스트 데이터");
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    @DisplayName("success() - 커스텀 메시지로 성공 응답을 생성한다")
    void success_withCustomMessage_returnsSuccessResponse() {
        ApiResponse<String> response = ApiResponse.success("데이터", "생성 완료");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("생성 완료");
        assertThat(response.getData()).isEqualTo("데이터");
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    @DisplayName("fail() - errorCode와 메시지로 실패 응답을 생성한다")
    void fail_withErrorCodeAndMessage_returnsFailResponse() {
        ApiResponse<Void> response = ApiResponse.fail("NOT_FOUND", "리소스를 찾을 수 없습니다");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("리소스를 찾을 수 없습니다");
        assertThat(response.getData()).isNull();
        assertThat(response.getErrorCode()).isEqualTo("NOT_FOUND");
    }

    @Test
    @DisplayName("success() - Jackson 직렬화 시 null 필드(errorCode)는 제외된다")
    void success_serialization_excludesNullFields() throws Exception {
        ApiResponse<String> response = ApiResponse.success("데이터");

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("\"success\":true");
        assertThat(json).contains("\"message\":\"성공\"");
        assertThat(json).contains("\"data\":\"데이터\"");
        assertThat(json).doesNotContain("errorCode");
    }

    @Test
    @DisplayName("fail() - Jackson 직렬화 시 null 필드(data)는 제외된다")
    void fail_serialization_excludesNullFields() throws Exception {
        ApiResponse<Void> response = ApiResponse.fail("INVALID_INPUT", "잘못된 요청입니다");

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("\"success\":false");
        assertThat(json).contains("\"errorCode\":\"INVALID_INPUT\"");
        assertThat(json).doesNotContain("\"data\"");
    }

    @Test
    @DisplayName("success() - data가 null이어도 성공 응답을 생성한다")
    void success_withNullData_returnsSuccessResponse() {
        ApiResponse<Void> response = ApiResponse.success(null);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNull();
    }
}
