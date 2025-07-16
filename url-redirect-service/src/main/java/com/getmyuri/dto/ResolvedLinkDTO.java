package com.getmyuri.dto;

import com.getmyuri.model.Location;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResolvedLinkDTO {
    private String alias;
    private String link;
    private String username;
    private String password;
    private Location location;
    private Integer radius;

}
