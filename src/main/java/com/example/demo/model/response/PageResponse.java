package com.example.demo.model.response;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class PageResponse<T> {

    Integer pageNum;

    Integer pageSize;

    Long total;

    List<T> list;
}
