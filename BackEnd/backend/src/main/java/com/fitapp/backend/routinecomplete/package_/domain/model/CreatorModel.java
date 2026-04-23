package com.fitapp.backend.routinecomplete.package_.domain.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatorModel {
    private Long id;
    private String username;
    private String email;
    private String avatarUrl;
    private Integer reputationScore;
}
 