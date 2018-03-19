package test.sort;

public class MergeSortTest {
	static int mid;

	public static void main(String[] args) {
		int[] data = new int[] { 4, 9, 11, 8, 67, 3, 4, 2, 32 };
		print(data);
		mergeSort(data);
		System.out.println("排序后的数组：");
		print(data);
	}

	public static void mergeSort(int[] data) {
		sort(data, 0, data.length - 1);
	}

	public static void sort(int[] data) {
		int n = data.length;
		// 步长
		int s = 2;
		int i;
		while (s <= n) {
			i = 0;
			while (i + s <= n) {
				merge(data, i, i + s / 2 - 1, i + s - 1);
				i += s;
			}
			// 处理末尾残余部分
			merge(data, i, i + s / 2 - 1, n - 1);
			s *= 2;
		}
		// 最后再从头到尾处理一遍
		merge(data, 0, s / 2 - 1, n - 1);
	}

	/**
	 * 将两个数组进行归并，归并前面2个数组已有序，归并后依然有序
	 * 
	 * @param data
	 *            数组对象
	 * @param left
	 *            左数组的第一个元素的索引
	 * @param center
	 *            左数组的最后一个元素的索引，center+1是右数组第一个元素的索引
	 * @param right
	 *            右数组最后一个元素的索引
	 */
	public static void merge(int[] data, int left, int center, int right) {
		// 临时数组
		int[] tmpArray = new int[data.length];
		// 右数组第一个元素索引
		int midNew = center + 1;
		// third 记录临时数组的索引
		int third = left;
		// 缓存左数组第一个元素的索引
		int tmp = left;
		while (left <= center && midNew <= right) {
			// 从两个数组中取出最小的放入临时数组
			if (data[left] <= data[mid]) {
				tmpArray[third++] = data[left++];
			} else {
				tmpArray[third++] = data[mid++];
			}
		}
		// 剩余部分依次放入临时数组（实际上两个while只会执行其中一个）
		while (midNew <= right) {
			tmpArray[third++] = data[midNew++];
		}
		while (left <= center) {
			tmpArray[third++] = data[left++];
		}
		// 将临时数组中的内容拷贝回原数组中
		// （原left-right范围的内容被复制回原数组）
		while (tmp <= right) {
			data[tmp] = tmpArray[tmp++];
		}
	}

	public static void print(int[] data) {
		for (int i = 0; i < data.length; i++) {
			System.out.print(data[i] + "\t");
		}
		System.out.println();
	}

	public static void sort(int[] data, int left, int right) {
		if (left >= right)
			return;
		// 找出中间索引
		int center = (left + right) / 2;
		// 对左边数组进行递归
		sort(data, left, center);
		// 对右边数组进行递归
		sort(data, center + 1, right);
		// 合并
		merge(data, left, center, right);
		print(data);
	}

}