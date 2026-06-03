package com.email.platform;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FlipInfo 兼容类 —— 对齐 OA com.seeyon.ctp.util.FlipInfo。
 *
 * <p>OA 中用作分页载体 + 条件参数 + 结果容器，Manager 和 DAO 层大量使用。
 * 方法签名与 OA 保持一致，确保业务代码零改动。</p>
 */
public class FlipInfo {

    /** 查询结果数据 */
    private List<?> data;

    /** 总记录数 */
    private long total;

    /** 当前页码（从 1 开始） */
    private int page = 1;

    /** 每页条数 */
    private int size = 15;

    /** 是否需要统计总数 */
    private boolean needTotal;

    /** 查询参数 Map */
    private Map<String, Object> params = new HashMap<>();

    // ==================== getter/setter ====================

    @SuppressWarnings("unchecked")
    public <T> List<T> getData() {
        return (List<T>) data;
    }

    public void setData(List<?> data) {
        this.data = data;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isNeedTotal() {
        return needTotal;
    }

    public void setNeedTotal(boolean needTotal) {
        this.needTotal = needTotal;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    // ==================== OA 可能用到的方法 ====================

    public void addParam(String key, Object value) {
        this.params.put(key, value);
    }

    /** 总页数（计算属性） */
    public int getPages() {
        if (size <= 0) return 0;
        return (int) ((total + size - 1) / size);
    }
}
