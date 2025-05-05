package org.almagestauth.controller.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.almagestauth.common.dto.CommonResponseDto;
import org.almagestauth.security.authentication.CustomUserDetails;
import org.almagestauth.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/a2/v1")
@RequiredArgsConstructor
public class HomeControllerV1 {
    private final MemberService memberService;
    @Operation(summary = "홈 화면용 사용자정보",
            responses = {
                    @ApiResponse(responseCode = "200", description = "인증 성공, 사용자 정보 반환"),
                    @ApiResponse(responseCode = "400", description = "인증 실패")
            })
    @GetMapping("/home")
    public ResponseEntity<?> home(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                CommonResponseDto.builder()
                        .status("200")
                        .message("")
                        .repCode("SUCCESS")
                        .repMsg("회원정보 조회 성공!")
                        .data(memberService.home(userDetails.getMember()))
                        .build());
    }
}
