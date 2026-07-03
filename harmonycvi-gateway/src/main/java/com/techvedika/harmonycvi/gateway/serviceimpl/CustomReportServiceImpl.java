package com.techvedika.harmonycvi.gateway.serviceimpl;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.techvedika.harmonycvi.gateway.cloud.StoragePresignService;
import com.techvedika.harmonycvi.gateway.constant.CommonConstants;
import com.techvedika.harmonycvi.gateway.constant.StatusConstants;
import com.techvedika.harmonycvi.gateway.constant.UserConstants;
import com.techvedika.harmonycvi.gateway.repository.UserRepository;
import com.techvedika.harmonycvi.gateway.security.SecurityUtil;
import com.techvedika.harmonycvi.gateway.service.CommonMethod;
import com.techvedika.harmonycvi.gateway.service.CustomReportService;

@Service
public class CustomReportServiceImpl implements CustomReportService {
	private static final Logger LOG = LoggerFactory
			.getLogger(CustomReportServiceImpl.class);
	
	@Value("${report.bucket-name}")
    private String bucketName;
	private CommonMethod commonMethod;
		
	private UserRepository userRepo;
	
	private StoragePresignService storagePresignSerice;
	
	public CustomReportServiceImpl(CommonMethod commonMethod,
			StoragePresignService storagePresignSerice,UserRepository userRepo) {
		this.commonMethod = commonMethod;
		this.storagePresignSerice = storagePresignSerice;
		this.userRepo = userRepo;
	}
	
	@Override
	public JSONObject getCustomReport(JSONObject request) { 
		JSONObject response = new JSONObject();
		try {
			LOG.info("Create getReport call");
			String reportURL = "";
			String email = SecurityUtil.currentUserEmailId();
			Optional<Long> userIdopt = userRepo.findIdByEmail(email);
			if(userIdopt.isEmpty()) {
				response.put(StatusConstants.STATUS_CODE, StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE);
				response.put(CommonConstants.S3_PDF_LINK, reportURL);
				return response;
			}
			
			Long userId = userIdopt.get();
			if("true".equalsIgnoreCase(commonMethod.getReportToS3())) {
				String fileName = createFileName(request,userId);
				reportURL = storagePresignSerice.getReportPresignedUrl(fileName,bucketName);
				if(reportURL.isEmpty()) {
					fileName = createFileNameWithoutUser(request);
					reportURL = storagePresignSerice.getReportPresignedUrl(fileName,bucketName);
				}
				LOG.info("reportUrl:{}",reportURL);
			} else {
				String orgId = (String) request.get(UserConstants.ORG_ID);
				String studyId = (String) request.get(UserConstants.STUDY_ID);
				
				// 1. Try user-specific path: orgId/userId/studyId/preview.pdf
				Path fullPath = Paths.get(commonMethod.getTargetPath(), orgId, userId.toString(), studyId, "preview.pdf");
				LOG.info("Checking local path: {}", fullPath);

				if (fullPath.toFile().exists()) {
					reportURL = commonMethod.getAccessPath() + orgId + "/" + userId + "/" + studyId + "/preview.pdf";
				} else {
					// 2. Fallback: Check study-specific path: orgId/studyId/preview.pdf
					Path fallbackPath = Paths.get(commonMethod.getTargetPath(), orgId, studyId, "preview.pdf");
					LOG.info("Fallback checking local path: {}", fallbackPath);
					if (fallbackPath.toFile().exists()) {
						reportURL = commonMethod.getAccessPath() + orgId + "/" + studyId + "/preview.pdf";
					}
				}
				LOG.info("Final local reportURL: {}", reportURL);
			}
			if(!reportURL.isEmpty()) {
				response.put(StatusConstants.STATUS_CODE, StatusConstants.SUCCESS_CODE);
				response.put(CommonConstants.S3_PDF_LINK, reportURL);
			}else {
				response.put(StatusConstants.STATUS_CODE, StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE);
				response.put(CommonConstants.S3_PDF_LINK, reportURL);
			}
		} catch (Exception e) {
			LOG.error("Exception in reading request");
			e.printStackTrace();
		}
		return response;
	}
	
	private String createFileName(JSONObject request, Long userId) {
		String orgId = (String) request.get(UserConstants.ORG_ID);
		String studyId = (String) request.get(UserConstants.STUDY_ID);
		String fileName = String.join(
    	        "/",
    	        orgId,
    	        userId.toString(),
    	        studyId
    	);
		LOG.info("FolderName: {}", fileName);
		return fileName;
	}
	
	private String createFileNameLocal(JSONObject request, Long userId) {
		String orgId = (String) request.get(UserConstants.ORG_ID);
		String studyId = (String) request.get(UserConstants.STUDY_ID);
		String fileName = String.join("/", orgId, userId.toString(), studyId);
		LOG.info("FolderName: {}", fileName);
		return fileName;
	}
	
	private String createFileNameWithoutUser(JSONObject request) {
		String orgId = (String) request.get(UserConstants.ORG_ID);
		String studyId = (String) request.get(UserConstants.STUDY_ID);
		String fileName = String.join(
    	        "/",
    	        orgId,
    	        studyId
    	);
		LOG.info("FolderName:{} ", fileName);
		return fileName;
	}
	
}