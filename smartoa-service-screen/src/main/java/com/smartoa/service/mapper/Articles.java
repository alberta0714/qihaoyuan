package com.smartoa.service.mapper;

import java.util.Date;
import javax.persistence.*;

@Table(name = "articles")
public class Articles {
	private Integer id;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private String uuid;

	private String title;

	@Column(name = "pub_date")
	private Date pubDate;

	private String author;

	@Column(name = "article_desc")
	private String articleDesc;

	@Column(name = "class_1")
	private String class1;

	@Column(name = "class_2")
	private String class2;

	@Column(name = "class_3")
	private String class3;

	@Column(name = "class_4")
	private String class4;

	@Column(name = "class_5")
	private String class5;

	@Column(name = "article_source_url")
	private String articleSourceUrl;

	@Column(name = "create_time")
	private Date createTime;

	private String content;

	@Column(name = "origin_content")
	private String originContent;

	/**
	 * @return id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return uuid
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * @param uuid
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	/**
	 * @return title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return pub_date
	 */
	public Date getPubDate() {
		return pubDate;	}

	/**
	 * @param pubDate
	 */
	public void setPubDate(Date pubDate) {
		this.pubDate = pubDate;
	}

	/**
	 * @return author
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * @param author
	 */
	public void setAuthor(String author) {
		this.author = author;
	}

	/**
	 * @return articleDesc
	 */
	public String getArticleDesc() {
		return articleDesc;
	}

	/**
	 * @param articleDesc
	 */
	public void setArticleDesc(String articleDesc) {
		this.articleDesc = articleDesc;
	}

	/**
	 * @return class_1
	 */
	public String getClass1() {
		return class1;
	}

	/**
	 * @param class1
	 */
	public void setClass1(String class1) {
		this.class1 = class1;
	}

	/**
	 * @return class_2
	 */
	public String getClass2() {
		return class2;
	}

	/**
	 * @param class2
	 */
	public void setClass2(String class2) {
		this.class2 = class2;
	}

	/**
	 * @return class_3
	 */
	public String getClass3() {
		return class3;
	}

	/**
	 * @param class3
	 */
	public void setClass3(String class3) {
		this.class3 = class3;
	}

	/**
	 * @return class_4
	 */
	public String getClass4() {
		return class4;
	}

	/**
	 * @param class4
	 */
	public void setClass4(String class4) {
		this.class4 = class4;
	}

	/**
	 * @return class_5
	 */
	public String getClass5() {
		return class5;
	}

	/**
	 * @param class5
	 */
	public void setClass5(String class5) {
		this.class5 = class5;
	}

	/**
	 * @return article_source_url
	 */
	public String getArticleSourceUrl() {
		return articleSourceUrl;
	}

	/**
	 * @param articleSourceUrl
	 */
	public void setArticleSourceUrl(String articleSourceUrl) {
		this.articleSourceUrl = articleSourceUrl;
	}

	/**
	 * @return create_time
	 */
	public Date getCreateTime() {
		return createTime;
	}

	/**
	 * @param createTime
	 */
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	/**
	 * @return content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * @param content
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * @return origin_content
	 */
	public String getOriginContent() {
		return originContent;
	}

	/**
	 * @param originContent
	 */
	public void setOriginContent(String originContent) {
		this.originContent = originContent;
	}
}