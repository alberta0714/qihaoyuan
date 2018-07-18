package spider.haodf.bean;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class PageListBean {
	String pageLink;
	List<PageItem> pageItemList = new ArrayList<PageItem>();
}

