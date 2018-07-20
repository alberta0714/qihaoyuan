package spider.haodf.bean;

import java.util.List;

import lombok.Data;

@Data
public class PageItem {
	String reportTime, userName, baseInfo, diseases, detailLink, interactLink;
	PatientProfile patientProfile;
	List<String> caseLinkList;
	List<InteractBean> interActList;
}
