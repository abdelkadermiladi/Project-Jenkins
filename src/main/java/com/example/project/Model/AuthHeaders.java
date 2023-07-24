package com.example.project.Model;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.http.HttpHeaders;

@Component
@SessionScope
public class AuthHeaders {
    private HttpHeaders headers;

    public AuthHeaders() {
    }

    public HttpHeaders getHeaders() {
        System.out.println("start getHeaders");
        return this.headers;
    }

    public void setHeaders(HttpHeaders headers) {
        System.out.println("setHeaders(): HttpHeaders:"+ headers.toString());
        this.headers = headers;
    }
}
