package com.efeyopixel.voiceassistant.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public abstract class BaseEntity {
    private Boolean active;
    private LocalDateTime createDateTime;
}
