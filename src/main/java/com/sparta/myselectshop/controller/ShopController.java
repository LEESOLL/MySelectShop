package com.sparta.myselectshop.controller;

import com.sparta.myselectshop.service.FolderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api")
public class ShopController {

    private final FolderService folderService;

    @GetMapping("/shop")
    public ModelAndView shop() {
        return new ModelAndView("index");
    }

    // 로그인 한 유저가 메인페이지를 요청할 때 유저의 이름과 가지고 있는 폴더를 반환
    @GetMapping("/user-folder")
    public String getUserInfo(Model model, HttpServletRequest request) { // 백엔드에서 HTML 을 만들어 준 다음에 타임리프를 이용해서 클라이언트로 반환, 비동기 통신, 클라이언트 화면의 데이터 변하는 부분만 바꿔서 보여줌

        model.addAttribute("folders", folderService.getFolders(request));

        return "/index :: #fragment";
    }
}