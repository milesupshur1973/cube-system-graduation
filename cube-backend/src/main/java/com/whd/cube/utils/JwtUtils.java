package com.whd.cube.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;

import java.security.Key;
import java.util.Date;

/**
 * JWT 工具类
 * 负责生成 Token 和 解析 Token
 */
@Component
public class JwtUtils {

    // 1. 设置过期时间：7天 (单位毫秒)
    private static final long EXPIRE = 604800000;

    // 2. 设置密钥 (Secret Key)
    @Value("${cube.jwt.secret}")
    private String secret;

    // 生成加密 Key
    private Key key;

    @PostConstruct
    public void init() {
        System.out.println("正在初始化 JWT Key, secret 长度: " + (secret != null ? secret.length() : "null"));
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 生成 Token
     * @param userId 用户ID
     * @param role 用户角色 (admin/user)
     * @return 加密后的 Token 字符串
     */
    public String generateToken(Long userId, String role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRE);

        return Jwts.builder()
                .setHeaderParam("type", "JWT")
                .setSubject(String.valueOf(userId)) // 将 userId 作为主题 (Subject)
                .claim("role", role)                // 存入自定义信息：角色
                .setIssuedAt(now)                   // 签发时间
                .setExpiration(expiration)          // 过期时间
                .signWith(key, SignatureAlgorithm.HS256) // 签名算法
                .compact();
    }

    /**
     * 解析 Token
     * @param token 前端传来的 Token 字符串
     * @return 解析后的 Claims (包含 userId 等信息)
     */
    public Claims getClaimsByToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            // 如果 Token 过期、被篡改或格式错误，会抛出异常
            // 这里我们简单返回 null，表示解析失败
            return null;
        }
    }
}