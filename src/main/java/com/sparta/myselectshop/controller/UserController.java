package com.sparta.myselectshop.controller;

import com.sparta.myselectshop.dto.LoginRequestDto;
import com.sparta.myselectshop.dto.SignupRequestDto;
import com.sparta.myselectshop.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/signup") // 회원가입 페이지 보여주기
    public ModelAndView signupPage() {
        return new ModelAndView("signup");
    }

    @GetMapping("/login") // 로그인 페이지 보여주기
    public ModelAndView loginPage() {
        return new ModelAndView("login");
    }

    @PostMapping("/signup") // 회원가입 정보 보내주는 URL
    public String signup(SignupRequestDto signupRequestDto) {
        userService.signup(signupRequestDto);
        return "redirect:/api/user/login";
    }

    @ResponseBody
    @PostMapping("/login")
    public String login(@RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response) {
        userService.login(loginRequestDto, response);
        return "success";
    }
}
