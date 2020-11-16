package com.tanhua.sso.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.HashMap;
import java.util.Map;

public class TestJWT {

    public static void main(String[] args) {

        String secret = "geek";

        Map<String, Object> claims = new HashMap<>();
        claims.put("mobile", "12345678910");
        claims.put("id", "2");

        // 生成 token。
        String jwt = Jwts.builder()
                .setClaims(claims)// 设置响应数据体。
                .signWith(SignatureAlgorithm.HS256, secret)// 设置加密方法和加密盐。
                .compact();

        System.out.println(jwt);// eyJhbGciOiJIUzI1NiJ9.eyJtb2JpbGUiOiIxMjM0NTY3ODkxMCIsImlkIjoiMiJ9.EMoIrvpILzNs7wiReWjFlSENB4wmtfluc-K3Ts9vc1s

        // 通过 token 解析数据。
        Claims body = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(jwt)
                .getBody();

        System.out.println(body);// {mobile=12345678910, id=2}
    }

}
