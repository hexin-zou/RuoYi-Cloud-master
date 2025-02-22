package com.ruoyi.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.List;

@Component
public class HeaderCheckGatewayFilterFactory extends AbstractGatewayFilterFactory<HeaderCheckGatewayFilterFactory.Config> {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public HeaderCheckGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getPath().value();
            boolean isExcluded = config.getExcludePaths().stream()
                    .anyMatch(excludePath -> pathMatcher.match(excludePath, path));

            if (isExcluded) {
                // 如果请求路径在排除列表中，则继续处理
                System.out.println("Path " + path + " is excluded from header check.");
                return chain.filter(exchange);
            }

            String headerValue = exchange.getRequest().getHeaders().getFirst(config.getHeaderName());
            if (headerValue != null && headerValue.equals(config.getExpectedValue())) {
                // 如果请求头符合预期值，则继续处理
                System.out.println("Header check passed for path " + path);
                return chain.filter(exchange);
            } else {
                // 否则返回403
                System.out.println("Header check failed for path " + path);
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
        };
    }

    public static class Config {
        private String headerName;
        private String expectedValue;
        private List<String> excludePaths;

        public String getHeaderName() {
            return headerName;
        }

        public void setHeaderName(String headerName) {
            this.headerName = headerName;
        }

        public String getExpectedValue() {
            return expectedValue;
        }

        public void setExpectedValue(String expectedValue) {
            this.expectedValue = expectedValue;
        }

        public List<String> getExcludePaths() {
            return excludePaths;
        }

        public void setExcludePaths(List<String> excludePaths) {
            this.excludePaths = excludePaths;
        }
    }
}