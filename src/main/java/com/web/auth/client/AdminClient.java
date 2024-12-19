package com.web.auth.client;

import com.web.auth.error.FeignCircuitBreakerErrorDecoder;
import com.web.auth.service.Core.ManagerDto;
import org.base.base.api.ApiResponseDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "admin-service", configuration = FeignCircuitBreakerErrorDecoder.class, url = "${admin-service-url}")
@Qualifier("AdminHttpClient")
public interface AdminClient extends HttpClient {

    @GetMapping("/api/manager/profile/{managerId}")
    ApiResponseDto<ManagerDto> getManagerById(@PathVariable("managerId") String managerId);

    @GetMapping("/api/no-auth/manager/{managerId}")
    ApiResponseDto<String> getManagerIdById(@PathVariable("managerId") String managerId);
}
