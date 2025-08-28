package com.ntn.utils;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

public class JwtUtils {
    private static final String SECRET = "12345678901234567890123456789012"; // >=32 ký tự
    private static final long EXPIRATION_MS = 24 * 60 * 60 * 1000L; // 1 ngày

    private static final String CLAIM_ROLES = "roles";

    // Sinh token kèm roles
    public static String generateToken(String username, List<String> roles) throws JOSEException {
        JWSSigner signer = new MACSigner(SECRET);

        Date now = new Date();
        Date exp = new Date(now.getTime() + EXPIRATION_MS);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(username)
                .issueTime(now)
                .expirationTime(exp)
                .claim(CLAIM_ROLES, roles) // thêm roles vào claim
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
        signedJWT.sign(signer);

        return signedJWT.serialize();
    }

    // Giữ hàm cũ nếu chỉ muốn tạo token đơn giản
    public static String generateToken(String username) throws JOSEException {
        return generateToken(username, Collections.emptyList());
    }

    public static String validateTokenAndGetUsername(String token) throws Exception {
        SignedJWT jwt = parseAndVerify(token);
        if (jwt == null) return null;

        Date exp = jwt.getJWTClaimsSet().getExpirationTime();
        if (exp != null && exp.after(new Date())) {
            return jwt.getJWTClaimsSet().getSubject();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static List<String> getRoles(String token) throws Exception {
        SignedJWT jwt = parseAndVerify(token);
        if (jwt == null) return Collections.emptyList();

        Object raw = jwt.getJWTClaimsSet().getClaim(CLAIM_ROLES);
        if (raw instanceof List<?> list) {
            return list.stream().map(Object::toString).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private static SignedJWT parseAndVerify(String token) throws ParseException, JOSEException {
        SignedJWT jwt = SignedJWT.parse(token);
        JWSVerifier verifier = new MACVerifier(SECRET);
        if (!jwt.verify(verifier)) return null;
        return jwt;
    }
}
