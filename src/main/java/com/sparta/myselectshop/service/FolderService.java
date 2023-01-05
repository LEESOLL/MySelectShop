package com.sparta.myselectshop.service;

import com.sparta.myselectshop.entity.Folder;
import com.sparta.myselectshop.entity.Product;
import com.sparta.myselectshop.entity.User;
import com.sparta.myselectshop.jwt.JwtUtil;
import com.sparta.myselectshop.repository.FolderRepository;
import com.sparta.myselectshop.repository.ProductRepository;
import com.sparta.myselectshop.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    private final ProductRepository productRepository;

    @Transactional // 로그인한 회원에 폴더들 등록
    public List<Folder> addFolders(List<String> folderNames, HttpServletRequest request) {

        // Request 에서 Token 가져오기
        String token = jwtUtil.resolveToken(request);
        Claims claims; //JWT 안에 있는 정보를 담는 객체

        // 토큰이 있는 경우에만 관심상품 조회 가능
        if (token != null) {

            // 토큰 검증
            if (jwtUtil.validateToken(token)) {
                // 토큰에서 사용자 정보 가져오기
                claims = jwtUtil.getUserInfoFromToken(token);
            } else {
                throw new IllegalArgumentException("Token Error");
            }

            // 토큰에서 가져온 사용자 정보를 사용하여 DB 조회
            User user = userRepository.findByUsername(claims.getSubject()).orElseThrow(
                    () -> new IllegalArgumentException("사용자가 존재하지 않습니다.")
            );

            List<Folder> folderList = new ArrayList<>();

            for (String folderName : folderNames) {
                Folder folder = new Folder(folderName, user);
                folderList.add(folder);
            }
            return folderRepository.saveAll(folderList);
        } else {
            return null;
        }
    }

    // 로그인한 회원이 등록된 모든 폴더 조회
    public List<Folder> getFolders(HttpServletRequest request) {

        // 사용자의 정보를 가져온다
        // Request 에서 Token 가져오기
         String token = jwtUtil.resolveToken(request);
         Claims claims;

         // 토큰이 있는 경우에만 관심상품 조회 가능
        if (token != null) {
           // token 검증
           if (jwtUtil.validateToken(token)) {
               //토큰에서 사용자 정보 가져오기
               claims = jwtUtil.getUserInfoFromToken(token);
           } else {
               throw new IllegalArgumentException("Token Error");
           }

           // 토큰에서 가져온 사용자 정보를 사용하여 DB 조회
            User user = userRepository.findByUsername(claims.getSubject()).orElseThrow(
                    () -> new IllegalArgumentException("사용자가 존재하지 않습니다.")
            );

           return folderRepository.findAllByUser(user);
        } else {
            return null;
        }
    }

    @Transactional(readOnly = true) //폴더에 있는 관심상품 가져오기
    public Page<Product> getProductsInFolder(Long folderId, int page, int size, String sortBy, boolean isAsc, HttpServletRequest request) {

        //페이징 처리
        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        //request 에서 토큰 가져오기
        String token = jwtUtil.resolveToken(request);
        Claims claims;

        // 토큰이 있는 경우에만 관심상품 조회 가능
        if (token != null) {
            //token 검증
            if (jwtUtil.validateToken(token)) {
                //토큰에서 사용자 정보 가져오기
                claims = jwtUtil.getUserInfoFromToken(token);
            } else {
                throw new IllegalArgumentException("Token Error");
            }

            //토큰에서 가져온 사용자 정보를 사용하여 DB 조회
            User user = userRepository.findByUsername(claims.getSubject()).orElseThrow(
                    () -> new IllegalArgumentException("사용자가 존재하지 않습니다.")
            );

            return productRepository.findAllByUserIdAndFolderList_Id(user.getId(), folderId, pageable);

        } else {
            return null;
        }

    }
}
