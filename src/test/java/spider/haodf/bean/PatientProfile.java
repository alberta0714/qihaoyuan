package spider.haodf.bean;

import java.util.List;

import lombok.Data;

@Data
public class PatientProfile {
	String birthDay, phone, interactLink, caseLink;
	List<String> casePicList;
	
}
