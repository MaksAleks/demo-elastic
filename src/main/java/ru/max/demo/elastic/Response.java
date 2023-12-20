package ru.max.demo.elastic;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.springframework.http.HttpStatus;

@Builder
@Value
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {
    HttpStatus status;
}
