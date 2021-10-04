package com.hungrybrothers.alarmforsubscription.security;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hungrybrothers.alarmforsubscription.account.Account;
import com.hungrybrothers.alarmforsubscription.account.AccountAdapter;
import com.hungrybrothers.alarmforsubscription.account.AccountRepository;
import com.hungrybrothers.alarmforsubscription.common.Const;
import com.hungrybrothers.alarmforsubscription.redis.refreshtoken.RefreshToken;
import com.hungrybrothers.alarmforsubscription.redis.refreshtoken.RefreshTokenService;
import com.hungrybrothers.alarmforsubscription.sign.SignInRequest;
import com.hungrybrothers.alarmforsubscription.sign.SignInResponse;

import lombok.SneakyThrows;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    private final AccountRepository accountRepository;
    private final RefreshTokenService refreshTokenService;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, ObjectMapper objectMapper, AccountRepository accountRepository, RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.objectMapper = objectMapper;
        this.accountRepository = accountRepository;
        this.refreshTokenService = refreshTokenService;
        setFilterProcessesUrl(Const.API_SIGN + "/in");
    }

    @SneakyThrows
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        SignInRequest signInRequest = objectMapper.readValue(request.getInputStream(), SignInRequest.class);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
            signInRequest.getUserId(), signInRequest.getPassword());

        return authenticationManager.authenticate(authenticationToken);
    }

    @SneakyThrows
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) {
        AccountAdapter accountAdapter = (AccountAdapter) authResult.getPrincipal();

        String jwtToken = jwtTokenProvider.createJwtToken(accountAdapter.getAccount());
        String refreshToken = jwtTokenProvider.createRefreshToken();

        Account account = accountRepository.findByUserId(accountAdapter.getUsername())
            .orElseThrow(() -> new UsernameNotFoundException(accountAdapter.getUsername()));
        account.setRefreshToken(refreshToken);
        accountRepository.save(account);

        RefreshToken savedRefreshToken = refreshTokenService.saveRefreshToken(refreshToken, account);

        response.getWriter().write(objectMapper.writeValueAsString(SignInResponse.builder()
            .jwtToken(jwtToken)
            .refreshToken(savedRefreshToken.getRefreshToken())
            .build()));
    }
}
