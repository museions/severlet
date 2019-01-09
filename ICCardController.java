package com.sxdh.driverTrain.controller.system.icCard;

import com.alibaba.fastjson.JSONObject;
import com.sxdh.driverTrain.controller.base.BaseController;
import com.sxdh.driverTrain.entity.Page;
import com.sxdh.driverTrain.entity.system.User;
import com.sxdh.driverTrain.service.business.student.StudentService;
import com.sxdh.driverTrain.service.institution.institution.InstitutionService;
import com.sxdh.driverTrain.service.resource.TrainTimeService;
import com.sxdh.driverTrain.service.system.dictionaries.DictionariesService;
import com.sxdh.driverTrain.service.system.icCard.ICCardService;
import com.sxdh.driverTrain.util.*;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/icCard")
public class ICCardController extends BaseController {

	String menuUrl = "icCard/list.do"; //菜单地址(权限用)
	@Resource(name="trainTimeService")
	private TrainTimeService trainTimeService;
	@Resource(name="dictionariesService")
	private DictionariesService dictionariesService;
	@Resource(name="studentService")
	private StudentService studentService;
	@Resource(name="icCardService")
	private ICCardService icCardService;
	@Resource(name="institutionService")
	private InstitutionService institutionService;

	@RequestMapping("/list")
	public ModelAndView getPageList(Page page) {
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		try {
			pd = this.getPageData();
			User user=getCurrentUser();
			if(user.getInsitution()!=null&&!"".equals(user.getInsitution())){
				pd.put("inscode", user.getInsitution());
			}
			String inscode = pd.getString("inscode");
			if(null != inscode && !"".equals(inscode)){
				inscode = inscode.trim();
				pd.put("inscode", inscode);
			}
			if(null != pd.get("startDate") && !"".equals(pd.get("startDate"))){
				pd.put("startDate", pd.get("startDate"));
			}
			String endDate = "";
			if(null != pd.get("endDate") && !"".equals(pd.get("endDate"))){
				endDate = pd.get("endDate").toString();
				pd.put("endDate", pd.get("endDate")+" 23:59:59");
			}
			page.setPd(pd);
			List<PageData> icCardList = icCardService.getICCardlistPage(page);
			List<PageData> institutionList = institutionService.getInstitutionList();
			pd.put("endDate", endDate);
			mv.addObject("pd", pd);
			mv.addObject("icCardList", icCardList);
			mv.addObject("institutionList", institutionList);
			mv.setViewName("system/icCard/icCard_list");
		} catch (Exception e) {
			logger.error(e.toString(),e);
		}
		return mv;
	}

	/**
	 * 打开上传EXCEL页面
	 */
	@RequestMapping(value="/goUploadExcel")
	public ModelAndView goUploadExcel(Page page)throws Exception{
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		String baseType = pd.getString("baseType");
		if(null != baseType && !"".equals(baseType)){
			baseType = baseType.trim();
			pd.put("baseType", baseType);
			pd.put("choose", "only");
			pd.put("mymsg", "导入IC卡");
			pd.put("myaction", "readExcel");
		}
		mv.setViewName("fromExcel");					//返回上传excel页面，可以在该步骤添加返回 baseType的值，从而判断需要下载的模板
		mv.addObject("pd", pd);
		return mv;
	}

	/**
	 * 从EXCEL导入到数据库
	 */
	@RequestMapping(value="/readExcel")
	public ModelAndView readExcel(@RequestParam(value="excel",required=false) MultipartFile file
	) throws Exception{
		ModelAndView mv = this.getModelAndView();
		User user=getCurrentUser();
		String importUserId = "";
		if(user.getUSER_ID()!=null&&!"".equals(user.getUSER_ID())){
			importUserId = user.getUSER_ID();
		}
		List<String> result = new ArrayList<String>();							//错误结果返回
		int res = 0;
		if (null != file && !file.isEmpty()) {
			String filePath = PathUtil.getClasspath() + Const.FILEPATHFILE;                                //文件上传路径
			String fileName = FileUpload.fileUp(file, filePath, "ICCard");                            //执行上传
			List<PageData> listPd = (List) ObjectExcelRead.readExcel(filePath, fileName, 0, 0, 0);    //执行读EXCEL操作,读出的数据导入List 1:从第2行开始；0:从第A列开始；0:第0个sheet
			Map<String, Object> returnMap = icCardService.fromExcel(listPd,importUserId);
			if (null != returnMap && !returnMap.isEmpty()) {
				res = (Integer) returnMap.get("res");
				result = (List<String>) returnMap.get("result");
				if (res != 0 && res != listPd.size()) {
					result.add("导入成功" + res + "条。");
				} else if (res == listPd.size()) {
					result.add("导入成功" + res + "条。");
				} else {
					result.add("没有导入信息。");
				}
				mv.addObject("result", result);
			}
		}
		PageData pd = new PageData();
		pd.put("baseType", "icCard");
		pd.put("myaction", "readExcel");
		mv.addObject("pd", pd);
		mv.setViewName("fromExcel");
		return mv;
	}
	/**
	 * 下载模版
	 */
	@RequestMapping(value="/downExcel")
	public void downExcel(HttpServletResponse response)throws Exception{

		FileDownload.fileDownload(response, PathUtil.getClasspath() + Const.FILEPATHFILE + "基础信息导入模版.xls", "基础信息导入模版.xls");

	}

	/**
	 * 分卡
	 */
	@RequestMapping("/distributeCard")
	public ModelAndView distributeCard() {
		ModelAndView mv = this.getModelAndView();
		PageData pd = this.getPageData();
		try {
			List<PageData> institutionList = institutionService.getInstitutionList();
			mv.addObject("institutionList", institutionList);
		} catch (Exception e) {
			logger.error(e.toString(),e);
		}
		mv.addObject("pd", pd);
		mv.addObject("msg", "edit");
		mv.setViewName("system/icCard/distributeCard");
		return mv;
	}

	/**
	 * 卡号验证
	 */
	@RequestMapping(value="/checkBeginCardNum", produces = "application/json; charset=UTF-8")
	@ResponseBody
	public String checkBeginCardNum() {
		PageData pd = this.getPageData();
		try {
			pd = icCardService.checkCardNum(pd.getString("cardNum"));
		} catch (Exception e) {
			logger.error(e.toString(),e);
		}
		return JSONObject.toJSONString(pd);
	}

	@RequestMapping(value="/checkEndCardNum", produces = "application/json; charset=UTF-8")
	@ResponseBody
	public String checkEndCardNum() {
		PageData pd = this.getPageData();
		try {
			PageData end = icCardService.checkCardNum(pd.getString("cardNumEnd"));
			if (end != null) {
				pd.put("cardType", end.get("cardType"));
				List<PageData> idList = icCardService.getCardCount(pd);
				pd.put("cardCount", idList.size());
				pd.put("serialNumEnd", end.get("serialNumEnd"));
				pd.put("count", icCardService.getUndiscribeCardCount(pd).size());
			}
		} catch (Exception e) {
			logger.error(e.toString(),e);
		}
		return JSONObject.toJSONString(pd);
	}

	/**
	 * 执行分卡
	 */
	@RequestMapping("/distribute")
	public ModelAndView distribute() {
		ModelAndView mv = this.getModelAndView();
		PageData pd = this.getPageData();
		try {
			User user = getCurrentUser();
			if (user.getUSER_ID() != null && !user.getUSER_ID().equals("")) {
				pd.put("assignPeopleId", user.getUSER_ID());
			}
			pd.put("assignTime", new Date());
			icCardService.saveCardRecord(pd);
			int cardType = 0;
			if (pd.getString("cardType").equals("学员卡")) {
				cardType = 1;
			}
			pd.put("cardType",cardType);
			List<PageData> idList = icCardService.getCardCount(pd);
			for (PageData id: idList) {
				id.put("cardStatus", 2);
				id.put("assignUserId", pd.get("assignPeopleId"));
				id.put("inscode", pd.get("inscode"));
				PageData record = icCardService.getLastCardRecord();
				id.put("recordId", record.get("RECORD_ID"));
				id.put("assignTime", new Date());
				icCardService.updateCardStatus(id);
			}
		} catch (Exception e) {
			logger.error(e.toString(),e);
		}
		mv.addObject("msg","success");
		mv.setViewName("save_result");
		return mv;
	}

	/**
	 * 统计
	 */
	@RequestMapping("/statistics")
	public ModelAndView statistics(Page page) {
		ModelAndView mv = this.getModelAndView();
		PageData pd = this.getPageData();
		page.setPd(pd);
		try {
			List<PageData> recordList = icCardService.getInstitutionForRecordlistPage(page);
			mv.addObject("recordList", recordList);
			List<PageData> institutionList = institutionService.getInstitutionList();
			mv.addObject("institutionList", institutionList);
			PageData cardInfo = icCardService.getICCardInfo();
			mv.addObject("cardInfo",cardInfo);
		} catch (Exception e) {
			logger.error(e.toString(),e);
		}
		mv.addObject("pd", pd);
		mv.addObject("msg", "edit");
		mv.setViewName("system/icCard/statistics");
		return mv;
	}

	/**
	 * 去修改页面
	 */
	@RequestMapping("/goEdit")
	public ModelAndView goEdit() {
		ModelAndView mv = this.getModelAndView();
		PageData pd = this.getPageData();
		try {
			pd = trainTimeService.getTrainTime(pd);
			mv.addObject("pd", pd);
			mv.addObject("msg", "edit");
			mv.setViewName("resource/trainTime/trainTime_edit");
		} catch (Exception e) {
			logger.error(e.toString(),e);
		}
		return mv;
	}

	/**
	 * 修改
	 */
	@RequestMapping(value="/edit")
	public ModelAndView edit() throws Exception{
		if(!Jurisdiction.buttonJurisdiction(menuUrl, "edit")){return null;} //校验权限
		ModelAndView mv = this.getModelAndView();
		PageData pd = this.getPageData();
		try {
			trainTimeService.editTrainTime(pd);
			mv.addObject("msg","success");
			mv.setViewName("save_result");
		} catch (Exception e) {
			logger.error(e.toString(),e);
		}
		return mv;
	}

	/**
	 * 重置卡
	 */
	@RequestMapping(value="/reset",produces = "application/json; charset=UTF-8")
	@ResponseBody
	public String reset() throws Exception{
		PageData pd = this.getPageData();
		String cardNum=null;
		try {
			if (pd.get("coachType").equals("")){
				icCardService.updateStuMakeCard(pd);
			} else{
				icCardService.updateCoachMakeCard(pd);
			}
			icCardService.ResetCardStatus(pd);
		} catch (Exception e) {
			logger.error(e.toString(),e);
		}
		return cardNum;
	}
	
	/**
	 * 转移卡
	 */
	@RequestMapping(value="/transferCard")
	public ModelAndView transferCard() throws Exception{
		ModelAndView mv = this.getModelAndView();
		PageData pd = this.getPageData();
		try {
			List<PageData> institutionList = institutionService.getInstitutionList();
			mv.addObject("institutionList", institutionList);
		} catch (Exception e) {
			logger.error(e.toString(),e);
		}
		mv.addObject("pd", pd);
		mv.addObject("msg", "edit");
		mv.setViewName("system/icCard/transferCard");
		return mv;
	}
	
	/**
	 * 转移卡卡号验证
	 */
	@RequestMapping(value="/checkTransferCardNum", produces = "application/json; charset=UTF-8")
	@ResponseBody
	public String checkTransferCardNum() {
		PageData pd = this.getPageData();
		PageData resultPD = new PageData();
		try {
			//判断范围内的卡是否属于所选驾校
			List<PageData> isSameInscodeList = icCardService.checkCardNumIsSameInscode(pd);
			if(isSameInscodeList.size() > 0){//有不属于所选驾校的卡
				resultPD.put("msg", "1");
				return JSONObject.toJSONString(resultPD);
			}
			//验证范围内卡号是否有已制卡成功的
			List<PageData> isMakeList = icCardService.checkCardNumIsMake(pd);
			if(isMakeList.size() > 0){//有已制卡成功的卡
				resultPD.put("msg", "2");
				return JSONObject.toJSONString(resultPD);
			}
			//验证范围内卡号是否有已被绑定的
			List<PageData> isBindList = icCardService.checkCardNumIsBind(pd);
			if(isBindList.size() > 0){//有已被绑定的卡
				resultPD.put("msg", "3");
				return JSONObject.toJSONString(resultPD);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return JSONObject.toJSONString(resultPD);
	}
	
	/**
	 * 转移卡操作
	 */
	@RequestMapping("/transfer")
	public ModelAndView transfer() {
		ModelAndView mv = this.getModelAndView();
		PageData pd = this.getPageData();
		try {
			icCardService.updateCardStatusByCardNum(pd);
			mv.addObject("msg","success");
		} catch (Exception e) {
			logger.error(e.toString(),e);
		}
		mv.setViewName("save_result");
		return mv;
	}
	/**
	 * 销卡
	 */
	@RequestMapping("/goClearCard")
	public ModelAndView goClearCard() {
		ModelAndView mv = this.getModelAndView();
		PageData pd = this.getPageData();
		mv.addObject("pd", pd);
		mv.setViewName("system/icCard/clearCard");
		return mv;
	}
	@RequestMapping("/clearCard")
	@ResponseBody
	//同步卡状态到卡管理平台
  	public String clearCard(){
		PageData resultPD = new PageData();
		PageData pd = this.getPageData();
		String result="";
  		try {
  			//1.查询状态为已分配和已制卡的卡
  			List<PageData> cardList = icCardService.getCardCountByCardNum(pd);
  			List<PageData>clearCardList=new ArrayList<PageData>();
  			if(null != cardList && cardList.size() > 0){
  					//更新计时平台卡同步状态
				for(PageData card : cardList){
					if(Integer.valueOf(card.get("status").toString()) == 4){
						result+="卡号："+card.getString("cardNum")+" ";
					}else{
						//销卡标识
						card.put("status", 1);
						clearCardList.add(card);
					}
				}
  				
  			}
  			if(!StringUtil.isEmpty(result)){
  				result+="已制卡，不能销卡！";
  			}
  			if(clearCardList.size()>0){
  				//调用接口更改卡管理平台中卡的状态为已初始化状态
  				int code=icCardService.callChangeCardStatusInterface(clearCardList);
  				if(code==0){
  				   //删除要消除的卡
  				   icCardService.deleteCardBySerialNum(clearCardList);
  				}
  			}else{
  				result+="系统中没有此号段信息！";
  			}
  		} catch (Exception e) {
  			e.printStackTrace();
  		}
  		resultPD.put("msg", result);
  		
		return JSONObject.toJSONString(resultPD);	
  	}
}
