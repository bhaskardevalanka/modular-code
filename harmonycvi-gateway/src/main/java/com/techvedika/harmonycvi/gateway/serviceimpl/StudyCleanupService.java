package com.techvedika.harmonycvi.gateway.serviceimpl;

import org.springframework.stereotype.Service;

import com.techvedika.harmonycvi.gateway.repository.BookmarksRepository;
import com.techvedika.harmonycvi.gateway.repository.SeriesMeasurementsRepository;
import com.techvedika.harmonycvi.gateway.repository.SeriesParameterRepository;
import com.techvedika.harmonycvi.gateway.repository.StudyClassificationRepository;
import com.techvedika.harmonycvi.gateway.repository.StudyClinicalDetailsCommentsRepository;
import com.techvedika.harmonycvi.gateway.repository.StudyClinicalDetailsRepository;
import com.techvedika.harmonycvi.gateway.repository.StudyExtensionRepository;
import com.techvedika.harmonycvi.gateway.repository.StudyParameterRepository;
import com.techvedika.harmonycvi.gateway.repository.StudyVolumeInfoRepository;
import com.techvedika.harmonycvi.gateway.repository.UserStudiesRepository;

import jakarta.transaction.Transactional;

@Service
public class StudyCleanupService {

    private final BookmarksRepository bookmarksRepo;
    private final SeriesMeasurementsRepository seriesMeasurementRepo;
    private final StudyParameterRepository studyParamRepo;
    private final SeriesParameterRepository seriesParamRepo;
    private final StudyVolumeInfoRepository studyVolumeInfoRepo;
    private final StudyClinicalDetailsRepository studyClinicalDetailsRepo;
    private final StudyClinicalDetailsCommentsRepository studyClinicalCommentsRepo;
    private final UserStudiesRepository userStudyRepo;
    private final StudyClassificationRepository studyClassificationRepo;
    private final StudyExtensionRepository studyExtensionRepo;

    public StudyCleanupService(
            BookmarksRepository bookmarksRepo,
            SeriesMeasurementsRepository seriesMeasurementRepo,
            StudyParameterRepository studyParamRepo,
            SeriesParameterRepository seriesParamRepo,
            StudyVolumeInfoRepository studyVolumeInfoRepo,
            StudyClinicalDetailsRepository studyClinicalDetailsRepo,
            StudyClinicalDetailsCommentsRepository studyClinicalCommentsRepo,
            UserStudiesRepository userStudyRepo,
            StudyClassificationRepository studyClassificationRepo,
            StudyExtensionRepository studyExtensionRepo) {

        this.bookmarksRepo = bookmarksRepo;
        this.seriesMeasurementRepo = seriesMeasurementRepo;
        this.studyParamRepo = studyParamRepo;
        this.seriesParamRepo = seriesParamRepo;
        this.studyVolumeInfoRepo = studyVolumeInfoRepo;
        this.studyClinicalDetailsRepo = studyClinicalDetailsRepo;
        this.studyClinicalCommentsRepo = studyClinicalCommentsRepo;
        this.userStudyRepo = userStudyRepo;
        this.studyClassificationRepo = studyClassificationRepo;
        this.studyExtensionRepo = studyExtensionRepo;
    }

    @Transactional
    public void cleanUp(String studyInstanceUID) {

        userStudyRepo.deleteByStudyId(studyInstanceUID);
//        seriesParamRepo.deleteByStudyId(studyInstanceUID);
//        seriesMeasurementRepo.deleteByStudyId(studyInstanceUID);
//        studyVolumeInfoRepo.deleteByStudyId(studyInstanceUID);
//        studyParamRepo.deleteByStudyId(studyInstanceUID);
        studyClinicalDetailsRepo.deleteByStudyId(studyInstanceUID);
        studyClinicalCommentsRepo.deleteByStudyId(studyInstanceUID);
//        bookmarksRepo.deleteByStudyInstanceUID(studyInstanceUID);
        studyClassificationRepo.deleteByStudyId(studyInstanceUID);
        studyExtensionRepo.deleteByStudyId(studyInstanceUID);
    }
}
