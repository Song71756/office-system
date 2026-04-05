package com.office.newofficeautomationbackend.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * 负责 Token 的生成、解析和校验
 */
@Component
public class JwtUtils {

    // Token 过期时间：24 小时 (单位：毫秒)
    private static final long EXPIRE_TIME = 24 * 60 * 60 * 1000;

    // 从配置文件中读取固定的签名密钥
    @Value("${jwt.secret-key}")
    private String secretKeyStr;

    private SecretKey key;

    /**
     * 初始化密钥
     * @PostConstruct 确保在依赖注入完成后执行，将配置文件的字符串转换为 SecretKey 对象
     */
    @PostConstruct
    public void init() {
        // 使用固定的字符串生成 HMAC-SHA 密钥，确保重启后密钥不变
        this.key = Keys.hmacShaKeyFor(secretKeyStr.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 根据用户名生成 Token
     * @param username 登录成功的用户名
     * @return 签名后的 JWT 字符串
     */
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析并获取 Token 中的用户信息 (Subject)
     * @param token 客户端发送的 Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    /**
     * 校验 Token 是否过期
     * @param token 待校验的 Token
     * @return true 表示已过期，false 表示仍有效
     */
    public boolean isTokenExpired(String token) {
        Date expiration = getClaimsFromToken(token).getExpiration();
        return expiration.before(new Date());
    }

    /**
     * 获取 Token 的过期时间
     * @param token 待解析的 Token
     * @return Token 的过期时间 Date 对象
     */
    public Date getExpirationFromToken(String token) {
        return getClaimsFromToken(token).getExpiration();
    }

    /**
     * 获取 Token 的完整载荷 (Claims)
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
