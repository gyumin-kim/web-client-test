package com.gyumin.webclienttest;

import com.gyumin.webclienttest.github.GitHubCommit;
import com.gyumin.webclienttest.github.GitHubRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@SpringBootApplication
public class WebClientTestApplication {

    @Autowired
    RestTemplateBuilder restTemplateBuilder;

    @Autowired
    WebClient.Builder webClientBuild;

    public static void main(String[] args) {
        SpringApplication.run(WebClientTestApplication.class, args);
    }

    @Bean
    public ApplicationRunner applicationRunner() {
        return args -> {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            // 아래의 두 코드는 blocking 방식으로 동작한다.
            // 즉, result를 다 받아오기 전까지는 그 밑의 commits는 받아올 수 없다.
            // 둘 사이에는 어떤 의존성도 없다. 동시에 call해도 상관 없는 경우다.

//			RestTemplate restTemplate = restTemplateBuilder.build();
//			GitHubRepository[] result = restTemplate.getForObject("https://api.github.com/users/gyumin-kim/repos", GitHubRepository[].class);
//			Arrays.stream(result).forEach(r -> {
//				System.out.println("repo: " + r.getUrl());
//			});
//
//			GitHubCommit[] commits = restTemplate.getForObject("https://api.github.com/repos/gyumin-kim/object/commits", GitHubCommit[].class);
//			Arrays.stream(commits).forEach(c -> {
//				System.out.println(c.getSha());
//			});


            // 아래의 방법은 asynchronous하다.

            WebClient webClient = webClientBuild.baseUrl("https://api.github.com").build();
            Mono<GitHubRepository[]> reposMono = webClient.get().uri("/users/gyumin-kim/repos")
                    .retrieve()
                    .bodyToMono(GitHubRepository[].class);

            Mono<GitHubCommit[]> commitsMono = webClient.get().uri("/repos/gyumin-kim/object/commits")
                    .retrieve()
                    .bodyToMono(GitHubCommit[].class);

            reposMono.doOnSuccess(ra -> {
                Arrays.stream(ra).forEach(r -> {
                    System.out.println("repo: " + r.getUrl());
                });
            }).subscribe();

            commitsMono.doOnSuccess(ca -> {
                Arrays.stream(ca).forEach(c -> {
                    System.out.println("commit: " + c.getSha());
                });
            }).subscribe();

            stopWatch.stop();
            System.out.println(stopWatch.prettyPrint());
        };
    }

}
