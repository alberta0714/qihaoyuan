package test;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class TestClone {
	public static void main(String[] args) throws Exception {
		TT a = new TT();
		a.a = "a1";
		a.b = "a2";

		TT b = new TT();
		b.a = "b1";


		b = (TT) a.clone();
		b.b = "b2";

		System.out.println(a);
		System.out.println(b);
	}

}

class TT implements Cloneable {
	String a;
	String b;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}