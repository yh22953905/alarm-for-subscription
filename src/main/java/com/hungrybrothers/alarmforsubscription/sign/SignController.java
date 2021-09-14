package com.hungrybrothers.alarmforsubscription.sign;

import com.hungrybrothers.alarmforsubscription.account.Account;
import com.hungrybrothers.alarmforsubscription.account.AccountAdapter;
import com.hungrybrothers.alarmforsubscription.common.Const;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(Const.API_SIGN)
@RequiredArgsConstructor
public class SignController {
    private final SignService signService;

    @PostMapping("/up")
    public ResponseEntity<Account> signUp(@RequestBody SignUpRequest signUpRequest) {
        Account signUpAccount = signService.signUp(signUpRequest);

        return ResponseEntity.ok(signUpAccount);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<SignInResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(signService.refreshToken(request));
    }

    @PostMapping("/email")
    public ResponseEntity<Object> sendEmail(@AuthenticationPrincipal AccountAdapter accountAdapter) {
        Account account = accountAdapter.getAccount();

        signService.sendEmail(account);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/email")
    public ResponseEntity<Object> verifyEmail(@RequestBody VerifyEmailRequest request, @AuthenticationPrincipal AccountAdapter accountAdapter) {
        Account account = accountAdapter.getAccount();

        signService.verifyEmail(request, account);

        return ResponseEntity.ok().build();
    }
}
