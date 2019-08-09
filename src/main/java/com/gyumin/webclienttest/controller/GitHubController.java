package com.gyumin.webclienttest.controller;

import com.gyumin.webclienttest.dto.GitHubCommit;
import com.gyumin.webclienttest.dto.GitHubRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
public class GitHubController {

    private RestTemplateBuilder restTemplateBuilder;
    private WebClient.Builder webClientBuilder;

    public GitHubController(final RestTemplateBuilder restTemplateBuilder, final WebClient.Builder webClientBuilder) {
        this.restTemplateBuilder = restTemplateBuilder;
        this.webClientBuilder = webClientBuilder;
    }

    /**
     * 아래의 두 호출은 blocking 방식으로 동작한다.
     * 즉, result를 다 받아오기 전까지는 그 밑의 commits는 받아올 수 없다.
     * 그러나 둘 사이에는 어떤 의존성도 없다. 동시에 call해도 상관 없는 경우다.
     */
    @GetMapping("/github-resttemplate")
    public void getGithubRestTemplate() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        RestTemplate restTemplate = restTemplateBuilder.build();

        GitHubRepository[] gitHubRepositories = restTemplate.getForObject("https://api.github.com/users/gyumin-kim/repos", GitHubRepository[].class);
        Arrays.stream(gitHubRepositories).forEach(r -> {
            System.out.println(r.getUrl());
        });

        GitHubCommit[] gitHubCommits = restTemplate.getForObject("https://api.github.com/repos/gyumin-kim/object/commits", GitHubCommit[].class);
        Arrays.stream(gitHubCommits).forEach(r -> {
            System.out.println(r.getSha());
        });

        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint());
    }

    /**
     * 아래의 방법은 asynchronous하다.
     */
    @GetMapping("/github-webclient")
    public void getGuthubWebClient() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        WebClient webClient = webClientBuilder
                .baseUrl("https://api.github.com")
                .build();

//        Mono<GitHubRepository[]> repositoriesMono = webClient.get().uri("/users/gyumin-kim/repos")
//                                                            .retrieve().bodyToMono(GitHubRepository[].class);
//        repositoriesMono.doOnSuccess(rm -> {
//            for (GitHubRepository repository : rm) {
//                System.out.println(repository.getUrl());
//            }
//        }).subscribe();

        List<GitHubRepository> gitHubRepositories = webClient.get().uri("/users/gyumin-kim/repos")
                .retrieve().bodyToFlux(GitHubRepository.class).collectList().block();
        for (GitHubRepository repository : gitHubRepositories) {
            System.out.println(repository.getUrl());
        }

        Mono<GitHubCommit[]> commitsMono = webClient.get().uri("/repos/gyumin-kim/object/commits")
                                                            .retrieve().bodyToMono(GitHubCommit[].class);
        commitsMono.doOnSuccess(cm -> {
            for (GitHubCommit commit : cm) {
                System.out.println(commit.getSha());
            }
        }).subscribe();

        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint());
    }
}
