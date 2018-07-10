package spider.main.beans;
public class Conversation extends BaseBean {
	private static final long serialVersionUID = 7118815161398177287L;
	AnsType type;
	String img, date, content;
	String doc, level;
	String patient, state;

	public static enum AnsType {
		patient, doctor, assistant;
	}

	public AnsType getType() {
		return type;
	}

	public void setType(AnsType type) {
		this.type = type;
	}

	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getDoc() {
		return doc;
	}

	public void setDoc(String doc) {
		this.doc = doc;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getPatient() {
		return patient;
	}

	public void setPatient(String patient) {
		this.patient = patient;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
}