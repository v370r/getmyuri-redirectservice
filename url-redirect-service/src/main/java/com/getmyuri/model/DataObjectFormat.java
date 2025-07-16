package com.getmyuri.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.getmyuri.dto.LinkDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "links")
public class DataObjectFormat {

    @Id
    private String id;

    private String alias;
    @Builder.Default
    private String link = "";
    @Builder.Default
    private Location location = null;
    @Builder.Default
    private Integer radius = null;
    @Builder.Default
    private long timestart = 0L;
    @Builder.Default
    private long timeend = 0L;
    private List<DataObjectFormat> sublinks;
    @Builder.Default
    private String password = "";
    @Builder.Default
    private int clicks = 0;
    private String username;
    private Date startTime;
    private Date expiresAt;
}
