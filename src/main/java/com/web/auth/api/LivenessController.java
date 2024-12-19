package com.web.auth.api;

import com.web.auth.exception.IgnoreException;
import com.web.auth.exception.RecordException;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Liveness Controller", description = "Health Check 컨트롤러")
public class LivenessController {

    @GetMapping("/health-check")
    public ResponseEntity<String> livenessProbe() {
        return ResponseEntity.ok("ok");
    }

    @GetMapping("/circuitbreaker-health-check")
    private String circuitBreakerTest(@RequestParam String param) throws InterruptedException {
        Thread.sleep(5000);
        if ("a".equals(param)) // a인 경우 RecordException 발생하는지 테스트
            throw new RecordException("record exception");
        else if ("b".equals(param)) // b인 경우 IgnoreException 발생하는지 테스트
            throw new IgnoreException("ignore exception");
        else if ("c".equals(param)) // 3초 이상 걸리는 경우도 실패로 간주
            Thread.sleep(4000);

        return param;
    }
}