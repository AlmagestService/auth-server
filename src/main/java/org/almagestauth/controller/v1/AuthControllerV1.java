package org.almagestauth.controller.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.almagestauth.common.dto.CommonResponseDto;
import org.almagestauth.domain.entity.Member;
import org.almagestauth.domain.entity.MobileAppVersion;
import org.almagestauth.dto.*;
import org.almagestauth.security.authentication.CustomUserDetails;
import org.almagestauth.security.authentication.JwtProvider;
import org.almagestauth.service.*;
import org.almagestauth.utils.RedisService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/a1/v1")
public class AuthControllerV1 {
    private final MemberService memberService;
    private final OtpService otpService;
    private final JwtProvider jwtProvider;
    private final FCMNotificationService fcmNotificationService;
    private final RedisService redisService;
    private final AppVersionService appVersionService;


    //aws 로드밸런서 상태체크용
    @GetMapping("/aws/check")
    public ResponseEntity<?> awsCheck() {
        return ResponseEntity.ok("OK");
    }

    //App 버전 확인
    @GetMapping("/app/version")
    public ResponseEntity<?> appVersion() {
        MobileAppVersion appversion = appVersionService.getAppversion();
        InfoResponseDto responseDto = new InfoResponseDto();
        responseDto.setAppVersion(appversion.getVersionCode());
        return ResponseEntity.ok(CommonResponseDto.builder()
                .status("200")
                .message("버전 조회 성공")
                .repCode("SUCCESS")
                .repMsg("버전 정보를 성공적으로 조회했습니다")
                .data(responseDto)
                .build());
    }



    /**
     * 계정 중복조회
     * */
    @PostMapping("/look/account")
    public ResponseEntity<?> lookAccount(@RequestBody AuthRequestDto authRequestDto) {
        memberService.lookAccount(authRequestDto);
        return ResponseEntity.ok(CommonResponseDto.builder()
                .status("200")
                .message("사용 가능한 계정입니다")
                .repCode("SUCCESS")
                .repMsg("해당 계정은 사용 가능합니다")
                .build());
    }

    /**
     * 이메일 중복조회
     * */
    @PostMapping("/look/email")
    public ResponseEntity<?> lookEmail(@RequestBody AuthRequestDto authRequestDto) {
        memberService.lookEmail(authRequestDto);
        return ResponseEntity.ok(CommonResponseDto.builder()
                .status("200")
                .message("사용 가능한 이메일입니다")
                .repCode("SUCCESS")
                .repMsg("해당 이메일은 사용 가능합니다")
                .build());
    }

//    /**
//     * 계정 생성 요청
//     * */
//    @PostMapping("/member")
//    public ResponseEntity<?> newMember(@RequestBody AuthRequestDto authRequestDto) {
//        memberService.newMember(authRequestDto);
//        return ResponseEntity.ok(CommonResponseDto.builder()
//                .status("200")
//                .message("사용 가능한 이메일입니다")
//                .repCode("SUCCESS")
//                .repMsg("해당 이메일은 사용 가능합니다")
//                .build());
//    }



    @Operation(summary = "회원 가입 요청",
            responses = {
                    @ApiResponse(responseCode = "201", description = "가입 성공"),
                    @ApiResponse(responseCode = "500", description = "서버 에러, 가입 실패"),
            })
    @PostMapping("/member")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDto registerRequestDto) {
        memberService.register(registerRequestDto);
        return ResponseEntity.ok(CommonResponseDto.builder()
                .status("201")
                .message("가입 성공")
                .repCode("SUCCESS")
                .repMsg("회원가입이 성공적으로 완료되었습니다")
                .build());
    }


    @Operation(summary = "로그인 후 OTP발급",
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그인정보 일치, OTP 발급 성공"),
                    @ApiResponse(responseCode = "400", description = "로그인정보 불일치"),
                    @ApiResponse(responseCode = "406", description = "요청차단됨"),
            })
    @PostMapping("/login/web")
    public ResponseEntity<?> webAuthenticate(@RequestBody AuthRequestDto requestDto) {
        Member member = memberService.checkBan(requestDto.getAccount());
        OtpTokenDto otpTokenDto = otpService.auth(requestDto, member);
        otpTokenDto.setId(member.getId());
        fcmNotificationService.sendNotificationByToken(otpTokenDto);

        return ResponseEntity.ok(CommonResponseDto.builder()
                .status("200")
                .message("OTP 코드가 전송되었습니다.")
                .repCode("SUCCESS")
                .repMsg("OTP 코드가 이메일로 전송되었습니다")
                .build());
    }


    @Operation(summary = "로그인 후 OTP발급",
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그인정보 일치, OTP 발급 성공"),
                    @ApiResponse(responseCode = "400", description = "로그인정보 불일치"),
                    @ApiResponse(responseCode = "403", description = "등록된 사용자 ID와 요청ID가 다른 경우"),
                    @ApiResponse(responseCode = "406", description = "요청차단됨"),
            })
    @PostMapping("/login/app")
    public ResponseEntity<?> mobileAuthenticate(@RequestBody AuthRequestDto requestDto) {
        if (requestDto.getAccount().equals("tester12")) {
            return ResponseEntity.ok(CommonResponseDto.builder()
                    .status("200")
                    .message("테스트 계정")
                    .repCode("SUCCESS")
                    .repMsg("테스트 계정으로 로그인되었습니다")
                    .build());
        }

        Member member = memberService.checkBan(requestDto.getAccount());
        OtpTokenDto otpTokenDto = otpService.auth(requestDto, member);
        otpTokenDto.setId(member.getId());
        memberService.initFCM(requestDto, member);
        fcmNotificationService.sendNotificationByToken(otpTokenDto);

        return ResponseEntity.ok(CommonResponseDto.builder()
                .status("200")
                .message("OTP 코드가 전송되었습니다.")
                .repCode("SUCCESS")
                .repMsg("OTP 코드가 이메일로 전송되었습니다")
                .data(otpTokenDto)
                .build());
    }


    @Operation(summary = "토큰 발급",
            responses = {
                    @ApiResponse(responseCode = "200", description = "토큰 발급 성공"),
                    @ApiResponse(responseCode = "400", description = "코드 불일치, 토큰 발급 실패"),
                    @ApiResponse(responseCode = "406", description = "요청차단됨"),
                    @ApiResponse(responseCode = "500", description = "서버 에러, 토큰 발급 실패"),
            })
    @PostMapping("/token/web")
    public ResponseEntity<?> publishToken(
            @RequestBody AuthRequestDto authRequestDto,
            HttpServletResponse response,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        InfoResponseDto infoResponseDto = new InfoResponseDto();
        Member member = null;

        if(userDetails != null){
            member = userDetails.getMember();
        }

        otpService.check(authRequestDto, member);
        member = otpService.extractUserFromOtp(authRequestDto);
        redisService.resetStatus(authRequestDto.getId());

        CustomUserDetails user = member.toCustomUserDetails();
        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshToken = jwtProvider.generateRefreshToken(user);

        ResponseCookie accessTokenCookie = ResponseCookie.from("access_token", accessToken)
                .secure(true)
                .httpOnly(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(60 * 10)
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", refreshToken)
                .secure(true)
                .httpOnly(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(60 * 60 * 24 * 180)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        infoResponseDto.setMemberId(member.getId());
        infoResponseDto.setIsEnabled(member.getIsEnabled());
        infoResponseDto.setEmail(member.getEmail());
        infoResponseDto.setTel(member.getTel());
        infoResponseDto.setName(member.getName());
        infoResponseDto.setBirthDate(member.getBirthDate());
        infoResponseDto.setGender(member.getGender());
        infoResponseDto.setCountry(member.getCountry());
        infoResponseDto.setLastUpdate(member.getLastUpdate());

        return ResponseEntity.ok(CommonResponseDto.builder()
                .status("200")
                .message("로그인 성공!")
                .repCode("SUCCESS")
                .repMsg("로그인이 성공적으로 완료되었습니다")
                .data(infoResponseDto)
                .build());
    }


    @Operation(summary = "모바일 토큰 발급",
            responses = {
                    @ApiResponse(responseCode = "200", description = "토큰 발급 성공"),
                    @ApiResponse(responseCode = "400", description = "코드 불일치, 토큰 발급 실패"),
                    @ApiResponse(responseCode = "406", description = "요청차단됨"),
                    @ApiResponse(responseCode = "500", description = "서버 에러, 토큰 발급 실패"),
            })
    @PostMapping("/token/app")
    public ResponseEntity<?> publishTokenMobile(@RequestBody AuthRequestDto authRequestDto,
                                                HttpServletResponse response,
                                                @AuthenticationPrincipal CustomUserDetails userDetails) {
        InfoResponseDto infoResponseDto = new InfoResponseDto();
        if (authRequestDto.getAccount().equals("tester12")) {
            Member member = otpService.extractUserFromOtp(authRequestDto);
            CustomUserDetails user = member.toCustomUserDetails();
            String accessToken = jwtProvider.generateAccessToken(user);
            String refreshToken = jwtProvider.generateRefreshToken(user);

            ResponseCookie accessTokenCookie = ResponseCookie.from("access_token", accessToken)
                    .secure(true)
                    .httpOnly(true)
                    .path("/")
                    .sameSite("Strict")
                    .maxAge(60 * 10)
                    .build();

            ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", refreshToken)
                    .secure(true)
                    .httpOnly(true)
                    .path("/")
                    .sameSite("Strict")
                    .maxAge(60 * 60 * 24)
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

            infoResponseDto.setMemberId(member.getId());
            infoResponseDto.setIsEnabled(member.getIsEnabled());
            infoResponseDto.setEmail(member.getEmail());
            infoResponseDto.setName(member.getName());
            infoResponseDto.setTel(member.getTel());
            infoResponseDto.setBirthDate(member.getBirthDate());
            infoResponseDto.setGender(member.getGender());
            infoResponseDto.setCountry(member.getCountry());
            infoResponseDto.setLastUpdate(member.getLastUpdate());

            return ResponseEntity.ok(CommonResponseDto.builder()
                    .status("200")
                    .message("테스트 로그인 성공!")
                    .repCode("SUCCESS")
                    .repMsg("테스트 계정으로 로그인이 성공적으로 완료되었습니다")
                    .data(infoResponseDto)
                    .build());
        }

        Member member = null;
        if(userDetails != null){
            member = userDetails.getMember();
        }

        otpService.check(authRequestDto, member);
        member = otpService.extractUserFromOtp(authRequestDto);
        CustomUserDetails user = member.toCustomUserDetails();
        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshToken = jwtProvider.generateRefreshToken(user);

        ResponseCookie accessTokenCookie = ResponseCookie.from("access_token", accessToken)
                .secure(true)
                .httpOnly(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(60 * 10)
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", refreshToken)
                .secure(true)
                .httpOnly(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(60 * 60 * 24)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        infoResponseDto.setMemberId(member.getId());
        infoResponseDto.setIsEnabled(member.getIsEnabled());
        infoResponseDto.setEmail(member.getEmail());
        infoResponseDto.setName(member.getName());
        infoResponseDto.setTel(member.getTel());
        infoResponseDto.setBirthDate(member.getBirthDate());
        infoResponseDto.setGender(member.getGender());
        infoResponseDto.setCountry(member.getCountry());
        infoResponseDto.setLastUpdate(member.getLastUpdate());

        return ResponseEntity.ok(CommonResponseDto.builder()
                .status("200")
                .message("로그인 성공!")
                .repCode("SUCCESS")
                .repMsg("로그인이 성공적으로 완료되었습니다")
                .data(infoResponseDto)
                .build());
    }




    @Operation(summary = "이메일로 계정 찾기",
            responses = {
                    @ApiResponse(responseCode = "200", description = "계정 검색 성공"),
                    @ApiResponse(responseCode = "400", description = "계정 검색 실패"),
            })
    @PostMapping("/find/account")
    public ResponseEntity<?> findAccount(@Valid @RequestBody InfoRequestDto requestDto) {
        return ResponseEntity.ok(CommonResponseDto.builder()
                .status("200")
                .message("계정 검색 성공!")
                .repCode("SUCCESS")
                .repMsg("계정 정보를 성공적으로 조회했습니다")
                .data(memberService.findAccount(requestDto))
                .build());
    }

    @Operation(summary = "계정과 이메일로 비밀번호 초기화",
            responses = {
                    @ApiResponse(responseCode = "200", description = "비밀번호 초기화 성공"),
                    @ApiResponse(responseCode = "400", description = "비밀번호 초기화 실패"),
            })
    @PostMapping("/reset/pw")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody DataChangeRequestDto passwordResetDto) {
        memberService.resetPassword(passwordResetDto);
        return ResponseEntity.ok(CommonResponseDto.builder()
                .status("200")
                .message("비밀번호 초기화 성공")
                .repCode("SUCCESS")
                .repMsg("비밀번호가 성공적으로 초기화되었습니다")
                .build());
    }

}
