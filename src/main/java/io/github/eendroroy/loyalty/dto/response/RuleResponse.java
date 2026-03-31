package io.github.eendroroy.loyalty.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.eendroroy.loyalty.enums.RuleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RuleResponse {
    private Long id;
    private String name;
    private String description;
    private String ruleExpression;
    private RuleStatus status;
    private Integer priority;
    private LocalDate activeFrom;
    private LocalDate activeTo;
    private String frequency;
    private LocalDateTime lastRunAt;
    private List<RuleActionResponse> actions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

