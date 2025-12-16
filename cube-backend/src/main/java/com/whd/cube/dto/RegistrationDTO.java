package com.whd.cube.dto;

import lombok.Data;
import java.util.List;

@Data
public class RegistrationDTO {
    private Long competitionId;
    private List<String> eventIds;
}