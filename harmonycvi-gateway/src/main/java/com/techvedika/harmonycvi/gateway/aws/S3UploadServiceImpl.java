package com.techvedika.harmonycvi.gateway.aws;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.techvedika.harmonycvi.gateway.cloud.StoragePresignService;
import com.techvedika.harmonycvi.gateway.constant.StatusConstants;
import com.techvedika.harmonycvi.gateway.dto.UploadPart;
import com.techvedika.harmonycvi.gateway.entity.StudyExtension;
import com.techvedika.harmonycvi.gateway.entity.StudyUpload;
import com.techvedika.harmonycvi.gateway.repository.StudyUploadRepository;
import com.techvedika.harmonycvi.gateway.repository.UserRepository;
import com.techvedika.harmonycvi.gateway.security.SecurityUtil;
import com.techvedika.harmonycvi.gateway.service.CommonMethod;
import com.techvedika.harmonycvi.gateway.serviceimpl.StudyArchiveServiceImpl;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class S3UploadServiceImpl implements S3UploadService {
	
	private static final Logger LOG = LoggerFactory
			.getLogger(S3UploadServiceImpl.class);

	private static final String END_OF = "End of ";
	
	private static final String VARIABLE_HOLDER = "{}{}{}{}";

	private static final String UPDATE_TRANSFER_STATUS = ".updateTransferStatus ";

    @Value("${upload.to-s3}")
    private String uploadSwitch;

    @Value("${studyupload.bucket-name}")
    private String bucketName;
    
    @Autowired
	StudyArchiveServiceImpl studyArchiveServiceImpl;

    @Autowired
    private StoragePresignService storageService;

    @Autowired
    private StudyUploadRepository studyUploadRepository;
    
    @Autowired
	UserRepository userRepo;

    @Autowired
    private CommonMethod commonMethod;

    private static final long MULTIPART_THRESHOLD = 1;

    @Override
    public JSONObject generatePreSignedUrl(
            String directoryName,
            String fileName,
            Integer totalParts,
            Long userId,
            Long orgId
    ) {

        JSONObject response = new JSONObject();

        if (!"true".equalsIgnoreCase(uploadSwitch)) {
            response.put("uploadtos3", false);
            return response;
        }

        if (directoryName == null || fileName == null ||
                userId == null || orgId == null) {

            return commonMethod.createResponse(
                    StatusConstants.BAD_REQUEST_CODE,
                    StatusConstants.BAD_REQUEST
            );
        }

        String objectKey = directoryName + "/" + fileName;

        try {

            if (totalParts <= MULTIPART_THRESHOLD) {

                String url = storageService.generateSingleUploadUrl(
                		bucketName,
                        objectKey,
                        "application/octet-stream",
                        20
                );

                response.put("uploadType", "SINGLE");
                response.put("preSignedUrl", url);

            } else {

                JSONObject multipart =
                        storageService.generateMultipartUploadUrls(
                        		bucketName,
                                objectKey,
                                totalParts,
                                60
                        );

                multipart.put("uploadType", "MULTIPART");
                response = multipart;
            }

            StudyUpload upload =
                    createNewRecord(directoryName, fileName, userId, orgId);

            if (upload == null) {
                return commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
            }

            response.put("id", upload.getPk());
            response.put("statusCode", StatusConstants.SUCCESS_CODE);
            response.put("uploadtos3", true);

            return response;

        } catch (Exception e) {
            return commonMethod.createResponse(
                    StatusConstants.OPERATION_FAILED,
                    StatusConstants.SERVER_ERROR
            );
        }
    }
    
    @Override
    public JSONObject uploadingtoS3Completed(JSONObject request) {

        try {

            Long pk = Long.valueOf(request.get("id").toString());
            String uploadId = (String) request.get("uploadId");
            String objectKey = (String) request.get("objectKey");

            JSONArray partsJson = (JSONArray) request.get("parts");

            List<UploadPart> parts = new ArrayList<>();

            for (Object obj : partsJson) {
                JSONObject partJson = (JSONObject) obj;

                UploadPart part = new UploadPart(
                        Integer.parseInt(partJson.get("PartNumber").toString()),
                        partJson.get("ETag").toString().replace("\"", "")
                );

                parts.add(part);
            }

            storageService.completeMultipartUpload(
            		bucketName,
                    objectKey,
                    uploadId,
                    parts
            );

            studyUploadRepository.markUploaded(pk, true);

            return commonMethod.createResponse(
                    StatusConstants.SUCCESS_CODE,
                    "Upload completed"
            );

        } catch (Exception e) {
            return commonMethod.createResponse(
                    StatusConstants.OPERATION_FAILED,
                    StatusConstants.SERVER_ERROR
            );
        }
    }
    
    private StudyUpload createNewRecord(String directoryName,String fileName,Long userId,Long orgId) {
    	try {
	    	StudyUpload studyUpload = new StudyUpload();
	    	studyUpload.setCreatedDate(new Date());
	    	studyUpload.setStudyLocation(directoryName);
	    	studyUpload.setStudyFileName(fileName);
	    	studyUpload.setActive(true);
	    	studyUpload.setTransferred(false);
	    	studyUpload.setUploaded(false);
	    	studyUpload.setStudyId(null);
	    	studyUpload.setUserId(userId);
	    	studyUpload.setOrgId(orgId);
	    	//em.persist(studyUpload);
		    //em.flush();
	    	studyUploadRepository.save(studyUpload);
		    return studyUpload;
    	}catch(Exception e) {
    		LOG.error("Exception : " + e);
			LOG.info(e.getLocalizedMessage());
			LOG.info("{}{}.createNewRecordforstudyUpload {}", END_OF, this.getClass().getName(), StatusConstants.OPERATION_FAILED);
			return null;
    	}
    }
    
    @Override
	public JSONObject updateConfStudies(String studyId, Long userId, Long orgId) {
    	LOG.info("Start of "+this.getClass().getName()+".updateConfStudies");

		// TODO Auto-generated method stub
    	JSONObject response = new JSONObject();
    	try {
        	StudyExtension seObj = new StudyExtension();
        	seObj.setStudyInstanceUID(studyId);
        	List<StudyExtension> studiesList = new ArrayList<>();
        	studiesList.add(seObj);

        	
        	System.out.println("At start of uploading to S3");

	    	List<StudyExtension> archivedStudies = studyArchiveServiceImpl.uploadConfToS3(studiesList);
	    	
	    	System.out.println("Uploaded to S3");
    		
			String fileName =  studyId.concat(".zip");
	    	StudyUpload studyUpload = createNewRecord(studyId, fileName, userId, orgId);

    		
    		response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, "ConfStudyUpload updated");
		    LOG.info("End of {}.updateConfStudies {}", this.getClass().getName(), StatusConstants.SUCCESS);
		    return response;
    	}catch(Exception e) {
    		LOG.error("Exception : " + e);
			LOG.info(e.getLocalizedMessage());
			response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
			LOG.info("{}{}.updateConfStudies {}", END_OF, this.getClass().getName(), StatusConstants.OPERATION_FAILED);
			return response;
    	}
	}
    
    @Transactional
    public JSONObject updateTransferStatus(Long Id,String studyId,boolean isTransferred) {
    	LOG.info("Start of "+this.getClass().getName()+".updateTransferStatus");
    	JSONObject response = new JSONObject();
    	try {
    		
    		String userEmailId = SecurityUtil.currentUserEmailId();
    		
    		if (!userRepo.existsByEmail(userEmailId)) {
    		    response = commonMethod.createResponse(StatusConstants.UNAUTHORIZED,
    		                                           StatusConstants.INVALID_TOKEN);
    		    LOG.info(VARIABLE_HOLDER, END_OF, this.getClass().getName(), UPDATE_TRANSFER_STATUS, StatusConstants.UNAUTHORIZED);
    		    return response;
    		}

    		if(Id == null || studyId == null ) {
    			response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
				LOG.info(VARIABLE_HOLDER, END_OF, this.getClass().getName(), UPDATE_TRANSFER_STATUS, StatusConstants.BAD_REQUEST_CODE);
				return response;
    		}
    		
    		int updated = studyUploadRepository.updateTransferStatus(Id, isTransferred, studyId);

    		if (updated > 0) {
    		    response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, "StudyUpload updated");
    		    LOG.info("End of {}.updateTransferStatus {}", this.getClass().getName(), StatusConstants.SUCCESS);
    		    return response;
    		} else {
    		    response = commonMethod.createResponse(StatusConstants.EMPTY_RESULT, "No record found");
    		    LOG.info("End of {}.updateTransferStatus - No record found", this.getClass().getName());
    		    return response;
    		}
    			
    	}catch(Exception e) {
    		LOG.error("Exception : " + e);
			LOG.info(e.getLocalizedMessage());
			response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
			LOG.info(VARIABLE_HOLDER, END_OF, this.getClass().getName(), UPDATE_TRANSFER_STATUS, StatusConstants.OPERATION_FAILED);
			return response;
    	}
    }
    
}