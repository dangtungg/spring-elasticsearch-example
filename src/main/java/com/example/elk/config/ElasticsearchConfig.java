package com.example.elk.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.time.Duration;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.example.elk.repository")
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris}")
    private String elasticsearchUrl;

    @Value("${spring.elasticsearch.connection-timeout}")
    private Duration connectTimeout;

    @Value("${spring.elasticsearch.socket-timeout}")
    private Duration socketTimeout;

    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(elasticsearchUrl.replace("http://", ""))
                .withConnectTimeout(connectTimeout)
                .withSocketTimeout(socketTimeout)
                .build();
    }
}
