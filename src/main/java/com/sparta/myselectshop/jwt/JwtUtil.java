package com.sparta.myselectshop.jwt;


import com.sparta.myselectshop.entity.UserRoleEnum;
import com.sparta.myselectshop.security.UserDetailsServiceImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {
    public static final String AUTHORIZATION_HEADER = "Authorization"; // 헤더에 들어가는 authorization 과 bearer 부분의 키값
    public static final String AUTHORIZATION_KEY = "auth"; // 사용자 권한 값의 키, 사용자 권한도 토큰안에 값을 넣어줄 건데 그거를 가지고 올 때 사용되는 키 값
    private static final String BEARER_PREFIX = "Bearer "; // 토큰을 만들 때 같이 앞에 붙어서 들어가는 부분
    private static final long TOKEN_TIME = 60 * 60 * 1000L; // 토큰 만료 시간에 사용할 시간 -> 1시간
    private final UserDetailsServiceImpl userDetailsService;
    @Value("${jwt.secret.key}") // @Value : 어노테이션이 필드나 메서드(혹은 생성자)의 파라미터 수준에서 표현식 기반으로 값을 주입해 줌
    private String secretKey;
    private Key key; // 토큰을 만들 때 넣어줄 키 값 , 여기서는 secretKey를 넣어줄거임(secretKey를 디코드해서)
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256; //HS256 이라는 알고리즘 으로 암호화 할거임

    @PostConstruct // 처음에 객체가 생성될 때 초기화 하는 함수, 결론적으로 secretKey 를 디코드 해서 key 에 넣어주는 함수
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey); // secretKey 가 base64로 인코딩이 되어 있기 때문에, 디코드를 하고 값을 넣어줌.(반환 값은 byte 배열임)
        key = Keys.hmacShaKeyFor(bytes); // secretKey를 디코드 한 결과를 key 에 넣어줌.
    }

    // header 토큰을 가져오기
    public String resolveToken(HttpServletRequest request) { // HttpServletRequest 객체 안에는 우리가 가져와야 할 토큰이 Header에 들어있음.(HttpServletRequest 안에는 사용자의 요청에 대한 모든 정보가 담겨 있다)
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER); // request 안에 있는 header 값을 가져옴, 파라미터로 어떤 키를 가지고 올지 넣어줌.
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) { // 토큰이 존재하는지, 혹은 bearer로 시작하는지 체크한 후에
            return bearerToken.substring(7); // 'bearer' 지우고 나머지 토큰 반환해줌(토큰에 연관이 되지 않는 그냥 string 은 빼고 반환)
        }
        return null;
    }

    // 토큰 생성
    public String createToken(String username, UserRoleEnum role) { // JWT를 만들어 주는 메서드
        Date date = new Date();

        return BEARER_PREFIX +
                Jwts.builder()
                        .setSubject(username)
                        .claim(AUTHORIZATION_KEY, role)
                        .setExpiration(new Date(date.getTime() + TOKEN_TIME))
                        .setIssuedAt(date)
                        .signWith(key, signatureAlgorithm)
                        .compact(); // 위의 과정을 통해 username, AUTHORIZATION_KEY, role, 현재시간, TOKEN_TIME, key, signatureAlgorithm 정보를 이용한 String 형식의 JWT 토큰이 만들어져 반환됨
    }

    // 토큰 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token); // 검증할 토큰을 받아서 넣어주고, 우리가 만든 key 값을 넣어서 검증을 해주는 메서드
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.");
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT token, 만료된 JWT token 입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
        }
        return false;
    }

    // 토큰에서 사용자 정보 가져오기
    public Claims getUserInfoFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody(); //validateToken 부분에서 getBody 를 추가해서 정보를 가져오는 메서드, 검증이 되었다고 가정해서 try,catch 문 없음
    }

    // 인증 객체 생성
    public Authentication createAuthentication(String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

}