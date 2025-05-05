package org.almagestauth.controller.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.almagestauth.common.dto.CommonResponseDto;
import org.almagestauth.domain.entity.Member;
import org.almagestauth.dto.AuthRequestDto;
import org.almagestauth.dto.DataChangeRequestDto;
import org.almagestauth.dto.InfoResponseDto;
import org.almagestauth.security.authentication.CustomUserDetails;
import org.almagestauth.security.authentication.JwtProvider;
import org.almagestauth.service.EmailVerificationService;
import org.almagestauth.service.MemberService;
import org.almagestauth.service.OtpService;
import org.almagestauth.utils.RedisService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/a2/v1/member")
public class MemberControllerV1 {
    private final OtpService otpService;
    private final RedisService redisService;
    private final MemberService memberService;
    private final EmailVerificationService emailVerificationService;
    private final JwtProvider jwtProvider;

    @Operation(summary = "인증토큰 재발행 요청",
            responses = {
                    @ApiResponse(responseCode = "200", description = "인증토큰 재발행 성공"),
                    @ApiResponse(responseCode = "400", description = "인증 만료"),
            })
    @PostMapping("/renew")
    public ResponseEntity<CommonResponseDto<?>> renewToken(@AuthenticationPrincipal CustomUserDetails userDetails){
        InfoResponseDto infoResponseDto = new InfoResponseDto();
        infoResponseDto.setMemberId(userDetails.getMember().getId());
        return ResponseEntity.ok(CommonResponseDto.builder()
                .status("200")
                .message("토큰 재발행 성공")
                .repCode("SUCCESS")
                .repMsg("토큰이 성공적으로 재발행되었습니다")
                .data(infoResponseDto)
                .build());
    }

    @GetMapping("/info")
    public ResponseEntity<CommonResponseDto<?>> getMemberInfo(@AuthenticationPrincipal CustomUserDetails userDetails){
        Member member = userDetails.getMember();
        InfoResponseDto responseDto = new InfoResponseDto();
        responseDto.setEmail(member.getEmail());
        responseDto.setName(member.getName());
        responseDto.setGender(member.getGender());
        responseDto.setTel(member.getTel());
        responseDto.setBirthDate(member.getBirthDate());
        responseDto.setCountry(member.getCountry());
        responseDto.setLastUpdate(member.getLastUpdate());

        return ResponseEntity.ok(CommonResponseDto.builder()
                .status("200")
                .message("사용자 정보 조회 성공")
                .repCode("SUCCESS")
                .repMsg("사용자 정보를 성공적으로 조회했습니다")
                .data(responseDto)
                .build());
    }

    @Operation(summary = "비밀번호 초기화",
            responses = {
                    @ApiResponse(responseCode = "200", description = "비밀번호 초기화 성공"),
                    @ApiResponse(responseCode = "500", description = "서버 에러, 비밀번호 초기화 실패"),
            })
    @PostMapping("/pw")
    public ResponseEntity<CommonResponseDto<?>> initializePassword(@AuthenticationPrincipal CustomUserDetails userDetails) {
        memberService.initializePassword(userDetails.getMember());
        System.out.println("on reset success");
        return ResponseEntity.ok(CommonResponseDto.builder()
                .status("200")
                .message("비밀번호 초기화 성공")
                .repCode("SUCCESS")
                .repMsg("비밀번호가 성공적으로 초기화되었습니다")
                .build());
    }

    @Operation(summary = "비밀번호 변경",
            responses = {
                    @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
                    @ApiResponse(responseCode = "400", description = "비밀번호 변경 실패"),
            })
    @PutMapping("/pw")
    public ResponseEntity<CommonResponseDto<?>> changePassword(@Valid @RequestBody DataChangeRequestDto requestDto,
                                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        System.out.println("on change");
        memberService.changePassword(requestDto, userDetails.getMember());
        System.out.println("on change success");
        return ResponseEntity.ok(CommonResponseDto.builder()
                .status("200")
                .message("비밀번호 변경 성공")
                .repCode("SUCCESS")
                .repMsg("비밀번호가 성공적으로 변경되었습니다")
                .build());
    }

    @Operation(summary = "이메일 인증 발송",
            responses = {
                    @ApiResponse(responseCode = "200", description = "코드 전송 성공"),
                    @ApiResponse(responseCode = "500", description = "서버 에러, 코드 전송 실패"),
            })
    @PostMapping("/email")
    public ResponseEntity<CommonResponseDto<?>> sendEmailVerification(@AuthenticationPrincipal CustomUserDetails userDetails) {
        String code = otpService.emailOtpGenerate(userDetails.getMember());
        emailVerificationService.send(code, userDetails.getMember());
        return ResponseEntity.ok(CommonResponseDto.builder()
                .status("200")
                .message("코드전송 성공")
                .repCode("SUCCESS")
                .repMsg("인증 코드가 이메일로 전송되었습니다")
                .build());
    }

    @Operation(summary = "이메일 인증 확인, 계정 활성화",
            responses = {
                    @ApiResponse(responseCode = "200", description = "코드 일치, 계정 활성화 성공"),
                    @ApiResponse(responseCode = "400", description = "코드 불일치, 계정 확성화 실패"),
            })
    @PutMapping("/email")
    public ResponseEntity<CommonResponseDto<?>> checkEmailVerification(
            @RequestBody AuthRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        otpService.check(requestDto, userDetails.getMember());
        memberService.enableUser(userDetails.getMember());
        return ResponseEntity.ok(CommonResponseDto.builder()
                .status("200")
                .message("사용자 승인 성공")
                .repCode("SUCCESS")
                .repMsg("사용자가 성공적으로 승인되었습니다")
                .build());
    }

    @Operation(summary = "이메일 변경 요청, 기존 메일주소로 코드전송",
            responses = {
                    @ApiResponse(responseCode = "200", description = "코드 전송 성공"),
                    @ApiResponse(responseCode = "500", description = "서버 에러, 코드 전송 실패"),
            })
    @PostMapping("/email/new")
    public ResponseEntity<CommonResponseDto<?>> changeEmailRequest(@AuthenticationPrincipal CustomUserDetails userDetails) {
        String code = otpService.emailOtpGenerate(userDetails.getMember());
        emailVerificationService.send(code, userDetails.getMember());

        return ResponseEntity.ok(CommonResponseDto.builder()
                .status("200")
                .message("코드전송 성공")
                .repCode("SUCCESS")
                .repMsg("인증 코드가 이메일로 전송되었습니다")
                .build());
    }

    @Operation(summary = "코드확인 후 새 이메일로 코드 전송",
            responses = {
                    @ApiResponse(responseCode = "200", description = "코드 일치, 새 이메일에 코드 전송 성공"),
                    @ApiResponse(responseCode = "400", description = "코드 불일치"),
                    @ApiResponse(responseCode = "500", description = "서버 에러, 코드 전송 실패"),
            })
    @PostMapping("/email/new/code")
    public ResponseEntity<CommonResponseDto<?>> newEmailCheck(
            @RequestBody DataChangeRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        otpService.check(requestDto, userDetails.getMember());
        String code = otpService.emailOtpGenerate(userDetails.getMember());
        emailVerificationService.sendNew(code, requestDto.getNewEmail());

        return ResponseEntity.ok(CommonResponseDto.builder()
                .status("200")
                .message("코드전송 성공")
                .repCode("SUCCESS")
                .repMsg("인증 코드가 새 이메일로 전송되었습니다")
                .build());
    }

    @Operation(summary = "코드확인 후 새 이메일로 변경",
            responses = {
                    @ApiResponse(responseCode = "200", description = "코드 일치, 이메일 변경 성공"),
                    @ApiResponse(responseCode = "400", description = "코드 불일치, 이메일 변경 실패"),
                    @ApiResponse(responseCode = "500", description = "서버 에러, 이메일 저장 실패"),
            })
    @PutMapping("/email/new/code")
    public ResponseEntity<CommonResponseDto<?>> changeEmailCheck(
            @RequestBody DataChangeRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        otpService.check(requestDto, userDetails.getMember());
        memberService.changeEmail(requestDto.getNewEmail(), userDetails.getMember());
        return ResponseEntity.ok(CommonResponseDto.builder()
                .status("200")
                .message("이메일 변경 성공")
                .repCode("SUCCESS")
                .repMsg("이메일이 성공적으로 변경되었습니다")
                .build());
    }

    @Operation(
            summary = "사용자 최신화 정보 요청",
            responses = {
                    @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공"),
                    @ApiResponse(responseCode = "400", description = "사용자 정보 없음")
            })
    @PostMapping("/info")
    public ResponseEntity<CommonResponseDto<?>> changeInfo(
            @RequestBody DataChangeRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws Exception {
        InfoResponseDto responseDto = memberService.changeInfo(requestDto, userDetails.getMember());

        return ResponseEntity.ok(CommonResponseDto.builder()
                .status("200")
                .message("정보 변경 성공")
                .repCode("SUCCESS")
                .repMsg("사용자 정보가 성공적으로 변경되었습니다")
                .data(responseDto)
                .build());
    }

    @Operation(
            summary = "사용자 최신화 정보 요청",
            responses = {
                    @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공"),
                    @ApiResponse(responseCode = "400", description = "사용자 정보 없음")
            })
    @PostMapping("/info/latest")
    public ResponseEntity<CommonResponseDto<?>> update(@AuthenticationPrincipal CustomUserDetails userDetails) throws Exception {
        InfoResponseDto responseDto = memberService.latestInfo(userDetails.getMember());

        return ResponseEntity.ok(CommonResponseDto.builder()
                .status("200")
                .message("정보 조회 성공")
                .repCode("SUCCESS")
                .repMsg("최신 사용자 정보를 성공적으로 조회했습니다")
                .data(responseDto)
                .build());
    }

    @Operation(summary = "가입용 정보 요청",
            responses = {
                    @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공"),
                    @ApiResponse(responseCode = "400", description = "사용자 정보 없음")
            })
    @PostMapping("/info/register")
    public ResponseEntity<CommonResponseDto<?>> socialRegister(@AuthenticationPrincipal CustomUserDetails userDetails) throws Exception {
        return ResponseEntity.ok(CommonResponseDto.builder()
                .status("200")
                .message("데이터 조회 성공")
                .repCode("SUCCESS")
                .repMsg("가입용 사용자 정보를 성공적으로 조회했습니다")
                .data(memberService.socialRegister(userDetails.getMember()))
                .build());
    }

    @Operation(summary = "로그아웃",
            responses = {
                    @ApiResponse(responseCode = "200", description = "토큰 삭제, 로그아웃 성공"),
                    @ApiResponse(responseCode = "500", description = "토큰 삭제 실패, 로그아웃 실패"),
            })
    @PostMapping("/logout")
    public ResponseEntity<CommonResponseDto<?>> logout(HttpServletResponse response,
                                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        ResponseCookie cookie = ResponseCookie.from("access_token", null)
                .secure(true)
                .httpOnly(true)
                .path("/")
                .sameSite("None")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        cookie = ResponseCookie.from("refresh_token", null)
                .secure(true)
                .httpOnly(true)
                .path("/")
                .sameSite("None")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        redisService.resetStatus(userDetails.getMember().getId());

        return ResponseEntity.ok(CommonResponseDto.builder()
                .status("200")
                .message("로그아웃 성공")
                .repCode("SUCCESS")
                .repMsg("성공적으로 로그아웃되었습니다")
                .build());
    }

    @Operation(summary = "회원 탈퇴",
            responses = {
                    @ApiResponse(responseCode = "200", description = "회원 탈퇴 성공"),
                    @ApiResponse(responseCode = "500", description = "회원 탈퇴 실패"),
            })
    @DeleteMapping("/leave")
    public ResponseEntity<CommonResponseDto<?>> leave(HttpServletResponse response, @AuthenticationPrincipal CustomUserDetails userDetails) {
        memberService.leave(userDetails.getMember());

        ResponseCookie cookie = ResponseCookie.from("access_token", null)
                .secure(true)
                .httpOnly(true)
                .path("/")
                .sameSite("None")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        cookie = ResponseCookie.from("refresh_token", null)
                .secure(true)
                .httpOnly(true)
                .path("/")
                .sameSite("None")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        redisService.resetStatus(userDetails.getMember().getId());

        return ResponseEntity.ok(CommonResponseDto.builder()
                .status("200")
                .message("Good bye")
                .repCode("SUCCESS")
                .repMsg("회원 탈퇴가 성공적으로 완료되었습니다")
                .build());
    }
}
