package spider.haodf.bean;

import lombok.Data;

@Data
public class PageItem {
	String reportTime, userName, baseInfo, diseases, detailLink, interactLink;
	PatientProfile patientProfile;
}
