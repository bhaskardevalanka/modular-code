package com.techvedika.harmonycvi.gateway.dto;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BookmarkDetailsDTO {
    private Long id;
    private List<String> combinedSeriesIds;
    private String studyInstanceUID;
    private String name;

    public BookmarkDetailsDTO(Long id, String combinedSeriesIds, String studyInstanceUID,String name) {
        this.id = id;
        this.studyInstanceUID = studyInstanceUID;
        if (combinedSeriesIds != null && !combinedSeriesIds.isEmpty()) {
            this.combinedSeriesIds = Arrays.stream(combinedSeriesIds.split(","))
                                           .map(String::trim)
                                           .collect(Collectors.toList());
        }
        this.name = name;
    }

    public Long getId() { return id; }
    public List<String> getCombinedSeriesIds() { return combinedSeriesIds; }
    public String getStudyInstanceUID() { return studyInstanceUID; }
    public String getName() { return name; }
}
