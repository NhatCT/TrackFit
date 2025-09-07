package com.ntn.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClientConfigs {

    @Value("${ai.http.connect-timeout-ms:5000}")
    private int connectTimeoutMs;

    @Value("${ai.http.read-timeout-ms:30000}")
    private int readTimeoutMs;

    @Bean
    public RestTemplate restTemplate() {
        var simple = new SimpleClientHttpRequestFactory();
        simple.setConnectTimeout(connectTimeoutMs);
        simple.setReadTimeout(readTimeoutMs);

        var rt = new RestTemplate(new BufferingClientHttpRequestFactory(simple));

        rt.getInterceptors().add((request, body, execution) -> {
            long t0 = System.currentTimeMillis();
            try {
                return execution.execute(request, body);
            } finally {
                long ms = System.currentTimeMillis() - t0;
                System.out.println("[AI_RECO] " + request.getMethod() + " " + request.getURI() + " in " + ms + "ms");
            }
        });

        return rt;
    }
}
