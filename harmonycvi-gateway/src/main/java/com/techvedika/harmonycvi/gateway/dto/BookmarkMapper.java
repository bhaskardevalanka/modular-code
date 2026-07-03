package com.techvedika.harmonycvi.gateway.dto;

import com.techvedika.harmonycvi.gateway.dto.BookmarkDTO;
import com.techvedika.harmonycvi.gateway.entity.Bookmarks;

import java.util.List;
import java.util.stream.Collectors;

public class BookmarkMapper {

    public static BookmarkDTO toDTO(Bookmarks entity) {
        if (entity == null) return null;

        BookmarkDTO dto = new BookmarkDTO();
        dto.setId(entity.getId());
        dto.setDescription(entity.getDescription());
        dto.setCreatedTime(entity.getCreatedDt());
        dto.setStudyInstanceUID(entity.getStudyInstanceUID());
        dto.setName(entity.getName());
        dto.setVersion(entity.getVersion());
        dto.setIsArchive(entity.getIsArchive());
        dto.setIsPrivateBookmark(entity.getIsPrivateBookmark());
        dto.setCombinedSeriesIds(entity.getCombinedSeriesIds());
        dto.setUserId(entity.getUserId());

        return dto;
    }

    public static List<BookmarkDTO> toDTOList(List<Bookmarks> entities) {
        return entities.stream()
                .map(BookmarkMapper::toDTO)
                .collect(Collectors.toList());
    }
}
