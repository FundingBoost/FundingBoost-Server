package kcs.funding.fundingboost.api.service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import kcs.funding.fundingboost.api.dto.DefaultMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomMessageService {
    private final MessageService messageService;

    @Async
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 5000))
    //메서드가 실패할 경우 최대 3번까지 재시도, 각 재시도 사이에 5초의 지연을 둠
    //예외(Exception.class)가 발생할 경우 최대 3회까지 자동으로 재시도
//    @Scheduled(cron = "0 0 0 * * ?") // 매일 00:00 작업 실행
    @Scheduled(cron = "*/5 * * * * *") // 5초마다 작업 실행
    public CompletableFuture<Boolean> sendReminderMessage() {
        DefaultMessageDto myMsg = DefaultMessageDto.createDefaultMessageDto("text", "바로 확인하기",
                "https://www.naver.com", "펀딩 남은 기간이 2일 남았습니다!!");
        String accessToken = HttpCallService.accessToken;
        return messageService.sendMessageToMe(accessToken, myMsg);
    }

    @Async
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 5000))
    //메서드가 실패할 경우 최대 3번까지 재시도, 각 재시도 사이에 5초의 지연을 둠
    //예외(Exception.class)가 발생할 경우 최대 3회까지 자동으로 재시도
//    @Scheduled(cron = "0 0 0 * * ?") // 매일 00:00 작업 실행
    @Scheduled(cron = "*/5 * * * * *") // 5초마다 작업 실행
    public CompletableFuture<Boolean> sendMessageToFriends() {
        DefaultMessageDto myMsg = DefaultMessageDto.createDefaultMessageDto("text", "버튼 버튼",
                "https://www.naver.com", "내가 지금 생각하고 있는 것은??");
        String accessToken = HttpCallService.accessToken;
        log.info("----------------------친구한테 메시지 보내기 성공!!!----------------------");
        List<String> friendUuids = Arrays.asList(
                "aFtpXm1ZaVtuQnRMeUp9Tn5PY1JiV2JRaF8z"); // TODO: 실제로는 적절한 UUID 목록을 제공해야 합니다.
        return messageService.sendMessageToFriends(accessToken, myMsg, friendUuids);
    }

    @Recover
    public boolean recover(Exception e) {
        log.error("sendMessageToFriends 메서드가 최대 재시도 횟수를 초과하여 실패했습니다.", e);
        return false;
    }
}
