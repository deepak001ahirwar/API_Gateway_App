package com.deepak.app.gateway.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.function.Predicate;

@Component
@Data
@ConfigurationProperties(prefix = "app.gateway.urls")
public class RouteValidator {

    public List<String> unsecured;

    public Predicate<ServerHttpRequest> isSecured = request -> unsecured == null ? false :
            unsecured.stream().noneMatch(request.getURI().getPath()::contains);
}
