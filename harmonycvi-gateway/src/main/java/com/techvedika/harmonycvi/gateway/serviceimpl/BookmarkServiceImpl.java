package com.techvedika.harmonycvi.gateway.serviceimpl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.techvedika.harmonycvi.gateway.constant.CommonConstants;
import com.techvedika.harmonycvi.gateway.constant.StatusConstants;
import com.techvedika.harmonycvi.gateway.constant.UserConstants;
import com.techvedika.harmonycvi.gateway.dto.BookmarkDTO;
import com.techvedika.harmonycvi.gateway.dto.BookmarkDetailsDTO;
import com.techvedika.harmonycvi.gateway.dto.BookmarkMapper;
import com.techvedika.harmonycvi.gateway.entity.Bookmarks;
import com.techvedika.harmonycvi.gateway.entity.SeriesMeasurements;
import com.techvedika.harmonycvi.gateway.entity.SeriesParameter;
import com.techvedika.harmonycvi.gateway.entity.StudyParameter;
import com.techvedika.harmonycvi.gateway.entity.StudyVolumeInfo;
import com.techvedika.harmonycvi.gateway.exception.RequestValidator;
import com.techvedika.harmonycvi.gateway.exception.ValidationResult;
import com.techvedika.harmonycvi.gateway.projection.PatientHeightWeightProjection;
import com.techvedika.harmonycvi.gateway.repository.*;
import com.techvedika.harmonycvi.gateway.repository.UserRepository;
import com.techvedika.harmonycvi.gateway.security.SecurityUtil;
import com.techvedika.harmonycvi.gateway.service.BookmarkService;
import com.techvedika.harmonycvi.gateway.service.CommonMethod;
import com.techvedika.harmonycvi.gateway.service.StudyParameterService;

import jakarta.servlet.http.HttpServletRequest;

@Service
@Transactional
public class BookmarkServiceImpl implements BookmarkService {

    //private final SecurityUtil securityUtil;

	private static final Logger LOG = LoggerFactory.getLogger(BookmarkServiceImpl.class);

	@Autowired
	private BookmarksRepository bookmarksRepo;

	@Autowired
	private SeriesMeasurementsRepository seriesMeasurementsRepo;

	@Autowired
	private StudyVolumeInfoRepository studyVolumeInfoRepo;

	@Autowired
	private StudyParameterRepository studyParameterRepo;

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private CommonMethod commonMethod;

	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	private StudyExtensionRepository studyExtensionRepo;
	
	@Autowired
	private StudyParameterService studyParameterService;
	
	@Autowired
	private SeriesParameterRepository seriesParameterRepo;
	
	@Autowired
	private ContourCommentRepository contourCommentRepo;

	@Override
	public JSONObject save(JSONObject jsonRequest) {
		LOG.info("Start – BookmarkServiceImpl.save");
		JSONObject response = new JSONObject();

		try {
			
			ValidationResult validationResult = RequestValidator.validateRequestWithDetails(jsonRequest, CommonConstants.ACCESS_KEY,
					CommonConstants.STUDY_ID,CommonConstants.USER_ID,CommonConstants.NAME);
			if(!validationResult.isValid()) {
				return commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
			}

			if (!CommonConstants.SECURITY_TOKEN.equals(jsonRequest.get(CommonConstants.ACCESS_KEY).toString())) {

				return commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
			}
			
			//Bookmarks bookmark = createBookmark(jsonRequest);
			
			
			Bookmarks bookmark = null;

			Object selectedBookmarkObj = jsonRequest.get(CommonConstants.SELECTED_BOOKMARK);

			if (selectedBookmarkObj != null && !selectedBookmarkObj.toString().isBlank()) {


			    Long bookmarkId = Long.parseLong(selectedBookmarkObj.toString());

			    // Try to fetch existing bookmark
			    Optional<Bookmarks> optionalBookmark = bookmarksRepo.findById(bookmarkId);

			    if (optionalBookmark.isPresent()) {
			        bookmark = optionalBookmark.get();
			    } else {
			        bookmark = createBookmark(jsonRequest);
			    }
			} else {
			    bookmark = createBookmark(jsonRequest);    
			} 
			Object dataObj = jsonRequest.get(CommonConstants.DATA);
			
			if (dataObj instanceof LinkedHashMap<?, ?> dataMap) {
								
				@SuppressWarnings("unchecked")
				List<LinkedHashMap<String, Object>> contourData = (List<LinkedHashMap<String, Object>>) dataMap.get(CommonConstants.CONTOUR_DATA);

				if (contourData != null && !contourData.isEmpty()) {
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

					List<SeriesMeasurements> entities = new ArrayList<>();

					for (LinkedHashMap<String, Object> seriesInstance : contourData) {

						/* all mandatory keys present? */
						var common = (LinkedHashMap<String, Object>) seriesInstance.get(CommonConstants.COMMON_DATA);
						ArrayList<?> instArr = (ArrayList<?>) seriesInstance.get(CommonConstants.INSTANCE_ARRAY);
						Object studyUID = common != null ? common.get(CommonConstants.STUDY_INSTANCE_UID) : null;
						Object seriesUID = seriesInstance.get(CommonConstants.SERIES_INSTANCE_UID);

						System.out.println("seriesUID::"+seriesUID+" "+studyUID);
						if (studyUID == null || seriesUID == null || studyUID.toString().isBlank() || seriesUID.toString().isBlank() || instArr == null || instArr.isEmpty()) {
							/* skip this record but keep processing the rest */
							continue;
						}

						/* massage common‑data with helper util */
						common = commonMethod.createCommonDataJson(common);

						/* build entity */
//						SeriesMeasurements sm = new SeriesMeasurements();
//						sm = commonMethod.setSeriesMeasurementsObjectForFreeHand(sm, new ArrayList<>(), // measurementArrayList // (legacy arg)
//								seriesUID.toString(), jsonRequest, common);
//
//						String now = df.format(new Date());
//						sm.setCreationDate(now);
//						sm.setLastUpdatedDate(now);
//						sm.setInstanceArray(instArr);
//						sm.setBookmark(bookmark);
//						sm.setVersion(bookmark.getVersion());
//
//						entities.add(sm);
						
						String now = df.format(new Date());

						// Check if existing entry is already present
						Optional<SeriesMeasurements> existing =
						        seriesMeasurementsRepo.findFirstByStudyIdAndSeriesIdAndBookmarkId(
						                studyUID.toString(),
						                seriesUID.toString(),
						                bookmark.getId()
						        );

						SeriesMeasurements sm;

						if (existing.isPresent()) {
						    sm = existing.get();
						    sm.setLastUpdatedDate(now);
						    sm.setInstanceArray(instArr);
						} else {
						    sm = new SeriesMeasurements();
						    sm = commonMethod.setSeriesMeasurementsObjectForFreeHand(
						            sm,
						            new ArrayList<>(),
						            seriesUID.toString(),
						            jsonRequest,
						            common
						    ); 
						    sm.setCreationDate(now);
						    sm.setBookmark(bookmark);
						    sm.setVersion(bookmark.getVersion());
						    sm.setInstanceArray(instArr);
						}

						entities.add(sm);

					}

					/* bulk‑persist */
					if (!entities.isEmpty()) {
						System.out.println("entities not empty");
						seriesMeasurementsRepo.saveAll(entities);
					}
				}

				/* ---------- study‑ & series‑level parameters ---------- */
				if (dataMap.containsKey(CommonConstants.PARAMETER)) {
					@SuppressWarnings("unchecked")
					var paramMap = (LinkedHashMap<String, Object>) dataMap.get(CommonConstants.PARAMETER);

					if (paramMap.containsKey(CommonConstants.SA_PARAMETER)) {
						StudyParameter sp = new StudyParameter();
						sp.setCreatedTime(new Date());
						sp.setUpdatedTime(new Date());
						sp.setStudyId(jsonRequest.get(CommonConstants.STUDY_ID).toString());
						sp.setBookmark(bookmark);
						sp.setVersion(bookmark.getVersion());
						studyParameterRepo.save(sp);
					}
				}

				/* ---------- volume information ---------- */
//				if (dataMap.containsKey(CommonConstants.VOLUME_INFO)) {
//					@SuppressWarnings("unchecked")
//					List<Object> volInfo = (List<Object>) dataMap.get(CommonConstants.VOLUME_INFO);
//System.out.println("volume info iam getting is "+volInfo); 
//					if (volInfo != null) {
//						StudyVolumeInfo svi = new StudyVolumeInfo();
//						svi.setStudyId(jsonRequest.get(CommonConstants.STUDY_ID).toString());
//						svi.setEndVolume(volInfo);
//						svi.setBookmark(bookmark);
//						svi.setVersion(bookmark.getVersion());
//						studyVolumeInfoRepo.save(svi);
//					}
//				}
				
				if (dataMap.containsKey(CommonConstants.VOLUME_INFO)) {
				    @SuppressWarnings("unchecked")
				    List<Object> volInfo = (List<Object>) dataMap.get(CommonConstants.VOLUME_INFO);
				    if (volInfo != null) {
				        Long bookmarkId = bookmark.getId();
				        String studyId = jsonRequest.get(CommonConstants.STUDY_ID).toString();
				        int version = bookmark.getVersion();

				        Optional<StudyVolumeInfo> existing = studyVolumeInfoRepo.findFirstByBookmarkId(bookmarkId);

				        if (existing.isPresent()) {

				            studyVolumeInfoRepo.updateByBookmarkId(  bookmarkId, studyId, volInfo, version );
				                                    
				        } else {
				            StudyVolumeInfo svi = new StudyVolumeInfo();
				            svi.setStudyId(studyId);
				            svi.setEndVolume(volInfo);
				            svi.setBookmark(bookmark);
				            svi.setVersion(version); 
				            studyVolumeInfoRepo.save(svi);
				        }
				    }
				}
			} // end DATA‑section processing

			/*
			 * -----------------------------------------------------------------------
			 * success response
			 * ---------------------------------------------------------------------
			 */
			response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, StatusConstants.SAVED);
			response.put("bookmark", bookmark);
			LOG.info("End – BookmarkServiceImpl.save : SUCCESS");
			return response;

		} catch (Exception ex) {
			LOG.error("Bookmark save failed", ex);
			return commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
		}
	}
	

	synchronized private Bookmarks createBookmark(JSONObject jsonRequest) {
		LOG.info("Start of " + this.getClass().getName() + ".createBookmark");
		Bookmarks bookmark = new Bookmarks();
		bookmark.setDescription(jsonRequest.get(CommonConstants.DESCRIPTION).toString());
		long userId = Long.parseLong(jsonRequest.get(CommonConstants.USER_ID).toString());
		LinkedHashMap<String, Object> data = (LinkedHashMap<String, Object>) jsonRequest.get(CommonConstants.DATA);
		ArrayList<String> combinedSeries = null;

		LOG.info("before pullin COMBINED_SERIES_IDS ::: ");
		String combined_series_ids = "";
		if (data != null
				&& (data.containsKey(CommonConstants.COMBINED_SERIES_IDS) && data.get("combinedSeriesIds") != null)) {
			combinedSeries = (ArrayList<String>) data.get("combinedSeriesIds");
			for (String i : combinedSeries) {
				combined_series_ids = combined_series_ids.concat(String.valueOf(i));
				combined_series_ids = combined_series_ids.concat(",");
			}
		}

		if (combined_series_ids != "") {
			bookmark.setCombinedSeriesIds(combined_series_ids.substring(0, combined_series_ids.length() - 1));
		}
		bookmark.setUserId(userId);
		bookmark.setStudyInstanceUID(jsonRequest.get(CommonConstants.STUDY_ID).toString());

		// check existing versions
		int version = 0;
		version = bookmarksRepo.findLatestVersion(jsonRequest.get(CommonConstants.STUDY_ID).toString());
		version = version + 1;
		bookmark.setVersion(version);
		// String nameWithVersion = version + " " + userName ;
		// LOG.info("nameWithVersion::" + nameWithVersion);
		bookmark.setName(jsonRequest.get(CommonConstants.NAME).toString());
		bookmark.setIsArchive(0);
		bookmark.setIsPrivateBookmark(jsonRequest.get("isPrivateBookmark").toString());
		bookmark.setCreatedDt(new Date());
		bookmarksRepo.save(bookmark);
		LOG.info("End of " + this.getClass().getName() + ".createBookmark - " + bookmark.getId()
				+ StatusConstants.SUCCESS);
		return bookmark;
	}

	@Override
	@Transactional
	public JSONObject getList(Long userId, String studyId) {
		LOG.info("Start getList");
		JSONObject response = new JSONObject();
		try {

		if (studyId == null || studyId.isEmpty()) {
			response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
			LOG.info("End getList - BAD_REQUEST");
			return response;
		}

		JSONObject responseData = new JSONObject();
		List<Bookmarks> bookmarks = bookmarksRepo.findByStudyInstanceUIDAndIsArchiveOrderByVersionDesc(studyId, 0);

		if (bookmarks.isEmpty()) {
			response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, StatusConstants.EMPTY_RESULT);
			responseData.put(StatusConstants.DATA, new JSONObject());
			response.put("nextVersion", 1);
		} else {
			response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, StatusConstants.BOOKMARK_DATA);
			List<BookmarkDTO> dtoList = BookmarkMapper.toDTOList(bookmarks);
			responseData.put("bookmarks", dtoList);
			Integer version = bookmarksRepo.findLatestVersion(studyId);
			response.put("nextVersion", version + 1);
		}

		Optional<String> userName = userRepo.findFirstNameById(userId);
		userName.ifPresent(user -> responseData.put("userName", user));
		response.put(StatusConstants.DATA, responseData);
		LOG.info("End getList - SUCCESS");
		return response;
		}catch (Exception e) {
			LOG.error(e.getMessage());
			response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.EMPTY_RESULT);
			return response;
		}
	}
	
	public  List<JSONObject> pullDEParamsWithBookmark(String studyInstanceUID, Long id) {
        LOG.info("Start of {}.pullDEParamsWithBookmark", this.getClass().getName());

        List<SeriesParameter> deSeriesParams = seriesParameterRepo.findByStudyIdAndBookmarkIdAndType(studyInstanceUID, id, CommonConstants.DE);

        List<JSONObject> paramsList = new ArrayList<>();

        for (SeriesParameter series : deSeriesParams) {
            JSONParser parser = new JSONParser();
			JSONObject paramsObj = new JSONObject();
			try {
				paramsObj = (JSONObject) parser.parse(series.getParameterJson());
			} catch (ParseException e) {
				LOG.info("While parsing DE series Params:"+e.getLocalizedMessage());
			}
			
			JSONObject seriesObject = new JSONObject();
			JSONObject parameterWithUnits =  commonMethod.deParameterResponse((JSONObject)((JSONObject)paramsObj.get("parameter")).get("parameters"));
			((JSONObject) paramsObj.get("parameter")).put("parameters", parameterWithUnits);
			seriesObject.put(CommonConstants.PARAMETER, (JSONObject) paramsObj.get("parameter"));
			seriesObject.put(CommonConstants.TYPE, series.getType());
			seriesObject.put(CommonConstants.SERIES_ID, series.getSeriesId());
			paramsList.add(seriesObject);
        }

        LOG.info("End of {}.pullDEParamsWithBookmark {}", this.getClass().getName(), StatusConstants.SUCCESS);
        return paramsList;
    }
	
	public List<JSONObject> pullGLSParamsWithBookmark(String studyInstanceUID, Long id) {
		LOG.info("Start of "+this.getClass().getName()+".pullGLSParamsWithBookmark " + StatusConstants.SUCCESS);
		List<SeriesParameter> seriesParams = null;		
		seriesParams = seriesParameterRepo.findByStudyIdAndBookmarkIdAndType(studyInstanceUID, id, CommonConstants.GLS);
		
		if(seriesParams!=null && seriesParams.size()>0) {
			List<JSONObject> paramsList = new ArrayList<>();
			seriesParams.forEach(series->{
				JSONObject seriesObject = new JSONObject();
				seriesObject.put(CommonConstants.PARAMETER, series.getParameterJson());
				seriesObject.put(CommonConstants.GRAPH, series.getGraph());
				seriesObject.put(CommonConstants.SUMMARY, series.getSummary());
				seriesObject.put(CommonConstants.TYPE, series.getType());
				seriesObject.put(CommonConstants.SERIES_ID, series.getSeriesId());
				paramsList.add(seriesObject);
			});
			LOG.info("End of "+this.getClass().getName()+".pullGLSParamsWithBookmark -" + paramsList.size());
			return paramsList;			
		}
		LOG.info("End of "+this.getClass().getName()+".pullGLSParamsWithBookmark. Returning null");
		return null;
	}
	
	@Override
	@Transactional
	public ResponseEntity<JSONObject> getBookmarkDetailById(Long bookmarkId) {
	    LOG.info("Start of {}.getBookmarkDetailById", this.getClass().getName());
	    JSONObject response = new JSONObject();
	    JSONObject data = new JSONObject();

	    try {

	        // Retrieve bookmark details by ID
	        if (bookmarkId != 0) {
	        	
	        	List<BookmarkDetailsDTO> bookmarkOpt = bookmarksRepo.findBookmarksById(bookmarkId);
	        	
	        	BookmarkDetailsDTO bookmark  = bookmarkOpt.get(0);
	            
	            List<SeriesMeasurements> smObjectList = seriesMeasurementsRepo.findByBookmarkId(bookmarkId);

	            // Check if no series measurements are found
	            if (smObjectList == null || smObjectList.isEmpty()) {
	                response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, StatusConstants.EMPTY_RESULT);
	            } else {
	                response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, StatusConstants.BOOKMARK_DATA);
	                data.put("contourData", smObjectList);
	            }

	            // Populate bookmark details in response
	            if (bookmark != null) {
	                data.put("combinedSeriesIds", bookmark.getCombinedSeriesIds());
	                response.put("bookmark_id", bookmark.getId());
	                response.put("bookmark_name", bookmark.getName());
	            }

	            // Pull study volume info for this bookmark
	            JSONObject volumeInfo = pullStudyVolumnInfoWithBookmark(bookmark.getStudyInstanceUID(), bookmark.getId());
	            data.put(CommonConstants.VOLUME_INFO, volumeInfo);

	            JSONObject params = new JSONObject();

	            // Pull study parameters for this bookmark
	            JSONObject saParams = pullStudyParamsWithBookmark(bookmark.getStudyInstanceUID(), bookmark.getId());
	            if (saParams != null) {
	                params.put(CommonConstants.SA_PARAMETER, saParams);
	            }
	            
	            List<SeriesParameter> seriesParams = seriesParameterRepo.findByStudyIdAndBookmarkIdAndType(bookmark.getStudyInstanceUID(), bookmarkId, CommonConstants.QFLOW);
	            
	            if (seriesParams != null && !seriesParams.isEmpty()) {
	                List<JSONObject> paramsList = new ArrayList<>();
	                seriesParams.forEach(series -> {
	                    JSONObject seriesObject = new JSONObject();
	                    seriesObject.put(CommonConstants.PARAMETER, series.getParameterJson());
	                    seriesObject.put(CommonConstants.GRAPH, series.getGraph());
	                    seriesObject.put(CommonConstants.SUMMARY, series.getSummary());
	                    seriesObject.put(CommonConstants.TYPE, series.getType());
	                    paramsList.add(seriesObject);
	                });
	                params.put("qFlowParams", paramsList);
	            }

	            // Retrieve DE parameters
	            List<JSONObject> deParams = pullDEParamsWithBookmark(bookmark.getStudyInstanceUID(), bookmark.getId());
	            if (deParams != null) {
	                params.put("deParams", deParams);
	            }

	            // Retrieve GLS parameters
	            List<JSONObject> glsParams = pullGLSParamsWithBookmark(bookmark.getStudyInstanceUID(), bookmark.getId());
	            if (glsParams != null) {
	                params.put("glsParams", glsParams);
	            }
	          //ATRIAL params
				List<JSONObject> atrialParams = pullAtrialParamsWithBookmark(bookmark.getStudyInstanceUID(), bookmark.getId());
				if(atrialParams!=null) {
					params.put("atrialParams",atrialParams);
				}
	            List<SeriesParameter> t1SeriesParams = seriesParameterRepo.findByStudyIdAndBookmarkIdAndType(bookmark.getStudyInstanceUID(), bookmarkId, CommonConstants.T1Map);

	            if (t1SeriesParams != null && !t1SeriesParams.isEmpty()) {
	                List<JSONObject> paramsList = new ArrayList<>();
	                t1SeriesParams.forEach(series -> {
	                    JSONObject seriesObject = new JSONObject();
	                    seriesObject.put(CommonConstants.PARAMETER, series.getParameterJson());
	                    seriesObject.put(CommonConstants.GRAPH, series.getGraph());
	                    seriesObject.put(CommonConstants.SUMMARY, series.getSummary());
	                    seriesObject.put(CommonConstants.TYPE, series.getType());
	                    paramsList.add(seriesObject);
	                });
	                params.put("t1Params", paramsList);
	            }
	            
	            List<SeriesParameter> t2SeriesParams = seriesParameterRepo.findByStudyIdAndBookmarkIdAndType(bookmark.getStudyInstanceUID(), bookmarkId, CommonConstants.T2Map);

	            if (t2SeriesParams != null && !t2SeriesParams.isEmpty()) {
	                List<JSONObject> paramsList = new ArrayList<>();
	                t2SeriesParams.forEach(series -> {
	                    JSONObject seriesObject = new JSONObject();
	                    seriesObject.put(CommonConstants.PARAMETER, series.getParameterJson());
	                    seriesObject.put(CommonConstants.GRAPH, series.getGraph());
	                    seriesObject.put(CommonConstants.SUMMARY, series.getSummary());
	                    seriesObject.put(CommonConstants.TYPE, series.getType());
	                    paramsList.add(seriesObject);
	                });
	                params.put("t2Params", paramsList);
	            }

	            
	            List<SeriesParameter> ecvSeriesParams = seriesParameterRepo.findByStudyIdAndBookmarkIdAndType(bookmark.getStudyInstanceUID(), bookmarkId, CommonConstants.ECVMap);

	            if (ecvSeriesParams != null && !ecvSeriesParams.isEmpty()) {
	                List<JSONObject> paramsList = new ArrayList<>();
	                ecvSeriesParams.forEach(series -> {
	                    JSONObject seriesObject = new JSONObject();
	                    seriesObject.put(CommonConstants.PARAMETER, series.getParameterJson());
	                    seriesObject.put(CommonConstants.GRAPH, series.getGraph());
	                    seriesObject.put(CommonConstants.SUMMARY, series.getSummary());
	                    seriesObject.put(CommonConstants.TYPE, series.getType());
	                    paramsList.add(seriesObject);
	                });
	                params.put("ecvParams", paramsList);
	            }
	            
	            List<SeriesParameter> t2StarSeriesParams = seriesParameterRepo.findByStudyIdAndBookmarkIdAndType(bookmark.getStudyInstanceUID(), bookmarkId, CommonConstants.T2Star);

	            if (t2StarSeriesParams != null && !t2StarSeriesParams.isEmpty()) {
	                List<JSONObject> paramsList = new ArrayList<>();
	                t2StarSeriesParams.forEach(series -> {
	                    JSONObject seriesObject = new JSONObject();
	                    seriesObject.put(CommonConstants.PARAMETER, series.getParameterJson());
	                    seriesObject.put(CommonConstants.GRAPH, series.getGraph());
	                    seriesObject.put(CommonConstants.SUMMARY, series.getSummary());
	                    seriesObject.put(CommonConstants.TYPE, series.getType());
	                    paramsList.add(seriesObject);
	                });
	                params.put("t2*Params", paramsList);
	            }

	            data.put(CommonConstants.PARAMETER, params);
	            List<String> contourComment = null;
				contourComment= contourCommentRepo.findCommentByStudyId(bookmark.getStudyInstanceUID());
				
				if(contourComment!= null && contourComment.size()>0) {
					String cc = contourComment.get(0);
					data.put(CommonConstants.CONTOUR_COMMENT, cc);
				}
	            response.put(StatusConstants.DATA, data);

	            LOG.info("End of {}.getBookmarkDetailById - {}", this.getClass().getName(), StatusConstants.SUCCESS);
	            return ResponseEntity.status(HttpStatus.OK).body(response);

	        } else {
	            response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
	            LOG.info("End of {}.getBookmarkDetailById - {}", this.getClass().getName(), StatusConstants.BAD_REQUEST_CODE);
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	        }
	    } catch (Exception ex) {
	        LOG.error("Exception: ", ex);
	        response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
	        LOG.info("End of {}.getBookmarkDetailById - {}", this.getClass().getName(), StatusConstants.OPERATION_FAILED);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}


	@Override
	@Transactional
	public ResponseEntity<JSONObject> deleteBookmarkById(Long userId, Long bookmarkId) {
	    LOG.info("Start of {}.deleteBookmarkById", this.getClass().getName());
	    JSONObject response = new JSONObject();

	    try {

	        // Validate bookmark and user_id
	        if (bookmarkId != 0 && userId != 0) {
    			int maxRetries = 3;
    			int attempts = 0;
    			boolean updatedSuccessfully = false;
	        	
    			while (attempts < maxRetries && !updatedSuccessfully) {
    			    attempts++;
    			    Optional<Long> lockVersion = bookmarksRepo.findLockVersionById(bookmarkId);
    	        	if (lockVersion.isEmpty()) {
    			        LOG.warn("Bookmark not found while retrying delete, bookmarkId={}", bookmarkId);
    			        response = commonMethod.createResponse(StatusConstants.NOT_FOUND, StatusConstants.BOOKMARK_NOT_FOUND);
    	                LOG.info("End of {}.deleteBookmarkById - {}", this.getClass().getName(), StatusConstants.NOT_FOUND);
    	                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    			    }
    			    int updated = bookmarksRepo.updateArchiveByUserIdAndId(bookmarkId, userId, 1,lockVersion.get());
    				if (updated == 1) {
    			        updatedSuccessfully = true;
    			    } else {
    			        // Optional: wait a bit before retrying
    			        try {
    						Thread.sleep(100);
    					} catch (InterruptedException e) {
    						// TODO Auto-generated catch block
    						e.printStackTrace();
    					} // 100ms backoff
    			    }
    			}
    			if (!updatedSuccessfully) {
    			    LOG.info("End of " + this.getClass().getName() + ".updateStatusByStudyInstanceUID"
    			             + StatusConstants.OPERATION_FAILED);
    		        response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
    		        LOG.info("End of {}.deleteBookmarkById - {}", this.getClass().getName(), StatusConstants.OPERATION_FAILED);
    		        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    			}else {
    				response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, "Bookmark deleted");
	                LOG.info("End of {}.deleteBookmarkById - {}", this.getClass().getName(), StatusConstants.SUCCESS);
	                return ResponseEntity.status(HttpStatus.OK).body(response);
    			}
	    		
	        } else {
	            response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
	            LOG.info("End of {}.deleteBookmarkById - {}", this.getClass().getName(), StatusConstants.BAD_REQUEST_CODE);
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	        }
	    } catch (Exception e) {
	        LOG.error("Exception: ", e);
	        response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
	        LOG.info("End of {}.deleteBookmarkById - {}", this.getClass().getName(), StatusConstants.OPERATION_FAILED);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}


	@Override
    public void deleteBookmarkByStudyId(String studyUID) {
		if (studyUID!=null) {
			//bookmarkDao.deleteByStudyId(studyUID);
			bookmarksRepo.deleteByStudyInstanceUID(studyUID);
		}       
    }

	@Override
	@Transactional
	public ResponseEntity<JSONObject> getBookmarkByVersion(String studyId, boolean isPreprocess) {
	    LOG.info("Start of {}.getBookmarkByVersion", this.getClass().getName());
	    JSONObject response = new JSONObject();
	    int version = 0;

	    try {
	    	
	    	String email = SecurityUtil.currentUserEmailId();
	        
	        Optional<String> roleName = userRepo.findRoleNameByEmail(email);
	        if(roleName.isEmpty()) {
	        	response = commonMethod.createResponse(UserConstants.INVALID_USERID, UserConstants.UNAUTHORIZED);
	            LOG.info("End of {}.getBookmarkByVersion - {}", this.getClass().getName(), UserConstants.INVALID_USERID);
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	        }

	        // Determine version based on the preprocessing flag
	        if (!isPreprocess) {
	            version = bookmarksRepo.findLatestVersion(studyId);
	        } 
	        
	        Optional<Long> bookmarkId = bookmarksRepo.findBookmarksIdByStudyIdAndVersion(studyId, version);
	        if(bookmarkId.isEmpty()) {
	        	response = commonMethod.createResponse(UserConstants.EMPTY_RESULT, UserConstants.EMPTY_RESULT);
	            LOG.info("End of {}.getBookmarkByVersion - {}", this.getClass().getName(), UserConstants.SUCCESS_WITH_EMPTY_RESULT_CODE);
	            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
	        }

	        // Get bookmark details and append user role
	        ResponseEntity<JSONObject> bookmarkEntity = getBookmarkDetailById(bookmarkId.get());
	        
	        JSONObject bookmarkDetails = bookmarkEntity.getBody();
	        
	        bookmarkDetails.put("user_role", roleName.get());

	        LOG.info("End of {}.getBookmarkByVersion - {}", this.getClass().getName(), StatusConstants.SUCCESS);
	        return ResponseEntity.status(HttpStatus.OK).body(bookmarkDetails);

	    } catch (Exception e) {
	        LOG.error("Exception: ", e);
	        response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
	        LOG.info("End of {}.getBookmarkByVersion - {}", this.getClass().getName(), StatusConstants.OPERATION_FAILED);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}
	
	@Override
    @Transactional
    public synchronized Long getBookmarkId(String studyId, String bookmarkId, boolean shouldCreate) {
        LOG.info("Start of getBookmark");
        Long bookmark = null;
        try {
            if (bookmarkId != null) {
               bookmark = Long.parseLong(bookmarkId);
//                List<Bookmarks> bookmarksList = em.createNamedQuery(Bookmarks.FIND_BY_ID, Bookmarks.class)
//                        .setParameter("id", bookmarkIdValue)
//                        .getResultList();
               boolean bookmarkExists = bookmarksRepo.existsById(bookmark);
               if(bookmarkExists)
            	   return bookmark;
            }else {
            	Optional<Long> bookmarksList = bookmarksRepo.findBookmarksIdByStudyIdAndVersion(studyId,0);

                if (!bookmarksList.isEmpty()) {
                    bookmark = bookmarksList.get();
                    LOG.info("Fetched Preprocess bookmark with id: {}", bookmark);
                    return bookmark;
                }
            }
        }catch (Exception e) {
            LOG.error("Error in getBookmark", e);
            throw e;
        } finally {
            LOG.info("End of getBookmark");
        }
        return bookmark;
	}

	
	@Override
   @Transactional
    public synchronized Bookmarks getBookmark(String studyId, String bookmarkId, boolean shouldCreate) {
        LOG.info("Start of getBookmark");

        Bookmarks bookmark = null;

        try {
            if (bookmarkId != null) {
                long bookmarkIdValue = Long.parseLong(bookmarkId);
//                List<Bookmarks> bookmarksList = em.createNamedQuery(Bookmarks.FIND_BY_ID, Bookmarks.class)
//                        .setParameter("id", bookmarkIdValue)
//                        .getResultList();
                bookmark = bookmarksRepo.findById(bookmarkIdValue)
                	    .orElse(null);
            } else {
//                List<Bookmarks> bookmarksList = em.createNamedQuery(Bookmarks.FINDID_BY_STUDY_AND_VERSION, Bookmarks.class)
//                        .setParameter(CommonConstants.STUDY_ID, studyId)
//                        .setParameter("version", 0)
//                        .getResultList();
                
                List<Bookmarks> bookmarksList = bookmarksRepo.findBookmarksByStudyIdAndVersion(studyId,0);

                if (!bookmarksList.isEmpty()) {
                    bookmark = bookmarksList.get(0);
                    LOG.info("Fetched Preprocess bookmark with id: {}", bookmark.getId());
                }

                if (bookmark == null && shouldCreate) {
                    LOG.info("Creating new preprocess bookmark");

                    bookmark = new Bookmarks();
                    bookmark.setName("Preprocess");
                    bookmark.setVersion(0);
                    bookmark.setStudyInstanceUID(studyId);
                    bookmark.setUserId(CommonConstants.SUPER_ADMIN_ID);
                    bookmark.setDescription("Has AI processed results");
                    bookmark.setCreatedDt(new Date());

                    //em.persist(bookmark);
                    //em.flush();
                    bookmarksRepo.save(bookmark);

                    LOG.info("Created Preprocess bookmark with id: {}", bookmark.getId());
                }
            }

            LOG.info("Returning bookmark: {}", bookmark);
        } catch (Exception e) {
            LOG.error("Error in getBookmark", e);
            throw e;
        } finally {
            LOG.info("End of getBookmark");
        }

        return bookmark;
    }

	@Override
	@Transactional
	public ResponseEntity<JSONObject> converttoPreprocessBookmark(String studyId, Long bookmarkId) {
		
	    JSONObject response = new JSONObject();
	    
	    try {
	        // Check for null or empty studyId and bookmarkId
	        if (studyId == null || bookmarkId == null || studyId.isEmpty()) {
	            response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
	            LOG.info("End of {}.convertToPreprocessBookmark - {}", this.getClass().getName(), StatusConstants.BAD_REQUEST);
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	        }

	        // Archive old bookmarks and set preprocess bookmark
	        bookmarksRepo.archiveOldVersions(studyId, bookmarkId);
	        bookmarksRepo.markAsPreprocess(studyId, bookmarkId);

	        // Return success response
	        response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, StatusConstants.SAVED);
	        LOG.info("End of {}.convertToPreprocessBookmark - {}", this.getClass().getName(), StatusConstants.SUCCESS);
	        return ResponseEntity.status(HttpStatus.OK).body(response);

	    } catch (Exception e) {
	        LOG.error("Exception: ", e);
	        response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
	        LOG.info("End of {}.convertToPreprocessBookmark - {}", this.getClass().getName(), StatusConstants.OPERATION_FAILED);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}

	
	private JSONObject pullStudyVolumnInfoWithBookmark(String studyInstanceUID, Long bookmarkId) {
	    LOG.info("Start of {}.pullStudyVolumnInfoWithBookmark", this.getClass().getName());
	    JSONObject response = new JSONObject();

	    try {
	    	
	    	List<PatientHeightWeightProjection> patientHeightWeight = studyExtensionRepo.findHeightWeightByStudyId(studyInstanceUID);
	    	
	        Optional<List<Object>> studyVolumeList = studyVolumeInfoRepo.findEndVolumeByBookmarkId(bookmarkId);

	        if (patientHeightWeight.isEmpty() || studyVolumeList.isEmpty()) {
	            LOG.info("End of {}.pullStudyVolumnInfoWithBookmark - {}", this.getClass().getName(), StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE);
	            return response;
	        } else {
	        	
	        	List<Object> svi = studyVolumeList.get();

	            response.put(StatusConstants.STATUS_CODE, StatusConstants.SUCCESS_CODE);
	            response.put(CommonConstants.PATIENT_HEIGHT, patientHeightWeight.get(0).getPatientHeight());
	    	    response.put(CommonConstants.PATIENT_WEIGHT, patientHeightWeight.get(0).getPatientWeight());
	            response.put("end_volume_info", svi.isEmpty()? new ArrayList<Object>(): svi.get(0));

	            LOG.info("End of {}.pullStudyVolumnInfoWithBookmark - {}", this.getClass().getName(), StatusConstants.SUCCESS);
	            return response;
	        }
	    } catch (Exception e) {
	        LOG.error("Exception: ", e);
	        response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
	        LOG.info("End of {}.pullStudyVolumnInfoWithBookmark - {}", this.getClass().getName(), StatusConstants.OPERATION_FAILED);
	        return response;
	    }
	}
	
	@Transactional
	public JSONObject pullStudyParamsWithBookmark(String studyInstanceUID, Long id) throws ParseException {
	    LOG.info("Start of {}.pullStudyParamsWithBookmark", this.getClass().getName());
	    JSONObject response = new JSONObject();
	    List<StudyParameter> smObject = null;

	    try {
	        
	        smObject = studyParameterRepo.findByBookmarkId(id);

	        if (smObject == null || smObject.isEmpty()) {
	            LOG.info("End of {}.pullStudyParamsWithBookmark - {}", this.getClass().getName(), StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE);
	            return response;
	        } else {
	            response.putAll(studyParameterService.prepareResponse(smObject.get(0).getStudyId(), smObject.get(0).getParameterJson()));
	            response.put(CommonConstants.RADIAL_STRAIN, smObject.get(0).getRadialStrainJson());
	            response.put(CommonConstants.GRAPH, smObject.get(0).getGraph());
	            response.put(CommonConstants.SUMMARY, smObject.get(0).getSummary());
	            response.put(CommonConstants.COMPUTED_SERIES, smObject.get(0).getComputedSeries());
	            LOG.debug("Response: {}", response);
	            LOG.info("End of {}.pullStudyParamsWithBookmark - {}", this.getClass().getName(), StatusConstants.SUCCESS);
	            return response;
	        }
	    } catch (Exception e) {
	        LOG.error("Exception: ", e);
	        response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
	        LOG.info("End of {}.pullStudyParamsWithBookmark - {}", this.getClass().getName(), StatusConstants.OPERATION_FAILED);
	        return response;
	    }
	}
	
	public List<JSONObject> pullAtrialParamsWithBookmark(String studyInstanceUID, Long id) {
		LOG.info("Start of "+this.getClass().getName()+".pullAtrialParamsWithBookmark " + StatusConstants.SUCCESS);
		List<SeriesParameter> seriesParams = null;
		
		seriesParams = seriesParameterRepo.findByStudyIdAndBookmarkIdAndType(studyInstanceUID, id, CommonConstants.ATRIAL);
		
		if(seriesParams!=null && seriesParams.size()>0) {
			List<JSONObject> paramsList = new ArrayList<>();
			seriesParams.forEach(series->{
				JSONObject seriesObject = new JSONObject();
				seriesObject.put(CommonConstants.PARAMETER, series.getParameterJson());
				seriesObject.put(CommonConstants.GRAPH, series.getGraph());
				seriesObject.put(CommonConstants.SUMMARY, series.getSummary());
				seriesObject.put(CommonConstants.TYPE, series.getType());
				seriesObject.put(CommonConstants.SERIES_ID, series.getSeriesId());
				paramsList.add(seriesObject);
			});
			LOG.info("End of "+this.getClass().getName()+".pullAtrialParamsWithBookmark -" + paramsList.size());
			return paramsList;			
		}
		LOG.info("End of "+this.getClass().getName()+".pullAtrialParamsWithBookmark. Returning null");
		return null;
	}

	
}
