package com.sxdh.driverTrain.service.system.icCard;

import com.sxdh.driverTrain.dao.DaoSupport;
import com.sxdh.driverTrain.entity.Page;
import com.sxdh.driverTrain.entity.card.StudentRegisterModel;
import com.sxdh.driverTrain.service.BaseService;
import com.sxdh.driverTrain.service.system.log.MethodLog;
import com.sxdh.driverTrain.util.Global;
import com.sxdh.driverTrain.util.HttpUtil;
import com.sxdh.driverTrain.util.PageData;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.*;

@Service("icCardService")
public class ICCardService extends BaseService {
	@Resource(name="daoSupport")
	private DaoSupport dao;

	/**
	 * 分页查询列表
	 */
	public List<PageData> getICCardlistPage(Page page) throws Exception{
		return (List<PageData>) dao.findForList("ICCardMapper.getICCardlistPage", page);
	}

	/**
	 * 导入验证
	 */
	public PageData checkNumber(PageData pd) throws Exception {
		return (PageData) dao.findForObject("ICCardMapper.checkNumber", pd);
	}

	public PageData checkSerialNum(PageData pd) throws Exception {
		return (PageData) dao.findForObject("ICCardMapper.checkSerialNum", pd);
	}
	public int checkSerialNum4Student(PageData pd) throws Exception {
		return (Integer) dao.findForObject("ICCardMapper.checkSerialNum4Student", pd);
	}

	/*
	 * 从excel导入
	 */
	@MethodLog(remark = "导入IC卡")
	public Map<String,Object> fromExcel(List<PageData> listPd, String importUserId)throws Exception{
		Map<String,Object> returnMap=new HashMap<String,Object>();
		List<String> result = new ArrayList<String>();
		Integer res = 0;
		for(int i=0;i<listPd.size();i++){
			PageData pd = new PageData();
			//卡号
			String cardNum = listPd.get(i).getString("var0").trim();
			pd.put("cardNum", cardNum);
			if(cardNum != null &&!"".equals(cardNum)){
//				if(cardNum.length()!= 16){
//					result.add("第"+(i + 1) + "行记录“学员卡号”不正确。");
//					continue;
//				}
				if(this.checkNumber(pd) != null){
					result.add("第"+(i + 1) + "行记录“学员卡号”不正确，可能是由于该值已存在。");
					continue;
				}
			}else{
				result.add("第"+(i + 1) + "行记录“学员卡号”不正确，该值必填不能为空。");
				continue;
			}
			//卡内号
			String serialNum = listPd.get(i).getString("var1").trim();
			pd.put("serialNum", serialNum);
			if(serialNum != null &&!"".equals(serialNum)){
//                if(serialNum.length()!= 16){
//                    result.add("第"+(i + 1) + "行记录“学员内卡号”不正确。");
//                    continue;
//                }
				if(this.checkSerialNum(pd) != null){
					result.add("第"+(i + 1) + "行记录“学员内卡号”不正确，可能是由于该值已存在。");
					continue;
				}
			}else{
				result.add("第"+(i + 1) + "行记录“学员内卡号”不正确，该值必填不能为空。");
				continue;
			}
			String cardType = listPd.get(i).getString("var2").trim();
			if(cardType == null || "".equals(cardType)){
				result.add("第"+(i + 1) + "行记录“卡内类型”不正确，该值必填不能为空。");
				continue;
			}
			if (cardType.equals("教练卡")) {
				pd.put("cardType", 0);
			} else if (cardType.equals("学员卡")) {
				pd.put("cardType", 1);
			}
			pd.put("createTime", new Date());
			pd.put("enableTime", new Date());
			pd.put("importUserId", importUserId);
			dao.save("ICCardMapper.fromExcel", pd);
			res = res + 1;
		}
		returnMap.put("res", res);
		returnMap.put("returnType", "IC卡");
		returnMap.put("result", result);
		return  returnMap;
	}

	/**
	 * 增加
	 */
	public void saveicCard(PageData pd) throws Exception {
		dao.save("ICCardMapper.addicCard", pd);
	}

	/**
	 * 查询两个卡号之间的所有卡
	 */
	public List<PageData> getCardCount(PageData pd) throws Exception {
		return (List<PageData>) dao.findForList("ICCardMapper.getCardCount", pd);
	}
	
	/**
	 * 查询两个卡号之间的所有卡(转移卡)
	 */
	public List<PageData> getCardCountByCardId(PageData pd) throws Exception {
		return (List<PageData>) dao.findForList("ICCardMapper.getCardCountByCardId", pd);
	}

	/**
	 * 查询同类型卡的库存信息
	 */
	public List<PageData> getUndiscribeCardCount(PageData pd) throws Exception {
		return (List<PageData>) dao.findForList("ICCardMapper.getUndiscribeCardCount", pd);
	}

	/**
	 * 根据卡号查询
	 */
	public PageData checkCardNum(String cardNum) throws Exception{
		return (PageData) dao.findForObject("ICCardMapper.checkCardNum", cardNum);
	}

	/**
	 * 添加分卡记录
	 */
	public void saveCardRecord(PageData pd) throws Exception {
		dao.save("ICCardMapper.addCardRecord", pd);
	}

	/**
	 * 查询分卡记录
	 */
	public PageData getLastCardRecord() throws Exception {
		return (PageData) dao.findForObject("ICCardMapper.getLastCardRecord",null);
	}

	/**
	 * 更新分卡记录
	 */
	public void updateCardStatus(PageData pd) throws Exception {
		dao.update("ICCardMapper.updateCardStatus", pd);
	}

	/**
	 * 查询分卡的驾培机构
	 */
	public List<PageData> getInstitutionForRecordlistPage(Page page) throws Exception {
		return (List<PageData>) dao.findForList("ICCardMapper.getInstitutionForRecordlistPage", page);
	}

	/**
	 * 制卡、补卡
	 */
	public PageData validStuInfoForCard(PageData pd) throws Exception {
		return (PageData) dao.findForObject("ICCardMapper.validStuInfoForCard", pd);
	}
	public PageData validCoachInfoForCard(PageData pd) throws Exception {
		return (PageData) dao.findForObject("ICCardMapper.validCoachInfoForCard", pd);
	}

	/**
	 * 更新卡信息
	 */
	public void updateStuInfoForMakeCard(PageData pd) throws Exception {
		dao.update("ICCardMapper.updateStuInfoForMakeCard", pd);
	}
	public void updateCoachInfoForMakeCard(PageData pd) throws Exception {
		dao.update("ICCardMapper.updateCoachInfoForMakeCard", pd);
	}
	public void updateCardStatusAfterMake(PageData pd) throws Exception {
		dao.update("ICCardMapper.updateCardStatusAfterMake", pd);
	}
	
	/**
	 * 转移卡更新卡信息
	 */
	public void updateCardStatusByCardNum(PageData pd) throws Exception {
		dao.update("ICCardMapper.updateCardStatusByCardNum", pd);
	}

	public List<PageData> findStudentByPd(PageData pd) throws Exception {
		return (List<PageData>)dao.findForList("ICCardMapper.findStudentByPd", pd);
	}


	/**
	 * 根据卡号查询信息
	 */
	public PageData getStuInfoWithCardNo(PageData pd) throws Exception {
		return (PageData) dao.findForObject("ICCardMapper.getStuInfoWithCardNo", pd);
	}
	public PageData getCoachInfoWithCardNo(PageData pd) throws Exception {
		return (PageData) dao.findForObject("ICCardMapper.getCoachInfoWithCardNo", pd);
	}

	//添加学时信息
	public void saveStudentLoginRecord(PageData pd)  throws Exception {
		dao.save("ICCardMapper.addStudentLoginRecord", pd);
	}
	public void saveCoachLoginRecord(PageData pd)  throws Exception {
		dao.save("ICCardMapper.addCoachLoginRecord", pd);
	}

	/**
	 * 查询学员的培训车型
	 */
	public PageData getStudentTrainType(PageData pd)  throws Exception {
		return (PageData) dao.findForObject("ICCardMapper.getStudentTrainType", pd);
	}
	/**
	 * 查询log_code和class_id
	 */
	public PageData getInfoFromLoginRecord(PageData pd)  throws Exception {
		return (PageData) dao.findForObject("ICCardMapper.getInfoFromLoginRecord", pd);
	}

	public Integer getClassId()  throws Exception {
		return (Integer) dao.findForObject("ICCardMapper.getClassId", null);
	}

	public PageData getStudentStudyInfo(PageData pd)  throws Exception {
		return (PageData) dao.findForObject("ICCardMapper.getStudentStudyInfo", pd);
	}

	public PageData getLoginRecordToGetTime(PageData pd) throws java.lang.Exception {
		return (PageData) dao.findForObject("ICCardMapper.getLoginRecordToGetTime",pd);
	}

	public void updateStudentRecord(PageData pd) throws Exception{
		dao.update("ICCardMapper.updateStudentRecord",pd);
	}

	public PageData getICCardInfo() throws Exception {
		return (PageData) dao.findForObject("ICCardMapper.getICCardInfo", null);
	}
	/**
	 * 重置卡
	 */
	public void ResetCardStatus(PageData pd) throws Exception {
		dao.update("ICCardMapper.ResetCardStatus", pd);
	}
	public void updateStuMakeCard(PageData pd) throws Exception {
		dao.update("ICCardMapper.updateStuMakeCard", pd);
	}
	public void updateCoachMakeCard(PageData pd) throws Exception {
		dao.update("ICCardMapper.updateCoachMakeCard", pd);
	}
	public PageData getICCardbyNum(PageData pd) throws Exception {
		return (PageData) dao.findForObject("ICCardMapper.getICCardbyNum", pd);
	}
	
	 /*
     * 新增
     */
    public void saveRegister(StudentRegisterModel pd) throws Exception {
    	dao.save("ICCardMapper.saveRegister", pd);
    }
    
    /**
	 * 转移卡卡号验证
	 */
	public List<PageData> checkCardNumIsSameInscode(PageData pd) throws Exception{
		return (List<PageData>) dao.findForList("ICCardMapper.checkCardNumIsSameInscode", pd);
	}
	public List<PageData> checkCardNumIsMake(PageData pd) throws Exception{
		return (List<PageData>) dao.findForList("ICCardMapper.checkCardNumIsMake", pd);
	}
	public List<PageData> checkCardNumIsBind(PageData pd) throws Exception{
		return (List<PageData>) dao.findForList("ICCardMapper.checkCardNumIsBind", pd);
	}
	
	//查询状态为已分配和已制卡的卡
  	public List<PageData> findCardsByForCartPlat() throws Exception{
  		return (List<PageData>) dao.findForList("ICCardMapper.findCardsByForCartPlat", null);
  	}
  	public void updateCardSendStatus(PageData pd) throws Exception {
		dao.update("ICCardMapper.updateCardSendStatus", pd);
	}
  	public int callChangeCardStatusInterface(List<PageData> cardList){
		String url=Global.getCardplatAddr();
		int code=1;		
		//3.同步卡状态到卡管理平台
		if(null != cardList && cardList.size() > 0){
			JSONObject obj=HttpUtil.doPostArray(url, cardList);
			code = obj.getInt("errorcode");
	    }
		return code;
  	}
    //根据卡号查询
  	public List<PageData> getCardCountByCardNum(PageData pd) throws Exception{
  		return (List<PageData>) dao.findForList("ICCardMapper.getCardCountByCardNum", pd);
  	}
    //根据卡的内码号删除
  	public int deleteCardBySerialNum(List<PageData>list) throws Exception{
  		return (int)dao.delete("ICCardMapper.deleteCardBySerialNum", list);
  	}
	
}
