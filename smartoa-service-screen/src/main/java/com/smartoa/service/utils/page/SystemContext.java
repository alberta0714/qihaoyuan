package com.smartoa.service.utils.page;

/**
 * @Author hbb
 * @Date 2016/12/9 16:42
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class SystemContext {

    private static String webPath;
    public static final String SYS_EXCEPTION_CODE = "999";
    public static final String SYS_EXCEPTION_MSG = "系统出错，请联系管理员！";
	private static ThreadLocal<Integer> offset = new ThreadLocal();
	private static ThreadLocal<Integer> pagesize = new ThreadLocal();

    public static String getWebPath() {
        return webPath;
    }

    public static void setWebPath(String webPath) {
    	SystemContext.webPath = webPath;
    }

    public static void setOffset(int _offset) {
        offset.set(Integer.valueOf(_offset));
    }

    public static int getOffset() {
        Integer os = (Integer) offset.get();
        if (os == null) {
            return 0;
        }
        return os.intValue();
    }

    public static void removeOffset() {
        offset.remove();
    }

    public static void setPagesize(int _pagesize) {
        pagesize.set(Integer.valueOf(_pagesize));
    }

    public static int getPagesize() {
        Integer ps = (Integer) pagesize.get();
        if (ps == null) {
            return 2147483647;
        }
        return ps.intValue();
    }

    public static void removePagesize() {
        pagesize.remove();
    }

}
