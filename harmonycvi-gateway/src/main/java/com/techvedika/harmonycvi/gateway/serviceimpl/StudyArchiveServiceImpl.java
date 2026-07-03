package com.techvedika.harmonycvi.gateway.serviceimpl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.techvedika.harmonycvi.gateway.cloud.StoragePresignService;
import com.techvedika.harmonycvi.gateway.constant.UserConstants;
import com.techvedika.harmonycvi.gateway.entity.ArchivedStudy;
import com.techvedika.harmonycvi.gateway.entity.LocationDao;
import com.techvedika.harmonycvi.gateway.entity.StudyExtension;
import com.techvedika.harmonycvi.gateway.pacsproxy.PacsProxyStudyService;
import com.techvedika.harmonycvi.gateway.repository.ArchivedStudyRepository;
import com.techvedika.harmonycvi.gateway.repository.OrganizationRepository;
import com.techvedika.harmonycvi.gateway.repository.StudyExtensionRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Service
@Transactional // Add at class level or ensure methods are public
public class StudyArchiveServiceImpl {
	
	private static final Logger LOG = LoggerFactory
			.getLogger(StudyArchiveServiceImpl.class);
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private LocationDao locationDao;
	 
	@Autowired
	private ArchivedStudyRepository archivedStudyRepo;
	
	@Autowired
	private StudyExtensionRepository studyExtensionRepo;
	
	@Autowired
	private OrganizationRepository organizationRepo;
	
	@Autowired
	private PacsProxyStudyService pacsProxyService;
	
	@Autowired
	AsyncDeleteServiceImpl asyncDeleteServiceImpl; 
	
	@Autowired
	StoragePresignService storagePresignService; 

	@Value("${archiving.study-life-time:30}")
	String studyLifeTime;

	@Value("${archiving.max-studies:20}")
	String studyCount;

	@Value("#{environment['app.data-dir'] + '/archive/'}")
	private String dataFolderPath;

	@Value("${archiving.device-name:fs1}")
	String archiveDeviceName;

	@Value("${archiving.to-s3}")
	String isArchiveToS3;

	@Value("${archiving.s3-bucket}")
	String bucketName;

	@Value("${studyupload.bucket-name}")
	String bucketNameAlt;

	@Value("#{environment['app.data-dir'] + '/archive/'}")
	String storagePath;
    
	
	public void archiveExpiredStudies() {
		try {
			System.gc();
			LOG.info("At start of archiving");
			getHeapMemoryDetails();
	        
			Integer studyLife = Integer.parseInt(studyLifeTime);
	    	LocalDateTime expiredDateTime = LocalDateTime.now().minusDays(studyLife);
	    	LocalDate expiredDate = expiredDateTime.toLocalDate();
	    	Date formattedDate = Date.from(expiredDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	    	LOG.info("Deleted studies uploaded before:" + formattedDate);
	    	Integer maxStudiesCount = Integer.parseInt(studyCount);
	    	List<StudyExtension> expiredStudiesList = studyExtensionRepo.findExpiredStudies(formattedDate,maxStudiesCount);
	    	if(expiredStudiesList == null || expiredStudiesList.size() == 0) {
	    		LOG.info("No expired studies found");
	    	}
	    	
	    	LOG.info("At start of uploading to S3");
			getHeapMemoryDetails();
	    	
			archiveStudies(expiredStudiesList);
		}catch(Exception e) {
			LOG.info("Exception while archiving studies:"+ e.getLocalizedMessage());
			e.printStackTrace();
		}    	
    }
	
	public void archiveStudies(List<StudyExtension> expiredStudiesList) {
		try {
			List<StudyExtension> archivedStudies = uploadToS3(expiredStudiesList);
	    	System.gc();
	    	LOG.info("After completion of uploading to S3");
			getHeapMemoryDetails();
	    		
			for (StudyExtension study : archivedStudies) {
				LOG.info("Study Id:" + study.getStudyInstanceUID());
				try {
					archiveStudy(study.getStudyInstanceUID());
				}catch(Exception e) {
					LOG.error("Exception while archiving study:" + study.getStudyInstanceUID());
					LOG.error("Exception :"+e.getLocalizedMessage());
					e.printStackTrace();
					continue;
				}				
			}    	
	    	
	    	LOG.info("Completed archiving expired studies");
	    	List<ArchivedStudy> archivingPendingStudies = archivedStudyRepo.findByStatus(1);
	    	if(archivingPendingStudies == null || archivingPendingStudies.size() == 0) {
	    		LOG.info("No studies with pending status");
	    	}
	    	
	    	for (ArchivedStudy study : archivingPendingStudies) {
	    		try {
	    			deleteImagesFromPACS(study);
	    		}catch(Exception e) {
	    			LOG.info("Excepion while deleting images from PACS" + study.getStudyId());
					continue;
	    		}
	    	}
		}catch(Exception e) {
			LOG.error("Error while archiving studies {}",e.getLocalizedMessage());
		}
	}

	private void getHeapMemoryDetails() {
		MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
		MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();	        
		long usedHeapMemory = heapMemoryUsage.getUsed();
		long maxHeapMemory = heapMemoryUsage.getMax();
		LOG.info("Used memory:"+ usedHeapMemory+ " Max memory:" + maxHeapMemory);
	}
	
	public List<StudyExtension> uploadToS3(List<StudyExtension> expiredStudiesList) {
		List<StudyExtension> archivedStudiesList = new ArrayList<StudyExtension>();
		for (StudyExtension study : expiredStudiesList) {
			String studyId = study.getStudyInstanceUID();
			LOG.info("Study Id archiving::"+studyId);
			if(studyId == null)
				continue;
			 String studyLocation = getLocation(studyId);
			 String fileName = studyId + "/" + studyId.concat(".zip");
			 LOG.info("fileName------------------"+fileName);
			 if(studyLocation != null) {
				 String fullLocation = "";
				 try {
					fullLocation = getStudyLocation(studyLocation);
					LOG.info("PACS location:"+ fullLocation + "\nS3 Location:" +  fileName);				
					LOG.info("Started archiving study:"+studyId);
					boolean zipUploaded = uploadZip(fullLocation, fileName);
					if(zipUploaded) {
						archivedStudiesList.add(study);
					}
				}catch(IOException e) {
					LOG.info("Exception with filepath :"+fullLocation + " " + e.getLocalizedMessage());
					archivedStudiesList.add(study);
				}catch(Exception exp) {
					LOG.info("Exception while getting or uploading images:");
					exp.printStackTrace();
				}
			 }
		}
		return archivedStudiesList;
	}


	@Transactional
	public void deleteImagesFromPACS(ArchivedStudy study) {
		try {
			String studyLocation = study.getStudyLocation();
			if(studyLocation == null) {
				return;
			}
			File file = new File(study.getStudyLocation());
			file.delete();
			study.setStatus(2);
			em.merge(study);
		}catch(Exception exp) {
			LOG.info("Exception while deleting study from path :"+exp.getLocalizedMessage());
		}
		LOG.info("Deleted images from PACS");
	}

//	public void archiveStudy(String studyId) {		    
//		if(saveArchivingDetails(studyId)) { 
//			LOG.info("Saved study details in DB");
//			String studyLocation = getLocation(studyId);
//			String fullLocation = getStudyLocation(studyLocation);
//			if(deleteStudyFromDB(studyId)) {
//				LOG.info("Deleted study details from DB");
//				try {
//					File file = new File(fullLocation);
//					file.delete();
//					List<ArchivedStudy> archivedStudy = archivedStudyRepo.findByStudyId(studyId);
//					ArchivedStudy archivedSt = archivedStudy!=null ?archivedStudy.get(0):new ArchivedStudy();
//					archivedSt.setStudyId(studyId);
//					archivedSt.setStatus(2);
//					archivedSt.setStudyLocation(fullLocation);
//					em.merge(archivedSt);
//				}catch(Exception exp) {
//					List<ArchivedStudy> archivedStudy = archivedStudyRepo.findByStudyId(studyId);
//					ArchivedStudy archivedSt = archivedStudy!=null ?archivedStudy.get(0):new ArchivedStudy();
//					archivedSt.setStudyId(studyId);
//					archivedSt.setStatus(1);
//					archivedSt.setStudyLocation(fullLocation);
//					em.merge(archivedSt);
//					LOG.info("Exception while deleting study from path :"+exp.getLocalizedMessage());
//				}
//				LOG.info("Deleted images from PACS");
//			}
//		}else {
//			LOG.info("Unable to archive or save Archiving details of study:"+ studyId);
//		}		
//	}
	
	@Transactional
	public void archiveStudy(String studyId) {		    
	    try {
	        if (!saveArchivingDetails(studyId)) { 
	            LOG.info("Unable to save archiving details for study: {}", studyId);
	            return;
	        }

	        LOG.info("Saved study details in DB for study: {}", studyId);
	        String studyLocation = getLocation(studyId);
	        
	        if (studyLocation == null || studyLocation.trim().isEmpty()) {
	            LOG.error("Study location is null or empty for study: {}", studyId);
	            updateArchivedStudyStatus(studyId, 1, "Location not found");
	            return;
	        }

	        String fullLocation = getStudyLocation(studyLocation);
	        
	        if (!deleteStudyFromDB(studyId)) {
	            LOG.info("Failed to delete study details from DB for study: {}", studyId);
	            updateArchivedStudyStatus(studyId, 1, "DB deletion failed");
	            return;
	        }

	        LOG.info("Deleted study details from DB for study: {}", studyId);
	        
	        // Delete file/directory
	        boolean fileDeleted = deleteStudyFiles(fullLocation);
	        
	        if (fileDeleted) {
	            LOG.info("Deleted images from PACS for study: {}", studyId);
	            updateArchivedStudyStatus(studyId, 2, fullLocation);
	        } else {
	            LOG.error("Failed to delete study files from path: {}", fullLocation);
	            updateArchivedStudyStatus(studyId, 1, fullLocation);
	        }
	        
	    } catch (Exception exp) {
	        LOG.error("Unexpected error during archiving study: {}", studyId, exp);
	        updateArchivedStudyStatus(studyId, 1, "Exception: " + exp.getMessage());
	    }
	}

	public boolean deleteStudyFiles(String filePath) {
	    if (filePath == null || filePath.trim().isEmpty()) {
	        return false;
	    }
	    
	    try {
	        File file = new File(filePath);
	        if (file.exists()) {
	            if (file.isDirectory()) {
	                // Delete directory recursively
	                return deleteDirectory(file);
	            } else {
	                return file.delete();
	            }
	        }
	        return false;
	    } catch (Exception e) {
	        LOG.error("Error deleting file: {}", filePath, e);
	        return false;
	    }
	}

	private boolean deleteDirectory(File directory) {
	    if (directory.isDirectory()) {
	        File[] files = directory.listFiles();
	        if (files != null) {
	            for (File file : files) {
	                deleteDirectory(file);
	            }
	        }
	    }
	    return directory.delete();
	}

	private void updateArchivedStudyStatus(String studyId, int status, String location) {
	    try {
	        List<ArchivedStudy> archivedStudies = archivedStudyRepo.findByStudyId(studyId);
	        ArchivedStudy archivedSt;
	        
	        if (archivedStudies != null && !archivedStudies.isEmpty()) {
	            archivedSt = archivedStudies.get(0);
	        } else {
	            archivedSt = new ArchivedStudy();
	            archivedSt.setStudyId(studyId);
	            archivedSt.setCreatedDate(new Date());
	        }
	        
	        archivedSt.setStatus(status);
	        archivedSt.setStudyLocation(location);
	        archivedSt.setModifiedDate(new Date());
	        
	        archivedStudyRepo.save(archivedSt);
	        LOG.info("Updated archived study status to {} for study: {}", status, studyId);
	        
	    } catch (Exception e) {
	        LOG.error("Failed to update archived study status for study: {}", studyId, e);
	    }
	}
	
	private String getLocation(String studyId) {
		LOG.info("Getting Location for studyId:"+studyId);
		List<String> storagePaths = locationDao.getStudyStoragePath(studyId);
		LOG.debug("Storage Paths ::"+storagePaths);
		if(storagePaths != null && storagePaths.size() > 0) {
			String storagePath = storagePaths.get(0);
			LOG.info("Storage Path ::"+storagePath);
			String[] studyLocationSplit = storagePath.split("/");
			if(studyLocationSplit.length >= 4) {
				String studyLocation = studyLocationSplit[0].concat("/").concat(studyLocationSplit[1]).concat("/").concat(studyLocationSplit[2]).
						concat("/").concat(studyLocationSplit[3]);
				LOG.info("Study Location ::"+studyLocation);
				return studyLocation;
			}
			LOG.info("Location value does not match the pattern"+ storagePath);
			return null;
		}
		LOG.info("No Locations found for the study:" + studyId);
		return null;
	}
	
	private boolean uploadZip(String sourcePath, String outputFileName) throws IOException {
		try {
	        LOG.info("At start of creating zip");
	        LOG.info("outputFileName-------------------"+outputFileName);
	        getHeapMemoryDetails();
	        
	        boolean uploaded = uploadFile(outputFileName, sourcePath);
	     // No manual close here
	        LOG.info("After creating zip");
	        getHeapMemoryDetails();

	        LOG.info("After closing streams");
	        getHeapMemoryDetails();

	        if (uploaded) {
	            LOG.info("Completed uploading to S3");
	            return true;
	        } else {
	            LOG.info("Could not upload to S3");
	            return false;
	        }
	    } catch (Exception e) {
	        LOG.error("Exception in uploadZip", e);
	        return false;
	    }
	}
	
	public boolean uploadFile(String outputfileName,String sourcePath) {
		
		String localStoragePath = uploadToLocalStorage(outputfileName, sourcePath);
		if("true".equalsIgnoreCase(isArchiveToS3)) {
			 boolean isUploaded = uploadToS3(outputfileName, localStoragePath);
			 if(isUploaded) {
				 File localFile = new File(localStoragePath);
				 File parentFile = localFile.getParentFile();
				 if(localFile.exists()) {
					 if(localFile.delete()) {
						 LOG.info("File deleted successfully from local storage:"+localStoragePath);
						 if(parentFile.exists() && parentFile.isDirectory() && parentFile.delete()) {
							 LOG.info("Folder deleted successfully from local storage:"+parentFile.getAbsolutePath());
						 }
					 } else {
						 LOG.error("Failed to delete the file from local storage:"+localStoragePath);
					 }
				 } else {
					 LOG.error("File not found in local storage:"+localStoragePath);
				 }
			 }
			 return isUploaded;
		}
		return true;
	}
	
	private boolean uploadToS3(String fileName, String localPath) {
		try {
			return storagePresignService.uploadFile(fileName,localPath, bucketName);
		} catch(Exception e) {
			LOG.info("Exception while uploading to S3:"+e.getLocalizedMessage());
		}
		return false;
	}
	
	private String uploadToLocalStorage(String outputfileName,String sourcePath) {
		String storagePat = storagePath+outputfileName;
		File studyFile = new File(storagePat);
		File parentDir = studyFile.getParentFile();
		LOG.info("parent Directory-----------------"+parentDir);
		LOG.info("parent Directory exists-----------------"+parentDir.exists());
        LOG.info("parent Directory before if-----------------"+parentDir.getAbsolutePath());
        LOG.info("sourcePath3-------------------"+sourcePath);   
        if (parentDir != null && !parentDir.exists()) {
        	LOG.info("INside IF-----------------");
        	boolean created = parentDir.mkdirs();
        	LOG.info("mkdirs() result: {}", created);
        	LOG.info("parent exists AFTER mkdirs: {}", parentDir.exists());
        	LOG.info("parent writable: {}", parentDir.canWrite());
        }
        try(FileOutputStream fos = new FileOutputStream(studyFile)){
	        File fileToZip = new File(sourcePath);
	        LOG.info("Creating Zip-----------------");
	        zipFolder(fileToZip,fos);
	        LOG.info("ZIP creation is done-----------------");
	        LOG.info("storagePat---------------------"+storagePat);	
	        return storagePat;
        } catch (IOException e) {
		    LOG.info("Error writing to file to local stoage: " + e.getMessage());
		    return null;
		}
		catch(Exception exp) {
        	LOG.info("Exception while uploding to local storage:"+exp.getLocalizedMessage());
        	return null;
        }
	}
	
	public void zipFolder(File folder, FileOutputStream outputStream) throws IOException {

	    Path sourcePath = folder.toPath();

	    try (ZipOutputStream zos =
	                 new ZipOutputStream(new BufferedOutputStream(outputStream));
	         Stream<Path> paths = Files.walk(sourcePath)) {

	        paths.filter(path -> !Files.isDirectory(path))
	             .forEach(path -> {

	                 String zipEntryName = sourcePath.relativize(path).toString();

	                 try (InputStream fis =
	                              new BufferedInputStream(new FileInputStream(path.toFile()))) {

	                     zos.putNextEntry(new ZipEntry(zipEntryName));

	                     byte[] buffer = new byte[16384];
	                     int bytesRead;
	                     while ((bytesRead = fis.read(buffer)) != -1) {
	                         zos.write(buffer, 0, bytesRead);
	                     }

	                     zos.closeEntry();

	                 } catch (IOException e) {
	                     LOG.error(
	                         "IOException while creating zip entry: " +
	                         zipEntryName + " - " + e.getMessage(), e
	                     );
	                 }
	             });

	    } catch (IOException e) {
	        LOG.error("Exception in zipFolder: " + e.getMessage(), e);
	        throw e;
	    }
	}
	
	public void zipFolder(File folder, ByteArrayOutputStream outputStream) throws IOException {

	    Path sourcePath = folder.toPath();

	    try (ZipOutputStream zos = new ZipOutputStream(outputStream);
	         Stream<Path> paths = Files.walk(sourcePath)) {

	        paths.filter(path -> !Files.isDirectory(path))
	             .forEach(path -> {

	                 String zipEntryName = sourcePath.relativize(path).toString();

	                 try (FileInputStream fis = new FileInputStream(path.toFile())) {

	                     zos.putNextEntry(new ZipEntry(zipEntryName));

	                     byte[] buffer = new byte[4096];
	                     int bytesRead;
	                     while ((bytesRead = fis.read(buffer)) != -1) {
	                         zos.write(buffer, 0, bytesRead);
	                     }

	                     zos.closeEntry();

	                 } catch (IOException e) {
	                     LOG.error(
	                         "Exception while creating zip entry {}: {}",
	                         zipEntryName,
	                         e.getLocalizedMessage(),
	                         e
	                     );
	                 }
	             });

	    } catch (IOException e) {
	        LOG.error("Exception in zipFolder", e);
	        throw e;
	    }
	}
	
	public boolean saveArchivingDetails(String studyId) {
	    try {
	        Optional<Long> orgIdOpt = studyExtensionRepo.findOrgIdByStudyInstanceUID(studyId);
	        
	        if (orgIdOpt == null || orgIdOpt.isEmpty()) {
	            LOG.info("No study data found for studyId: {}", studyId);
	            return false;
	        }
	        
	        Long orgId = orgIdOpt.get();

	        List<ArchivedStudy> archivedStudies = archivedStudyRepo.findByStudyId(studyId);
	        ArchivedStudy archivedSt;

	        // Check if archived study already exists
	        if (archivedStudies != null && !archivedStudies.isEmpty()) {
	            LOG.info("Updating existing archived study data");
	            archivedSt = archivedStudies.get(0);
	            archivedSt.setModifiedDate(new Date());
	            archivedSt.setStatus(0);
	            archivedStudyRepo.save(archivedSt); // Use repository save instead of em.merge
	        } else {
	            LOG.info("Creating new archived study record");
	            archivedSt = new ArchivedStudy();
	            archivedSt.setStudyId(studyId);
	            archivedSt.setOrgId(orgId);
	            
	            // Set organization name
	            Optional<String> orgOptional = organizationRepo.findNameById(orgId);
	            if (orgOptional.isPresent()) {
	                LOG.info("Setting org name");
	                archivedSt.setOrgName(orgOptional.get());
	            } else {
	                LOG.info("Could not find org, setting super admin organization");
	                Optional<String> superAdminOrgOptional = organizationRepo.findNameById(1L);
	                if (superAdminOrgOptional.isPresent()) {
	                    LOG.info("Setting super admin org name");
	                    archivedSt.setOrgName(superAdminOrgOptional.get());
	                } else {
	                    LOG.warn("Could not find super admin org");
	                    archivedSt.setOrgName("Unknown Organization");
	                }
	            }
	            
	            // Set study/patient name
	            List<String> patientNameList = studyExtensionRepo.findPatientFullName(studyId);
	            if (patientNameList != null && !patientNameList.isEmpty() && patientNameList.get(0) != null) {
	                LOG.info("Setting patient name");
	                archivedSt.setStudyName(patientNameList.get(0));
	            } else {
	                LOG.info("Couldn't find patient name, setting default name");
	                archivedSt.setStudyName("Anonymized");
	            }
	            
	            archivedSt.setCreatedDate(new Date());
	            archivedSt.setModifiedDate(new Date());
	            archivedSt.setStatus(0);
	            
	            archivedStudyRepo.save(archivedSt); // Use repository save instead of em.persist
	            LOG.info("Saved new archived record");
	        }
	        return true;
	        
	    } catch (Exception e) {
	        LOG.error("Error saving archiving details for studyId: {}", studyId, e);
	        return false;
	    }
	}
	
	@Transactional
	 public boolean deleteStudyFromDB(String studyUID) {
		 try {
//       	List<Study> study = em.createNamedQuery(Study.FIND_BY_STUDY_IUID, Study.class)
//                   .setParameter(1, studyUID).getResultList();
//       	study.get(0).setRejectionState(RejectionState.COMPLETE);
//       	em.merge(study.get(0));
       	LOG.info("Updating rejection state:");
			JSONObject response = pacsProxyService.deleteStudyExtension(studyUID);
			
			LOG.info("response deleteStudyExtension:"+response);
			if(response.containsKey(UserConstants.STATUS_CODE) && response.get(UserConstants.STATUS_CODE).equals(UserConstants.STATUS_SUCCESS)) {
				asyncDeleteServiceImpl.performAsyncDelete(studyUID,null);
					LOG.info("returning true for deleting study");
			        return true;
			}
			LOG.info("returning false for deleting study");
			 return  false;
       } catch (Exception e) {
       	LOG.info("Exception while deleting study" + e.getLocalizedMessage());
       	return false;
       }
	 }
	 
//	 private ArchiveAEExtension getArchiveAE() {
//		 try {
//			String aetTitle = CommonMethodImpl.env.getProperty("AET_TITlE");
//	        ApplicationEntity ae = device.getApplicationEntity(aetTitle, true);
//	        if (ae == null || !ae.isInstalled()) {
//	        	LOG.info("AET not found or not installed");
//	        }
//	        return ae.getAEExtensionNotNull(ArchiveAEExtension.class);
//		 }catch(Exception e) {
//			 LOG.info("Exception while getting ArchiveAE" + e.getLocalizedMessage());
//	     }
//		 LOG.info("No AET");
//		 return null;
//	}
	 
	 private String getStudyLocation(String studyLocation) {
		 if(dataFolderPath != null) {
			 String fullPath = dataFolderPath.concat("/").concat(archiveDeviceName).concat("/").concat(studyLocation);
			 return fullPath;
		 }
		 return null;
	 }

	public List<StudyExtension> uploadConfToS3(List<StudyExtension> studiesList) {
		List<StudyExtension> archivedStudiesList = new ArrayList<StudyExtension>();
		for (StudyExtension study : studiesList) {
			String studyId = study.getStudyInstanceUID();
			LOG.info("Study Id archiving::"+studyId);
			if(studyId == null)
				continue;
			 String studyLocation = getLocation(studyId);
			 String fileName = studyId + "/" + studyId.concat(".zip");
			LOG.info("fileName----------------------"+fileName);
			 if(studyLocation != null) {
				 String fullLocation = "";
				 try {
					fullLocation = getStudyLocation(studyLocation);
					LOG.info("PACS location:"+ fullLocation + "\nS3 Location:" +  fileName);				
					LOG.info("Started archiving study:"+studyId);
					boolean zipUploaded = uploadConfZip(fullLocation, fileName);
					if(zipUploaded) {
						archivedStudiesList.add(study);
					}
				}catch(IOException e) {
					LOG.info("Exception with filepath :"+fullLocation + " " + e.getLocalizedMessage());
					archivedStudiesList.add(study);
				}catch(Exception exp) {
					LOG.info("Exception while getting or uploading images:");
					exp.printStackTrace();
				}
			 }
		}
		return archivedStudiesList;
	}

	private boolean uploadConfZip(String outputFileName, String sourcePath) throws IOException {
		try {
	        LOG.info("At start of creating zip");
	        LOG.info("sourcePath1--------------------"+sourcePath);
	        getHeapMemoryDetails();
	        
	        boolean uploaded = uploadConfFile(outputFileName, sourcePath);
	     // No manual close here
	        LOG.info("After creating zip");
	        getHeapMemoryDetails();

	        LOG.info("After closing streams");
	        getHeapMemoryDetails();

	        if (uploaded) {
	            LOG.info("Completed uploading to S3");
	            return true;
	        } else {
	            LOG.info("Could not upload to S3");
	            return false;
	        }
	    } catch (Exception e) {
	        LOG.error("Exception in uploadZip", e);
	        return false;
	    }
	}

	private boolean uploadConfFile(String outputFileName, String sourcePath) {
		LOG.info("sourcePath2--------------------"+sourcePath);
		
		String localStoragePath = uploadToLocalStorage(outputFileName, sourcePath);
		if("true".equalsIgnoreCase(isArchiveToS3)) {
			 boolean isUploaded = uploadConfToS3(outputFileName, localStoragePath);
			 if(isUploaded) {
				 File localFile = new File(localStoragePath);
				 File parentFile = localFile.getParentFile();
				 if(localFile.exists()) {
					 if(localFile.delete()) {
						 LOG.info("File deleted successfully from local storage:"+localStoragePath);
						 if(parentFile.exists() && parentFile.isDirectory() && parentFile.delete()) {
							 LOG.info("Folder deleted successfully from local storage:"+parentFile.getAbsolutePath());
						 }
					 } else {
						 LOG.error("Failed to delete the file from local storage:"+localStoragePath);
					 }
				 } else {
					 LOG.error("File not found in local storage:"+localStoragePath);
				 }
			 }
			 return isUploaded;
		}
		return true;
	}

	private boolean uploadConfToS3(String outputFileName, String localPath) {
		try {
			return storagePresignService.uploadFile(bucketName,outputFileName, localPath);
		} catch(Exception e) {
			LOG.info("Exception while uploading to S3:"+e.getLocalizedMessage());
		}
		return false;
	}

}
