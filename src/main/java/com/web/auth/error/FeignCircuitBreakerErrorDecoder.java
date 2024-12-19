package com.web.auth.error;

import com.web.auth.exception.RecordException;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class FeignCircuitBreakerErrorDecoder implements ErrorDecoder {

    Environment env;

    @Autowired
    public FeignCircuitBreakerErrorDecoder(Environment env) {
        this.env = env;
    }

    @Override
    public Exception decode(String s, Response response) {
        String errorMessage = "알 수 없는 오류";

        try {
            if (response.body() != null) {
                errorMessage = Util.toString(response.body().asReader());
            }
        } catch (IOException e) {
            errorMessage = "응답 본문 읽기 오류";
        }

        HttpStatus status = HttpStatus.valueOf(response.status());
        switch (status) {
            case BAD_REQUEST:
                return new RecordException("잘못된 요청 오류: " + errorMessage);
            case NOT_FOUND:
                return new RecordException("리소스를 찾을 수 없음: " + errorMessage);
            case INTERNAL_SERVER_ERROR:
                return new RecordException("내부 서버 오류: " + errorMessage);
            default:
                return new Exception("일반 오류: " + errorMessage);
        }
    }
}
