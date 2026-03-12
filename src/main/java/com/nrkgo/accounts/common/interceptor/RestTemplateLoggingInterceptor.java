package com.nrkgo.accounts.common.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class RestTemplateLoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RestTemplateLoggingInterceptor.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        logRequest(request, body);
        ClientHttpResponse response = execution.execute(request, body);
        logResponse(response);
        return response;
    }

    private void logRequest(HttpRequest request, byte[] body) {
        log.info("=========================== Outbound Request Start ===========================");
        log.info("URI         : {}", request.getURI());
        log.info("Method      : {}", request.getMethod());

        // Log headers but mask Authorization
        request.getHeaders().forEach((name, values) -> {
            if (name.equalsIgnoreCase("Authorization")) {
                log.info("Header      : {} = [Bearer ********]", name);
            } else {
                log.info("Header      : {} = {}", name, values);
            }
        });

        log.info("Request body: {}", new String(body, StandardCharsets.UTF_8));
        log.info("=========================== Outbound Request End =============================");
    }

    private void logResponse(ClientHttpResponse response) throws IOException {
        log.info("============================ Outbound Response Start ===========================");
        log.info("Status code  : {}", response.getStatusCode());
        log.info("Status text  : {}", response.getStatusText());
        log.info("Headers      : {}", response.getHeaders());

        StringBuilder inputStringBuilder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {
            String line = bufferedReader.readLine();
            while (line != null) {
                inputStringBuilder.append(line);
                inputStringBuilder.append('\n');
                line = bufferedReader.readLine();
            }
        }
        log.info("Response body: {}", inputStringBuilder);
        log.info("============================ Outbound Response End =============================");
    }
}
