package com.acg.edu.itc4247;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * This class intercepts all requests and checks the Authorization table.
 * The /tables endpoint requires no Authorization, every else does.
 * An api key is associated with a tenant_id: if a valid api key is found, the associated
 * tenant_id is set as a request attribute so it can be used downstream.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
  private static Logger logger = LoggerFactory.getLogger(WebConfig.class);
  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new HandlerInterceptor() {
      @Override
      public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //the /tables endpoint is just for testing, bypass authentication
        String uri = request.getRequestURI();
        if (uri.equals("/tables")) {
          logger.info("authorizing request for /tables endpoint");
          return true;
        }

        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
          logger.info("invalid/no authorization header: {}", authorizationHeader);
          throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }

        String token = authorizationHeader.substring(7); // Extract token after "Bearer "
        String sql = "select tenant_id from apikeys where apikey=? and enabled=true  and (expiration is null OR expiration > now())";
        List<Long> TIDs = DbUtil.getJdbcTemplate().query(sql, new Object[]{token},
                (rs, rowNum) -> rs.getLong(1));
        if (TIDs.size() == 0) {
          logger.info("invalid authorization header: {}", authorizationHeader);
          throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }
        else {
          request.setAttribute("tenant_id", TIDs.get(0));
        }

        logger.info("authorization grander for endpoint {}", uri);
        return true;
      }
    });
  }
}
