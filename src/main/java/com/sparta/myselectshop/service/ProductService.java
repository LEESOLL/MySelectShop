package com.sparta.myselectshop.service;

import com.sparta.myselectshop.dto.ProductMypriceRequestDto;
import com.sparta.myselectshop.dto.ProductRequestDto;
import com.sparta.myselectshop.dto.ProductResponseDto;
import com.sparta.myselectshop.entity.Folder;
import com.sparta.myselectshop.entity.Product;
import com.sparta.myselectshop.entity.User;
import com.sparta.myselectshop.entity.UserRoleEnum;
import com.sparta.myselectshop.jwt.JwtUtil;
import com.sparta.myselectshop.naver.dto.ItemDto;
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
public class ProductService {

    private final ProductRepository productRepository;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository; // user 정보를 알아야 user 정보에 따른 상품 관련 로직을 수행할 수 있음 -> userRepository 의존성 주입해줘야 함

    private final FolderRepository folderRepository;

    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto requestDto, HttpServletRequest request) {
        // Request 의 Header 에서 Token 가져오기
        String token = jwtUtil.resolveToken(request);
        Claims claims; // JWT 안에 들어있는 정보를 담을 수 있는 객체 정도로 이해하기

        // 토큰이 있는 경우에만 관심상품 추가 가능
        if (token != null) {
            if (jwtUtil.validateToken(token)) {
                // 토큰에서 사용자 정보 가져와서 claims 객체에 넣어주기
                claims = jwtUtil.getUserInfoFromToken(token);
            } else {
                throw new IllegalArgumentException("Token Error");
            }

            // 토큰에서 가져온 사용자 정보를 사용하여 DB 조회
            User user = userRepository.findByUsername(claims.getSubject()).orElseThrow( // claims.getSubject() 하면 user 의 이름을 가져올 수 있음
                    () -> new IllegalArgumentException("사용자가 존재하지 않습니다.")
            );

            // 요청받은 DTO 로 DB에 저장할 객체 만들기
            Product product = productRepository.saveAndFlush(new Product(requestDto, user.getId()));

            return new ProductResponseDto(product);
        } else { // 토큰이 없으면 null 반환
            return null;
        }
    }


    @Transactional(readOnly = true)
    public Page<Product> getProducts(HttpServletRequest request,
                                     int page, int size, String sortBy, boolean isAsc) {

        // 페이징 처리
        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy); // sortBy 파라미터 -> 어떤 값을 기준으로 오름차순 혹은 내림차순 할 건지(id, 상품명, 최저가)
        Pageable pageable = PageRequest.of(page, size, sort);

        // Request 의 Header 에서 Token 가져오기
        String token = jwtUtil.resolveToken(request);
        Claims claims; // JWT 안에 들어있는 정보를 담을 수 있는 객체 정도로 이해하기

        if(token != null) {  // 토큰이 있는 경우에만 관심상품 조회 가능
            // Token 검증
            if (jwtUtil.validateToken(token)) {
                // 토큰에서 사용자 정보 가져오기
                claims = jwtUtil.getUserInfoFromToken(token);
            } else {
                throw new IllegalArgumentException("Token Error");
            }

            // 토큰에서 가져온 사용자 정보를 사용하여 DB 조회
            User user = userRepository.findByUsername(claims.getSubject()).orElseThrow( // claims.getSubject() 하면 user 의 이름을 가져올 수 있음
                    () -> new IllegalArgumentException("사용자가 존재하지 않습니다.")
            );

            // 사용자 권한 가져와서 ADMIN 이면 전체 조회, USER 면 본인이 추가한 부분 조회
            UserRoleEnum userRoleEnum = user.getRole();
            System.out.println("role = " + userRoleEnum);

            Page<Product> products;

            if (userRoleEnum == UserRoleEnum.USER) {
                // 사용자 권한이 USER 일 경우
                products = productRepository.findAllByUserId(user.getId(), pageable);
            } else {
                products = productRepository.findAll(pageable); // 사용자 권한이 ADMIN 일 경우
            }

            return products;

        }else {
            return null;
        }
    }

    @Transactional
    public Long updateProduct(Long id, ProductMypriceRequestDto requestDto, HttpServletRequest request) {
        // Request에서 Token 가져오기
        String token = jwtUtil.resolveToken(request);
        Claims claims;

        // 토큰이 있는 경우에만 관심상품 최저가 업데이트 가능
        if (token != null) {
            // Token 검증
            if (jwtUtil.validateToken(token)) {
                claims = jwtUtil.getUserInfoFromToken(token);  // 토큰에서 사용자 정보 가져오기
            } else {
                throw new IllegalArgumentException("Token Error");
            }

            // 토큰에서 가져온 사용자 정보를 사용하여 DB 조회
            User user = userRepository.findByUsername(claims.getSubject()).orElseThrow(
                    () -> new IllegalArgumentException("사용자가 존재하지 않습니다.")
            );

            Product product = productRepository.findByIdAndUserId(id, user.getId()).orElseThrow(
                    () -> new NullPointerException("해당 상품은 존재하지 않습니다.")
            );

            product.update(requestDto);

            return product.getId();

        } else {
            return null;
        }
    }

    @Transactional
    public void updateBySearch(Long id, ItemDto itemDto) {
        Product product = productRepository.findById(id).orElseThrow(
                () -> new NullPointerException("해당 상품은 존재하지 않습니다.")
        );
        product.updateByItemDto(itemDto);
    }

    @Transactional
    public Product addFolder(Long productId, Long folderId, HttpServletRequest request) {
        // request 에서 Token 가져오기
        String token = jwtUtil.resolveToken(request);
        Claims claims;

        // 토큰이 있는 경우에만 관심상품에 폴더 추가 가능
        if (token != null) {
            // Token 검증
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

            //1)관심상품 조회
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new NullPointerException("해당 상품 아이디가 존재하지 않습니다."));

            //2)폴더 조회
            Folder folder = folderRepository.findById(folderId)
                    .orElseThrow(() -> new NullPointerException("해당 폴더 아이디가 존재하지 않습니다"));

            //3)조회한 폴더와 관심상품이 모두 로그인한 회원의 소유인지 확인
            Long loginUserId = user.getId();
            if(!product.getUserId().equals(loginUserId) || !folder.getUser().getId().equals(loginUserId)) {
                throw new IllegalArgumentException("회원님의 관심상품이 아니거나 회원님의 폴더가 아닙니다.");
            }

            //4)상품에 폴더를 추가
            product.addFolder(folder);

            return product;
        } else {
            return null;
        }

    }
}