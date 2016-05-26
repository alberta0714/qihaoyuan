# 搜索整合方案研究
## 一、概述
&emsp;&emsp;目前程序通过统一的入口UniSearchServlet进入后，根据projectId走PCLineSearchHandler，MobileSearchHandler两大流程。每个流量，再通去获取不同的索引数据。  
&emsp;&emsp;合并后，先通过appplt记录出来属于PC/Mobile的流程，再执行具体的检索方法，最后在合成数据的时候，读取约定的索引内容，完成数据的组装。  

## 二、实现细节及流程图
>1. 程序通过UniSearchServlet进入后，会将appplt封装到UniSearchInput，此时需要一个枚举类，根据appplt区分出来具体使用PcLine还是MobileLine的搜索逻辑。 
```java
// 代码片段
UniSearchResult result = null;
if (SearchUtils.isPCLineSearch(appplt)) {
    result = PCLineSearchHandler.uniSearch(input);
} else {
    result = MobileSearchHandler.uniSearch(input);
}
```
>2. 类似下面这种用到project.id的地方都需要通过appplt做转换。
```java
//主站不进行扩展
String PROJECT_ID = ProConfig.Default.getProperty("project.id");
if(PROJECT_ID.equalsIgnoreCase(IKAN)){
	break;
}
```
>3. 进入搜索后，会通过统一的NRTIndexWriterManager得到索引的Searcher
```java 
// search
indexSearcher = (PPtvIndexSearcher)NRTIndexWriterManager.getInstance().acquireSearcher();
```
>4.执行完搜索后，会有PC,Mobile两种结果的封装
```java
private List<VideoBean> getSearchResult(int start, int size,
	List<SortVideoBean> allVideo, InputBean inputBean, Query videoQuery, IndexSearcher indexSearcher)
	throws IOException {
	allVideo = SearchUtil.cpIs2ToTail(allVideo, indexHolder, indexSearcher);
	List<VideoBean> listVideo = new ArrayList<VideoBean>();
	for (int i = start; i < size && i < allVideo.size(); ++i) {
		VideoBean bean = getVideoBean(inputBean, videoQuery, allVideo.get(i).getDoc());
		listVideo.add(bean);
	}
	// dp数据进行组装
	VideoRedisServiceImpl.instance().getBatchBeanFromDP(listVideo,
			inputBean);
	VideoRedisServiceImpl.instance().filterAreaSubChannel(listVideo, inputBean, "dp.url");
	ViewsFromDataService.getInstance().setViewForVideoBean(listVideo);
	return listVideo;
}
```
```java
private VideoBean getVideoBean(InputBean input, Query query, int docId)
		throws IOException {
	IndexSearcher indexSearcher = null;
	Document doc = null;
	VideoBean bean = new VideoBean();
		try {
		indexSearcher = NRTIndexWriterManager.getInstance().acquireSearcher();
		doc = indexSearcher.doc(docId);
		bean.id = Integer.parseInt(doc.get(Constant.FieldName_ID));
		... ...
		
		// 公共的部分
		bean.pid = Integer.parseInt(doc.get(Constant.FieldName_PID));
			bean.hid = StringUtil.ensureNotNull(doc.get(Constant.FieldName_HID));
			bean.score = Float.parseFloat(doc.get(Constant.FieldName_SCORE));
		... ... 
		
		// 个性化的部分
		// title,status...解耦合后需要从索引中加前缀读取

```
## 三、流程图：

```
graph TD
A[UniSearchServlet]-->B{appplt}
B-->C[PCLineSearchHandler]
B-->D[MobileSearchHandler]
C-->|NRTIndexWriterManager|E{PPtvIndexSearcher}
D-->|NRTIndexWriterManager|E{PPtvIndexSearcher}
E-->|读取对应前缀的索引|F[Index]
F-->|返回结果|A
```

附：
## 一、公共索引部分-索引字段名
> Title、Areas、Status、VStatus、 fb(Forbidden)、EnforceForbidden、VirtualFBInfo、
> VirtualFBArea、PlayList4Search、ChannelId、Pid、VT、ContentType、Hot、Score
> PlayList、InfoId、CustomScore、CP、Alias、Season、Tag、BppCataRoot、SubChannel
> CreateTime、Vip、Year、Hid、Version、Sign3D、CataId、SecondCataId、
> CataRootIds、CataIdTitle、CataIds
## 二、Ikan与client端-索引字段名
> Actor、Director、PeopleId、Classes、BaikeType、ComingTime、CoverPicture、Descrption、VideoLanguage、VideoType
## 三、多终端与ott端-索引字段名
> TitlePy、FtAll、HasMobile、Catalog、Mark、MergeFlag、Bitrate、Width、Height、VirtualFBInfo、VirtualFBarea、State、Total_State、Flag、PlayLink、PlayLink2、Othersrcs、IsOthersrc

## 四、多终端-索引字段名
> TitleFullPy、Actor、Director、PlayLink4OpenApi、ExtraStatusSuffix、ExtraSubChannelCount、ExtraMarkSuffix、ExtraFbSuffix、ExtraForceSuffix、ExtraVStatusSuffix、ExtraVirtualFBInfoSuffix、VasGameType、
> VasGameTitle、VasGameIntro、VasGameGid、VasGameIco、VasGamePic、VasGameLink、VasGameDownUrl、VasGameExtra

## 五、Ott端-索引字段名
> Actor、ActorPy、Director、DirectorPy、AliasPy、JailBrokenStatus、Safety_Sign、ExStatus、PrePlay_Sign

## 六、最终整合后的索引字段名
> Ikan_title、client_title、ott_title、mobile_title、
> Ikan_status、client_status、ott_status、mobile_status
> Ikan_vStatus、client_ vStatus、ott_ vStatus、mobile_ vStatus
> Ikan_ fb、client_ fb、ott_ fb、mobile_ fb
> Ikan_ffb、client_ffb、ott_ffb、mobile_ffb
> Ikan_contentType、client_contentType、ott_contentType、mobile_contentType
> Ikan_vip、client_vip、ott_vip、mobile_vip
> Ikan_cataId、client_cataId、ott_cataId、mobile_cataId
> Ikan_secondCataId、client_secondCataId、ott_secondCataId、mobile_secondCataId
> Ikan_cataRootIds、client_cataRootIds、ott_cataRootIds、mobile_cataRootIds、
> Ikan_cataIdTitle、client_ cataIdTitle、ott_ cataIdTitle、mobile_ cataIdTitle、
> Ikan_cataIds、client_cataIds、ott_cataIds、mobile_cataIds、
> Ikan_comingTime、client_comingTime
> ott_catalog、mobile_catalog
> ott_mark、mobile_mark
> Areas、VirtualFBInfo、VirtualFBArea、PlayList4Search、ChannelId、Pid、VT、、Hot、Score
> PlayList、InfoId、CustomScore、CP、Alias、Season、Tag、BppCataRoot、SubChannel
> CreateTime、、Year、Hid、Version、Sign3D、Actor、Director、
> PeopleId、Classes、BaikeType、、CoverPicture、Descrption、VideoLanguage、VideoType
> TitlePy、FtAll、HasMobile、、MergeFlag、Bitrate、Width、Height、VirtualFBInfo、VirtualFBarea、State、Total_State、Flag、PlayLink、PlayLink2、Othersrcs、IsOthersrc、TitleFullPy、PlayLink4OpenApi、ExtraStatusSuffix、ExtraSubChannelCount、ExtraMarkSuffix、ExtraFbSuffix、ExtraForceSuffix、ExtraVStatusSuffix、ExtraVirtualFBInfoSuffix、VasGameType、
> VasGameTitle、VasGameIntro、VasGameGid、VasGameIco、VasGamePic、VasGameLink、VasGameDownUrl、VasGameExtra、ActorPy、DirectorPy、AliasPy、JailBrokenStatus、Safety_Sign、ExStatus、PrePlay_Sign
> 



```
graph TD
XX(索引关系)
A[公共索引]-->B[Ikan,Client端索引]
A-->C[多终端与ott端公共索引]
C-->D[多终端索引]
C-->E[ott端索引]
```


