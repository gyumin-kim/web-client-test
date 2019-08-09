package com.gyumin.webclienttest.controller;

import com.gyumin.webclienttest.dto.Tweet;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
public class TweetController {

    private static final int DEFAULT_PORT = 8080;

    @Setter
    private int serverPort = DEFAULT_PORT;

    @GetMapping("/tweets-blocking")
    public List<Tweet> getTweetsBlocking() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        log.info("Starting BLOCKING Controller!");

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<List<Tweet>> response = restTemplate.exchange(
                getSlowServiceUri(), HttpMethod.GET, null, new ParameterizedTypeReference<List<Tweet>>() {}
        );

        List<Tweet> result = response.getBody();
        result.forEach(tweet -> log.info(tweet.toString()));

        log.info("Exiting BLOCKING Controller!");

        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint());

        return result;
    }

    @GetMapping("/tweets-non-blocking")
    public Flux<Tweet> getTweetsNonBlocking() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        log.info("Starting NON-BLOCKING Controller!");

        Flux<Tweet> tweetFlux = WebClient.create()
                .get()
                .uri(getSlowServiceUri())
                .retrieve()
                .bodyToFlux(Tweet.class);
        tweetFlux.subscribe(tweet -> log.info(tweet.toString()));

        log.info("Exiting NON-BLOCKING Controller!");

        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint());

        return tweetFlux;
    }

    @GetMapping("/slow-service-tweets")
    public List<Tweet> getAllTweets() throws Exception {
        Thread.sleep(2000L);
        return Arrays.asList(
                new Tweet("RestTemplate rules", "@user1"),
                new Tweet("WebClient is better", "@user2"),
                new Tweet("Ok, both are useful", "@user3")
        );
    }

    private String getSlowServiceUri() {
        return "http://localhost:" + serverPort + "/slow-service-tweets";
    }

}