package com.scriptkill.entity;

import lombok.Data;

@Data
public class FavoriteVO {
    private Long id;  // favorite record id
    private Long scriptId;
    private String name;
    private String type;
    private String difficulty;
    private Integer minPlayers;
    private Integer maxPlayers;
    private Integer duration;
    private java.math.BigDecimal price;
    private String cover;
    private String description;
}
