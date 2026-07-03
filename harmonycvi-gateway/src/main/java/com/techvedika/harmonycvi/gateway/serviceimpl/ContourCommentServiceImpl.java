package com.techvedika.harmonycvi.gateway.serviceimpl;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.techvedika.harmonycvi.gateway.constant.CommonConstants;
import com.techvedika.harmonycvi.gateway.constant.StatusConstants;
import com.techvedika.harmonycvi.gateway.entity.ContourComment;
import com.techvedika.harmonycvi.gateway.repository.ContourCommentRepository;
import com.techvedika.harmonycvi.gateway.service.CommonMethod;
import com.techvedika.harmonycvi.gateway.service.ContourCommentService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ContourCommentServiceImpl implements ContourCommentService {
	
	private static final Logger LOG = LoggerFactory
			.getLogger(BookmarkServiceImpl.class);
	
	    
	@Autowired
	private CommonMethod commonMethod;
	
	@Autowired
	private ContourCommentRepository contourCommentRepo;

	@Override
	@Transactional
	public JSONObject saveComment(JSONObject request) {
		LOG.info("Start of "+this.getClass().getName()+".saveComment");
		JSONObject response = new JSONObject();
		if(request!=null && request.containsKey(CommonConstants.ACCESS_KEY)
				&& request.containsKey(CommonConstants.STUDY_ID)
				&& request.containsKey(CommonConstants.CONTOUR_COMMENT)) {
			LinkedHashMap contourComment = (LinkedHashMap) request.get(CommonConstants.CONTOUR_COMMENT);
			String studyId = request.get(CommonConstants.STUDY_ID).toString();
			
			List<ContourComment> smObjectList = null;
			smObjectList = contourCommentRepo.findByStudyId(studyId);
			
			if (smObjectList == null || smObjectList.size() == 0) {
				ContourComment cc = new ContourComment();
				cc.setStudyId(studyId);
				JSONObject json = new JSONObject(contourComment);
				cc.setComment(json.toJSONString());
				cc.setCreatedTime(new Date());
				cc.setUpdatedTime(new Date());
				contourCommentRepo.save(cc);
			} else {
				ContourComment cc = smObjectList.get(0);
				JSONObject json = new JSONObject(contourComment);
				cc.setComment(json.toJSONString());
				cc.setUpdatedTime(new Date());
				contourCommentRepo.save(cc);
			}
			LOG.info("End of "+this.getClass().getName()+".saveComment " + StatusConstants.SUCCESS);
			return response;
		}else {
			response=commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE,
					StatusConstants.BAD_REQUEST);
			LOG.info("End of "+this.getClass().getName()+".saveComment " + StatusConstants.BAD_REQUEST_CODE);
			return response;
		}
	}

}
