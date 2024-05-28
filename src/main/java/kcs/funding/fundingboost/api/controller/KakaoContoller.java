package kcs.funding.fundingboost.api.controller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import kcs.funding.fundingboost.api.service.CustomMessageService;
import kcs.funding.fundingboost.api.service.KakaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequiredArgsConstructor
public class KakaoContoller {

    private final KakaoService kakaoService;

    private final CustomMessageService customMessageService;

    @RequestMapping("/login")
    public RedirectView goKakaoOAuth() {
        return kakaoService.goKakaoOAuth();
    }

    @RequestMapping("/login-callback")
    public RedirectView loginCallback(@RequestParam("code") String code) {
        return kakaoService.loginCallback(code);
    }

    @GetMapping("/profile")
    public String getProfile() {
        return kakaoService.getProfile();
    }

    @GetMapping("/friends")
    public String getFriends() {
        return kakaoService.getFriends();
    }

    @RequestMapping("/logout")
    public String logout() {
        return kakaoService.logout();
    }

    @GetMapping("/send/me")
    public CompletableFuture<Boolean> sendMyMessage() {
        return customMessageService.sendReminderMessage();
    }

    @GetMapping("/send/friends")
    public ResponseEntity<String> sendMessageToFriends() {
        CompletableFuture<Boolean> resultFuture = customMessageService.sendMessageToFriends();
        boolean result;
        try {
            result = resultFuture.get();
        } catch (InterruptedException | ExecutionException exception) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("메시지 전송 실패");
        }

        if (result) {
            return ResponseEntity.ok().body("친구들에게 메세지 전송 성공");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("메시지 전송 실패");
        }
    }
}
