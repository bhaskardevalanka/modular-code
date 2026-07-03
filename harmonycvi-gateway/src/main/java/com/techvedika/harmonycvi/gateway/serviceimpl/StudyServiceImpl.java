package com.techvedika.harmonycvi.gateway.serviceimpl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techvedika.harmonycvi.gateway.repository.StudyExtensionRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
@Transactional
public class StudyServiceImpl {
	
	@Autowired
	private StudyExtensionRepository studyExtRepo;

	@PersistenceContext
	private EntityManager em;
	
	private static final Logger LOG = LoggerFactory.getLogger(StudyServiceImpl.class);

	public long countStudyByDateDrafted(String startDate, String endDate) {
		try {
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			Date stDate = formatter.parse(startDate);
			Date enDate = formatter.parse(endDate);
			
			long studyCount = studyExtRepo.countStudyByStatusAndDate("Draft", stDate, enDate);

			return studyCount;
		} catch (Exception e) {
			return 0;
		}

	}
	

	public long getStudyCountByDateAssigned(String startDate, String endDate) {
		try {
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			Date stDate = formatter.parse(startDate);
			Date enDate = formatter.parse(endDate);
			
			long studyCount = studyExtRepo.countStudyByStatusAndDate("Assigned", stDate, enDate);
			LOG.info("getStudyCountByDateAssigned:::"+studyCount);
			return studyCount;

		} catch (Exception e) {
			e.printStackTrace();
			return 0; // handle no-results case
		}

	}

	public long getStudyCountByDateUnassigned(String startDate, String endDate) {
		try {
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			Date stDate = formatter.parse(startDate);
			Date enDate = formatter.parse(endDate);
			
			long studyCount = studyExtRepo.countStudyByStatusAndDate("Unassigned", stDate, enDate);
			System.out.println("getStudyCountByDateUnassigned:::"+studyCount);
			return studyCount;
		} catch (Exception e) {
			return 0; // handle no-results case
		}

	}

	public long getStudyCountByDateCompleted(String startDate, String endDate) {
		
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date stDate = null;
		Date enDate = null;
		try {
		    stDate = formatter.parse(startDate);
		    enDate = formatter.parse(endDate);
		    
		    long studyCount = studyExtRepo.countStudyByStatusAndDate("Confirmed", stDate, enDate);
		    return studyCount;
		} catch (Exception e) {
		    // Handle or log the error
		    e.printStackTrace();
		    return 0;
		}
	}

	public long countStudyByDateOrgIdDrafted(Long orgId, String startDate, String endDate) {
		try {
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			Date stDate = formatter.parse(startDate);
			Date enDate = formatter.parse(endDate);
			long studyCount = studyExtRepo.countStudyByOrgIdAndStatusAndDate("Draft", orgId, stDate, enDate);
			return studyCount;
		} catch (Exception e) {
			return 0; // handle no-results case
		}

	}


	public long countStudyByDateOrgIdAssigned(Long orgId, String startDate, String endDate) {
		try {
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			Date stDate = formatter.parse(startDate);
			Date enDate = formatter.parse(endDate);
			long studyCount = studyExtRepo.countStudyByOrgIdAndStatusAndDate("Assigned", orgId, stDate, enDate);
			return studyCount;
		} catch (Exception e) {
			return 0; // handle no-results case
		}

	}


	public long countStudyByDateOrgIdUnassigned(Long orgId, String startDate, String endDate) {
		try {
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			Date stDate = formatter.parse(startDate);
			Date enDate = formatter.parse(endDate);
			long studyCount = studyExtRepo.countStudyByOrgIdAndStatusAndDate("Unassigned", orgId, stDate, enDate);
			return studyCount;
		} catch (Exception e) {
			return 0; // handle no-results case
		}

	}

	public long countStudyByDateOrgIdCompleted(Long orgId, String startDate, String endDate) {
		try {
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			Date stDate = formatter.parse(startDate);
			Date enDate = formatter.parse(endDate);
			long studyCount = studyExtRepo.countStudyByOrgIdAndStatusAndDate("Confirmed", orgId, stDate, enDate);
			return studyCount;

		} catch (Exception e) {
			return 0; // handle no-results case
		}
	}

}
