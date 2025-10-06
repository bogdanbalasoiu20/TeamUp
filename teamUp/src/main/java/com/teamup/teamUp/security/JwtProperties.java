package com.teamup.teamUp.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
//se citesc din configuratia application-local.properties setarile jwt(tine secretul Base64 pentrru semnare si timpul de expirare al token-ului)
public record JwtProperties(String secret, long expMinutes) {}

