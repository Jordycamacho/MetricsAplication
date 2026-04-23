package com.fitapp.backend.routinecomplete.package_.aplication.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fitapp.backend.infrastructure.persistence.entity.enums.PackageStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Confirmation of status change")
public class PackageStatusChangeResponse {

    @Schema(description = "Package ID")
    @JsonProperty("packageId")
    private Long packageId;

    @Schema(description = "Package name")
    @JsonProperty("packageName")
    private String packageName;

    @Schema(description = "Old status")
    @JsonProperty("oldStatus")
    private PackageStatus oldStatus;

    @Schema(description = "New status")
    @JsonProperty("newStatus")
    private PackageStatus newStatus;

    @Schema(description = "Change timestamp")
    @JsonProperty("changedAt")
    private LocalDateTime changedAt;

    @Schema(description = "Change reason")
    @JsonProperty("reason")
    private String reason;
}