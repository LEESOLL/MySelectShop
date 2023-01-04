package com.sparta.myselectshop.dto;

import com.sparta.myselectshop.entity.Product;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ProductResponseDto {
    private Long id;
    private String title;
    private String link;
    private String image;
    private int lprice;
    private int myprice;

    private LocalDateTime createAt;
    private LocalDateTime modifiedAt;

    public ProductResponseDto(Product product) {
        this.id = product.getId();
        this.title = product.getTitle();
        this.link = product.getLink();
        this.image = product.getImage();
        this.lprice = product.getLprice();
        this.myprice = product.getMyprice();
        this.createAt = product.getCreatedAt();
        this.modifiedAt = product.getModifiedAt();

    }
}