package com.techvedika.harmonycvi.gateway.scheduler;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.techvedika.harmonycvi.gateway.serviceimpl.StudyArchiveServiceImpl;

import jakarta.annotation.PostConstruct;

@Component
public class ArchivingScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(ArchivingScheduler.class);

    private final StudyArchiveServiceImpl studyArchiveServiceImpl;
    private final TaskScheduler taskScheduler;

    @Value("${archiving.hour-of-day:21}")
    private int hourOfDay;

    @Value("${archiving.minute-of-hour:0}")
    private int minuteOfHour;

    @Value("${archiving.duration:PT24H}")
    private String duration;

    @Value("${archiving.enabled:true}")
    private String archivingSwitch;

    public ArchivingScheduler(@Lazy StudyArchiveServiceImpl studyArchiveServiceImpl,@Lazy TaskScheduler taskScheduler) {
        this.studyArchiveServiceImpl = studyArchiveServiceImpl;
        this.taskScheduler = taskScheduler;
    }

    @PostConstruct
    public void scheduleTask() {
        if (!"true".equalsIgnoreCase(archivingSwitch)) {
            LOG.info("ArchivingScheduler is turned off (ARCHIVE_SWITCH={})", archivingSwitch);
            return;
        }

        Duration durationObj = Duration.parse(duration);

        LocalDateTime firstExecution = LocalDateTime.now()
            .withHour(hourOfDay)
            .withMinute(minuteOfHour)
            .withSecond(0)
            .withNano(0);

        if (firstExecution.isBefore(LocalDateTime.now())) {
            firstExecution = firstExecution.plusDays(1);
        }

        Instant startTime = firstExecution.atZone(ZoneId.systemDefault()).toInstant();

        LOG.info("Time Now:{}",LocalDateTime.now());
        LOG.info("ArchivingScheduler initialized. First execution: {}, repeat interval: {}", 
                 firstExecution, durationObj);

        taskScheduler.scheduleAtFixedRate(this::execute, startTime, durationObj);
    }

    public void execute() {
        try {
            LOG.info("ArchivingScheduler triggered at {}", LocalDateTime.now());
            studyArchiveServiceImpl.archiveExpiredStudies();
        } catch (Exception e) {
            LOG.error("Exception while executing archiving", e);
        }
    }
}