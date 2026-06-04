package com.email.common;


import java.util.List;

/**
 * 分页结果（替代 OA 的 FlipInfo 分页对象）。
 */
public class PageResult<T> {

    /** 数据列表 */
    private List<T> records;
    /** 总记录数 */
    private long total;
    /** 当前页 */
    private long current;
    /** 每页条数 */
    private long size;
    /** 总页数 */
    private long pages;

    public static <T> PageResult<T> of(List<T> records, long total, long current, long size) {
        PageResult<T> p = new PageResult<>();
        p.records = records;
        p.total = total;
        p.current = current;
        p.size = size;
        p.pages = (total + size - 1) / size;
        return p;
    }
}
