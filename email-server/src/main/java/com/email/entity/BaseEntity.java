package com.email.entity;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;

import java.io.Serializable;

/**
 * 替代 OA BasePO 的基础实体类。
 * 只提供 setIdIfNew() —— 当 id 为 null 时自动生成雪花ID。
 */
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    /** 如果 id 为空则自动生成（兼容 OA PO 的 setIdIfNew() 调用） */
    public void setIdIfNew() {
        if (this.id == null) {
            this.id = IdWorker.getId();
        }
    }
}
