package org.example.global.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {
    private T data;
    private PageInfo page;

    private ApiResponse(T data) {
        this.data = data;
    }

    private ApiResponse(T data, PageInfo page) {
        this.data = data;
        this.page = page;
    }

    // 페이징 적용 X
    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(data);
    }

    // 페이징 적용
    public static <T> ApiResponse<T> of(T data, PageInfo page) {
        return new ApiResponse<>(data, page);
    }

}