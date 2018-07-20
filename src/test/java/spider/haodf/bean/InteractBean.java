package spider.haodf.bean;

import java.util.List;

import lombok.Data;
import spider.main.beans.Conversation;

@Data
public class InteractBean {
	String title,link,  relatedDiseases, updateDate;
	List<Conversation> conversationList;
	String question;
}
