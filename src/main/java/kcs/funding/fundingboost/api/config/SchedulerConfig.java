package kcs.funding.fundingboost.api.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@EnableRetry
@EnableScheduling
public class SchedulerConfig implements AsyncConfigurer {
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10); //스레드 풀의 기본 크기
        executor.setMaxPoolSize(20); // 스레드 풀 최대 크기 (동시에 실행 가능한 최대 스레드 수)
        executor.setQueueCapacity(500); // 작업 큐의 용량 (최대 스레드 수 초과 시 대기 시킬 큐의 크기)
        executor.setThreadNamePrefix("Async-"); // 스레드 풀에서 생성된 스레드의 이름 접두사
        executor.initialize(); // 스레드 풀을 초기화 & 사용 준비 완료
        return executor;
    }
}
