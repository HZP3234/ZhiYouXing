package com.zhiyouxing.common.utils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class PageUtils implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<?> list;
    private int totalCount;
    private int pageSize;
    private int totalPage;
    private int currPage;

    public PageUtils(List<?> list, int totalCount, int pageSize, int currPage) {
        this.list = list;
        this.totalCount = totalCount;
        this.pageSize = pageSize;
        this.currPage = currPage;
        this.totalPage = pageSize > 0 ? (int) Math.ceil((double) totalCount / pageSize) : 0;
    }

    public PageUtils(Page<?> page) {
        this.list = page.getRecords();
        this.totalCount = (int) page.getTotal();
        this.pageSize = (int) page.getSize();
        this.currPage = (int) page.getCurrent();
        this.totalPage = (int) page.getPages();
    }
}
