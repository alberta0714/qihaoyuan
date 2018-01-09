package test.lifetools;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import com.google.common.collect.Lists;

public class OrderingMain {
	public static void main(String[] args) {
		/*
		 * 多个同事订餐时,还在为 每个人应该优惠多少 而苦恼吗? 来执行这段程序吧!
		 */
		// suanCaiyu();
		order(new Order()//
				.addOrderPerson(new OrderPerson().setSkuList(Lists.newArrayList(new Sku("传统鸭血粉丝汤-不辣", 30.9f))))//
				.addOrderPerson(new OrderPerson().setSkuList(Lists.newArrayList(new Sku("传统鸭血粉丝汤-不辣", 30.9f))))//
				.addOrderPerson(new OrderPerson().setSkuList(Lists.newArrayList(new Sku("凉面+线纯瘦肉夹馍-辣", 34.8f))))//
				, 41 + 10// 优惠
				, 6// 餐盒
				, 3// 送餐费
				, 0// 其它
		);
	}

	@SuppressWarnings("unused")
	private static void suanCaiyu() {
		order(new Order()//
				.addOrderPerson(new OrderPerson().setSkuList(Lists.newArrayList(new Sku("酸菜鱼-无刺", 30.9f)// 继续添加单品
				)))//
				.addOrderPerson(new OrderPerson().setSkuList(Lists.newArrayList(new Sku("酸菜鱼-无刺", 30.9f)// 继续添加单品
				)))//
				.addOrderPerson(new OrderPerson().setSkuList(Lists.newArrayList(new Sku("酸菜鱼-无刺", 30.9f)// 继续添加单品
		))), 53 + 7.5, 9, 5, 0);
	}

	private static void order(Order order, double discounts, double box, double shippingFee, double other) {
		order.discounts = discounts;
		order.shipping = box + shippingFee + other;

		// need to pay, total need to pay
		double totalNeedToPay = 0d;
		for (OrderPerson person : order.orderPersonList) {
			double price = 0d;
			for (Sku sku : person.skuList) {
				price += sku.getMoney() * sku.getCount();
			}
			person.setNeedToPay(price);
			totalNeedToPay += price;
		}
		order.needToPayTotal = totalNeedToPay;

		for (OrderPerson person : order.orderPersonList) {
			person.ratio = person.getNeedToPay() / totalNeedToPay;
		}

		double avgShippingFee = (box + shippingFee + other) / order.orderPersonList.size();
		for (OrderPerson person : order.orderPersonList) {
			person.discounts = discounts * person.ratio;
			person.finalPay = person.needToPay - person.discounts + avgShippingFee;
			person.shippingFee = avgShippingFee;
		}
		double finalPrice = totalNeedToPay + box + shippingFee + other - discounts;
		order.finalPrice = finalPrice;

		// 56.1
		double jiaoyan = 0d;
		System.out.println("---- 订单日期:" + new DateTime().toString("yyyy-MM-dd"));
		for (OrderPerson orderPerson : order.orderPersonList) {
			System.out.println("> " + orderPerson.getName() + ":");
			for (Sku sku : orderPerson.getSkuList()) {
				System.out
						.println("\t - " + sku.getName() + "\t单价:" + sku.getMoney() + "元\t数量:" + sku.getCount() + "份");
			}
			System.out.println("- (原价:" + format(orderPerson.needToPay) + "元,优惠:" + format(orderPerson.discounts)
					+ "元,其它:" + format(orderPerson.shippingFee) + "元)\t应付:" + format(orderPerson.finalPay) + "元");
			jiaoyan += orderPerson.finalPay;
		}
		System.out.println("++++ 总金额:" + format(order.finalPrice) + "元\t(已优惠:" + format(order.discounts) + "元" //
				+ ",送餐:" + format(shippingFee) + "元" //
				+ ",餐盒:" + format(box) + "元" //
				+ ",其它:" + format(other) + "元" //
				+ ")");
		System.out.println("<校验:" + format(jiaoyan) + ">(备:优惠根据支付占比,分配. 餐盒及配送费,均分)");
		if (format(jiaoyan) != format(order.finalPrice)) {
			System.err.println("校验错误!!");
		}
	}

	public static double format(Double d) {
		return new BigDecimal(d).setScale(2, RoundingMode.HALF_UP).doubleValue();
	}
}

class Order implements Serializable {
	private static final long serialVersionUID = -336712574528243771L;
	List<OrderPerson> orderPersonList = Lists.newArrayList();// 订餐人
	double needToPayTotal = 0;// 原价
	double finalPrice = 0;// 最终需付
	double discounts = 0;// 折扣费用
	double shipping = 0;// 送餐,餐盒等杂费

	public Order addOrderPerson(OrderPerson op) {
		this.orderPersonList.add(op);
		return this;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}

class OrderPerson implements Serializable {
	static char defaultSuffixStart = 'A';
	private static final long serialVersionUID = 2877085953013835428L;
	String name;// 订餐人姓名
	List<Sku> skuList;// 购买单品
	double needToPay;// 需要支付的金额
	double discounts;// 折扣费用
	double finalPay;// 应付
	double ratio;// 占比(根据占比,分配优惠金额)
	double shippingFee;// 送餐,餐盒等费用

	public OrderPerson() {
		initName();
	}

	public OrderPerson(String name) {
		this.name = name;
		initName();
	}

	public List<Sku> getSkuList() {
		return skuList;
	}

	public OrderPerson setSkuList(List<Sku> skuList) {
		this.skuList = skuList;
		return this;
	}

	public double getNeedToPay() {
		return needToPay;
	}

	public void setNeedToPay(double needToPay) {
		this.needToPay = needToPay;
	}

	public String getName() {
		return name;
	}

	private void initName() {
		if (StringUtils.isEmpty(this.name)) {
			String suffix = new String(new char[] { defaultSuffixStart });
			int code = (int) defaultSuffixStart;
			code++;
			defaultSuffixStart = (char) code;
			this.name = "订餐人" + suffix;
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}

class Sku implements Serializable {
	private static final long serialVersionUID = -4393866328107839793L;

	public Sku(String name, int count, float money) {
		super();
		this.name = name;
		this.count = count;
		this.money = money;
	}

	public Sku(String name, float money) {
		super();
		this.name = name;
		this.count = 1;
		this.money = money;
	}

	private String name;
	private int count = 1;
	private float money;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public float getMoney() {
		return money;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}