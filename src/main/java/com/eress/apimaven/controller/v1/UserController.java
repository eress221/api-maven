package com.eress.apimaven.controller.v1;

import com.eress.apimaven.advice.exception.CUserNotFoundException;
import com.eress.apimaven.entity.User;
import com.eress.apimaven.model.response.CommonResult;
import com.eress.apimaven.model.response.ListResult;
import com.eress.apimaven.model.response.SingleResult;
import com.eress.apimaven.repo.UserJpaRepo;
import com.eress.apimaven.service.ResponseService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Api(tags = {"2. User"})
@RequiredArgsConstructor
@RestController // 결과값을 JSON으로 출력합니다.
@RequestMapping(value = "/v1")
public class UserController {

    private final UserJpaRepo userJpaRepo;
    private final ResponseService responseService; // 결과를 처리할 Service

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 access_token", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "회원 리스트 조회", notes = "모든 회원을 조회한다")
    @GetMapping(value = "/users")
    public ListResult<User> findAllUser() {
        // 결과데이터가 여러건인경우 getListResult를 이용해서 결과를 출력한다.
        return responseService.getListResult(userJpaRepo.findAll());
    }

//    @ApiOperation(value = "회원 단건 조회", notes = "userId로 회원을 조회한다")
//    @GetMapping(value = "/user/{msrl}")
//    public SingleResult<User> findUserById(@ApiParam(value = "회원ID", required = true) @PathVariable long msrl) {
//        // 결과데이터가 단일건인경우 getBasicResult를 이용해서 결과를 출력한다.
//        return responseService.getSingleResult(userJpaRepo.findById(msrl).orElseThrow(CUserNotFoundException::new));
//    }

//    @ApiOperation(value = "회원 단건 조회", notes = "userId로 회원을 조회한다")
//    @GetMapping(value = "/user/{msrl}")
//    public SingleResult<User> findUserById(@ApiParam(value = "회원ID", required = true) @PathVariable long msrl,
//                                           @ApiParam(value = "언어", defaultValue = "ko") @RequestParam String lang) {
//        // 결과데이터가 단일건인경우 getBasicResult를 이용해서 결과를 출력한다.
//        return responseService.getSingleResult(userJpaRepo.findById(msrl).orElseThrow(CUserNotFoundException::new));
//    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 access_token", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "회원 단건 조회", notes = "회원번호(msrl)로 회원을 조회한다")
    @GetMapping(value = "/user")
    public SingleResult<User> findUser() {
        // SecurityContext에서 인증받은 회원의 정보를 얻어온다.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String id = authentication.getName();
        // 결과데이터가 단일건인경우 getSingleResult를 이용해서 결과를 출력한다.
        return responseService.getSingleResult(userJpaRepo.findByUid(id).orElseThrow(CUserNotFoundException::new));
    }

//    @ApiOperation(value = "회원 입력", notes = "회원을 입력한다")
//    @PostMapping(value = "/user")
//    public SingleResult<User> save(@ApiParam(value = "회원아이디", required = true) @RequestParam String uid,
//                                   @ApiParam(value = "회원이름", required = true) @RequestParam String name) {
//        User user = User.builder()
//                .uid(uid)
//                .name(name)
//                .build();
//        return responseService.getSingleResult(userJpaRepo.save(user));
//    }

//    @ApiOperation(value = "회원 수정", notes = "회원정보를 수정한다")
//    @PutMapping(value = "/user")
//    public SingleResult<User> modify(
//            @ApiParam(value = "회원번호", required = true) @RequestParam long msrl,
//            @ApiParam(value = "회원아이디", required = true) @RequestParam String uid,
//            @ApiParam(value = "회원이름", required = true) @RequestParam String name) {
//        User user = User.builder()
//                .msrl(msrl)
//                .uid(uid)
//                .name(name)
//                .build();
//        return responseService.getSingleResult(userJpaRepo.save(user));
//    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 access_token", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "회원 수정", notes = "회원정보를 수정한다")
    @PutMapping(value = "/user")
    public SingleResult<User> modify(
            @ApiParam(value = "회원번호", required = true) @RequestParam long msrl,
            @ApiParam(value = "회원이름", required = true) @RequestParam String name) {
        User user = User.builder()
                .msrl(msrl)
                .name(name)
                .build();
        return responseService.getSingleResult(userJpaRepo.save(user));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 access_token", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "회원 삭제", notes = "회원번호(msrl)로 회원정보를 삭제한다")
    @DeleteMapping(value = "/user/{msrl}")
    public CommonResult delete(
            @ApiParam(value = "회원번호", required = true) @PathVariable long msrl) {
        userJpaRepo.deleteById(msrl);
        // 성공 결과 정보만 필요한경우 getSuccessResult()를 이용하여 결과를 출력한다.
        return responseService.getSuccessResult();
    }
}
