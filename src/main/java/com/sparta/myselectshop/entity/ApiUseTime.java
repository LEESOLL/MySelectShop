package com.sparta.myselectshop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class ApiUseTime {
    // ID가 자동으로 생성 및 증가함
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    @OneToOne
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Column(nullable = false)
    private Long totalTime;

    public ApiUseTime(User user, long totalTime) {
        this.user = user;
        this.totalTime = totalTime;
    }

    public void addUseTime(long useTime) {
        this.totalTime += useTime;
    }

}
