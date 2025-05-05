//package org.almagestauth.controller;
//
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.almagestauth.service.MemberService;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.DeleteMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api/auth/admin")
//public class AdminController {
//    private final MemberService memberService;
//    임시삭제
//    @Operation(summary = "권한 생성",
//            responses = {
//                    @ApiResponse(responseCode = "201", description = "권한 생성 성공"),
//                    @ApiResponse(responseCode = "500", description = "권한 생성 실패"),
//            })
//    @PostMapping("/role/insert")
//    public ResponseEntity<String> insertRole(@Valid String roleName) {
//        System.out.println("Insert role");
//        boolean result = memberService.insertRole(roleName);
//        if(result){
//            return ResponseEntity.status(HttpStatus.CREATED).body("Role created");
//        }
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("에러, 다시 시도하세요");
//    }
//
//    @Operation(summary = "권한 삭제",
//            responses = {
//                    @ApiResponse(responseCode = "200", description = "권한 삭제 성공"),
//                    @ApiResponse(responseCode = "500", description = "권한 삭제 실패"),
//            })
//    @DeleteMapping("/role/delete")
//    public ResponseEntity<String> deleteRole(Long id) {
//        System.out.println("Delete role");
//        boolean result = memberService.deleteRole(id);
//        if(result){
//            return ResponseEntity.status(HttpStatus.OK).body("Role deleted");
//        }
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("에러, 다시 시도하세요");
//    }
//}
