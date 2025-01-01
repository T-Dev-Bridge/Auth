package com.bridge.auth.client;

import com.bridge.auth.error.FeignCircuitBreakerErrorDecoder;
import com.bridge.auth.service.admin.ManagerDto;
import org.bridge.base.api.CommonResponseDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "admin-service", configuration = FeignCircuitBreakerErrorDecoder.class, url = "${admin-service-url}")
@Qualifier("AdminHttpClient")
public interface AdminClient extends HttpClient {

    @GetMapping("/api/admin/manager/profile/{managerId}")
    CommonResponseDto<ManagerDto> getManagerById(@PathVariable("managerId") String managerId);

    @GetMapping("/api/no-auth/manager/{managerId}")
    CommonResponseDto<String> getManagerIdById(@PathVariable("managerId") String managerId);
}
