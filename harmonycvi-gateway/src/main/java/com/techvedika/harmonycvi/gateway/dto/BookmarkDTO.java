package com.techvedika.harmonycvi.gateway.dto;

import java.util.Date;
import java.util.List;

public class BookmarkDTO {
    private Long id;
    private String description;
    private Date createdTime;
    private String studyInstanceUID;
    private String name;
    private int version;
    private int isArchive;
    private String isPrivateBookmark;
    private List<String> combinedSeriesIds;
    private Long userId;

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getCreatedTime() { return createdTime; }
    public void setCreatedTime(Date createdTime) { this.createdTime = createdTime; }

    public String getStudyInstanceUID() { return studyInstanceUID; }
    public void setStudyInstanceUID(String studyInstanceUID) { this.studyInstanceUID = studyInstanceUID; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public int getIsArchive() { return isArchive; }
    public void setIsArchive(int isArchive) { this.isArchive = isArchive; }

    public String getIsPrivateBookmark() { return isPrivateBookmark; }
    public void setIsPrivateBookmark(String isPrivateBookmark) { this.isPrivateBookmark = isPrivateBookmark; }

    public List<String> getCombinedSeriesIds() { return combinedSeriesIds; }
    public void setCombinedSeriesIds(List<String> combinedSeriesIds) { this.combinedSeriesIds = combinedSeriesIds; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
