package com.krunish.common.dto;

import java.util.List;
import org.springframework.data.domain.Page;

public class ApiPage<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public static <T> ApiPage<T> from(Page<T> page) {
        ApiPage<T> p = new ApiPage<>();
        p.content = page.getContent();
        p.page = page.getNumber();
        p.size = page.getSize();
        p.totalElements = page.getTotalElements();
        p.totalPages = page.getTotalPages();
        return p;
    }
}
