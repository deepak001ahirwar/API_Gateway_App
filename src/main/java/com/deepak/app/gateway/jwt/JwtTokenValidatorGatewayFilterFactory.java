package com.deepak.app.gateway.jwt;


import com.deepak.app.gateway.config.RouteValidator;
import com.deepak.app.gateway.dto.ErrorResponseDTO;
import com.deepak.app.gateway.exception.JwtTokenMissingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@RefreshScope
@Component
public class JwtTokenValidatorGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    private final static Logger LOGGER = LoggerFactory.getLogger(JwtTokenValidatorGatewayFilterFactory.class);
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RouteValidator routeValidator;

    @Override
    public GatewayFilter apply(Object config) {

        return ((exchange, chain) -> {
            if (routeValidator.isSecured.test(exchange.getRequest())) {
                try {
                    // check headers AUTHORIZATION is missing or not
                    if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                        LOGGER.warn("Authentication Token is missing");
                        throw new JwtTokenMissingException("Authentication Token is missing");
                    }
                    //fetch the Token  from req and Validate it
                    String header = Objects.requireNonNull(exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0));
                    Claims claims = jwtUtils.validateToken(header);
                    // build the headers and mutate the request and send in the updated req
                    ServerHttpRequest updatedReq = exchange.getRequest().mutate().headers(httpHeaders -> buildHttpHeaders(claims, httpHeaders)).build();
                    exchange = exchange.mutate().request(updatedReq).build();
                } catch (Exception e) {
                    LOGGER.error("Method Name : apply | Message: Token Unauthorized");
                    ErrorResponseDTO responseDTO = new ErrorResponseDTO(new Date(), HttpStatus.UNAUTHORIZED.value(),
                            HttpStatus.UNAUTHORIZED.getReasonPhrase(), e.getMessage());
                    ServerHttpResponse response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE.toString());

                    return response.writeWith(Mono.fromSupplier(() -> {
                        DataBufferFactory bufferFactory = response.bufferFactory();
                        try {
                            ObjectMapper objectMapper = new ObjectMapper();
                            return bufferFactory.wrap(objectMapper.writeValueAsBytes(responseDTO));
                        } catch (Exception ex) {
                            LOGGER.warn("Method Name : apply | Message :Error writing response");
                            return bufferFactory.wrap(new byte[0]);
                        }
                    }));
                }
            }
            return chain.filter(exchange);
        });
    }
    private void buildHttpHeaders(Claims claims, HttpHeaders httpHeaders) {
        String userName = claims.get("userName", String.class);
        String email = claims.get("email", String.class);
        List<String> roles = claims.get("roles", List.class);
        if (!StringUtils.hasText(userName)) {
            throw new RuntimeException("Malformed Token");
        }
        httpHeaders.add("X-Forwarded-UserName", userName);
        httpHeaders.add("X-Forwarded-Email", email);
        httpHeaders.addAll("X-Forwarded-Roles", roles);
    }

    public GatewayFilter apply() {
        return apply(o -> {
        });
    }
}
