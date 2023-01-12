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
    private final ProductRepository productRepository;

    @Transactional // 로그인한 회원에 폴더들 등록
    public List<Folder> addFolders(List<String> folderNames, String name) {

        User user = userRepository.findByUsername(name).orElseThrow(
                () -> new IllegalArgumentException("사용자가 존재하지 않습니다.")
        );

        //입력으로 들어온 폴더 이름을 기준으로, 회원이 이미 생성한 폴더들을 조회(폴더이름 중복 조회)
        List<Folder> existFolderList = folderRepository.findAllByUserAndNameIn(user, folderNames);

        List<Folder> folderList = new ArrayList<>();

        for (String folderName : folderNames) {
            //이미 생성한 폴더가 아닌 경우에만 폴더 생성
            if (!isExistFolderName(folderName, existFolderList)) {
                Folder folder = new Folder(folderName, user);
                folderList.add(folder);
            }
        }
        return folderRepository.saveAll(folderList);
    }

    // 로그인한 회원이 등록한 모든 폴더 조회
    public List<Folder> getFolders(User user) {
        return folderRepository.findAllByUser(user);
    }

    @Transactional(readOnly = true) //폴더별로 관심상품 가져오기
    public Page<Product> getProductsInFolder(Long folderId, int page, int size, String sortBy, boolean isAsc, User user) {

        //페이징 처리
        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        return productRepository.findAllByUserIdAndFolderList_Id(user.getId(), folderId, pageable);
    }

    private boolean isExistFolderName(String folderName, List<Folder> existFolderList) {
        //기존 폴더 리스트에서 folder name 이 있는지?
        for (Folder existFolder : existFolderList) {
            if (existFolder.getName().equals(folderName)) {
                return true;
            }
        }
        return false;
    }
}
