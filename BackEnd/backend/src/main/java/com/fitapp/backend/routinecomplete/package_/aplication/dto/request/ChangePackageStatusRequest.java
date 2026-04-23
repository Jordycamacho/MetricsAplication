package com.fitapp.backend.routinecomplete.package_.aplication.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to change package status")
public class ChangePackageStatusRequest {
 
    @NotBlank(message = "Status is required")
    @Schema(description = "New status: DRAFT, PUBLISHED, DEPRECATED, SUSPENDED")
    @JsonProperty("status")
    private String status;
 
    @Schema(description = "Reason for status change (especially for SUSPENDED)")
    @Size(max = 500, message = "Reason must be max 500 characters")
    @JsonProperty("reason")
    private String reason;
}