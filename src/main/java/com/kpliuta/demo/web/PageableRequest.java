package com.kpliuta.demo.web;

public record PageableRequest(int pageNumber, int pageSize) {

    public static PageableRequest of(int pageNumber, int pageSize) {
        return new PageableRequest(pageNumber, pageSize);
    }
}
