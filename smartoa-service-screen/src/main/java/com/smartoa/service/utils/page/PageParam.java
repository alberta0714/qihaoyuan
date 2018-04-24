package com.smartoa.service.utils.page;

/**
 * @Author hbb
 * @Date 2016/12/9 16:40
 */
public class PageParam {

    private Integer start;
    private Integer limit;
    private Integer total;

    public Integer getStart() {
        return this.start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getLimit() {
        return this.limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getTotal() {
        return this.total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

}
