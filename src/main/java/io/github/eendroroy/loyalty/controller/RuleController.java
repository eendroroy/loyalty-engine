package io.github.eendroroy.loyalty.controller;

import io.github.eendroroy.loyalty.dto.request.ExpressionValidationRequest;
import io.github.eendroroy.loyalty.dto.request.RuleActionRequest;
import io.github.eendroroy.loyalty.dto.request.RuleRequest;
import io.github.eendroroy.loyalty.dto.response.ExpressionValidationResponse;
import io.github.eendroroy.loyalty.dto.response.RuleActionResponse;
import io.github.eendroroy.loyalty.dto.response.RuleResponse;
import io.github.eendroroy.loyalty.mapper.RuleActionMapper;
import io.github.eendroroy.loyalty.mapper.RuleMapper;
import io.github.eendroroy.loyalty.rule.RuleParser;
import io.github.eendroroy.loyalty.rule.exception.RuleParseException;
import io.github.eendroroy.loyalty.service.RuleActionService;
import io.github.eendroroy.loyalty.service.RuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing {@link io.github.eendroroy.loyalty.entity.Rule} resources
 * and their nested {@link io.github.eendroroy.loyalty.entity.RuleAction} definitions.
 *
 * <p>Base path: {@code /api/admin/rules}
 *
 * <p>Rules are authored in the custom {@code WHEN … THEN …} expression language,
 * progress through a {@code DRAFT → ACTIVE → INACTIVE} lifecycle, and are evaluated
 * by the rule engine on the configured cron {@code frequency}.
 */
@Tag(name = "Rules", description = "Author and manage dynamic reward rules and their actions")
@RestController
@RequestMapping("/api/admin/rules")
@RequiredArgsConstructor
public class RuleController {

    private final RuleService ruleService;
    private final RuleActionService ruleActionService;
    private final RuleMapper ruleMapper;
    private final RuleActionMapper ruleActionMapper;
    private final RuleParser ruleParser;

    // ── Expression validation ─────────────────────────────────────────────────

    /**
     * Validates a rule expression without persisting anything.
     *
     * @param request the expression to validate
     * @return {@code 200 OK} with {@code {valid: true}} or {@code {valid: false, error: "..."}};
     *         never returns 4xx — parse failures are reported in the body
     */
    @Operation(summary = "Validate a rule expression",
               description = "Parses the expression and returns whether it is syntactically valid.")
    @ApiResponse(responseCode = "200", description = "Validation result returned")
    @PostMapping("/validate-expression")
    public ExpressionValidationResponse validateExpression(
            @Valid @RequestBody ExpressionValidationRequest request) {
        try {
            ruleParser.parse(request.getExpression());
            return ExpressionValidationResponse.builder().valid(true).build();
        } catch (RuleParseException e) {
            return ExpressionValidationResponse.builder().valid(false).error(e.getMessage()).build();
        }
    }

    // ── Rule CRUD ─────────────────────────────────────────────────────────────

    /**
     * Returns all rules, each including their full list of reward actions.
     *
     * @return list of all rules with embedded actions
     */
    @Operation(summary = "List all rules", description = "Returns all rules with their reward actions.")
    @GetMapping
    public List<RuleResponse> getAll() {
        return ruleService.findAll().stream().map(ruleMapper::toResponse).toList();
    }

    /**
     * Retrieves a single rule by its database ID.
     *
     * @param id the rule identifier
     * @return {@code 200 OK} with the rule, or {@code 404} if not found
     */
    @Operation(summary = "Get rule by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rule found"),
        @ApiResponse(responseCode = "404", description = "Rule not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<RuleResponse> getById(
            @Parameter(description = "Rule ID") @PathVariable Long id) {
        return ruleService.findById(id)
                .map(ruleMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Creates a new rule. The rule starts in {@code DRAFT} status unless explicitly set.
     *
     * @param request validated creation payload
     * @return {@code 201 Created} with the persisted rule
     */
    @Operation(summary = "Create a rule",
               description = "Creates a new rule. Status defaults to DRAFT.")
    @ApiResponse(responseCode = "201", description = "Rule created")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RuleResponse create(@Valid @RequestBody RuleRequest request) {
        return ruleMapper.toResponse(ruleService.save(ruleMapper.toEntity(request)));
    }

    /**
     * Updates an existing rule. Null fields in the request body leave the stored
     * values unchanged (PATCH semantics via {@code updateEntity} mapper).
     *
     * @param id      the rule identifier
     * @param request validated update payload
     * @return {@code 200 OK} with the updated rule, or {@code 404} if not found
     */
    @Operation(summary = "Update a rule",
               description = "Partially updates a rule. Null fields leave stored values unchanged.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rule updated"),
        @ApiResponse(responseCode = "404", description = "Rule not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<RuleResponse> update(
            @Parameter(description = "Rule ID") @PathVariable Long id,
            @Valid @RequestBody RuleRequest request) {
        return ruleService.findById(id).map(existing -> {
            ruleMapper.updateEntity(request, existing);
            return ResponseEntity.ok(ruleMapper.toResponse(ruleService.save(existing)));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Deletes a rule and all of its reward actions (cascaded).
     *
     * @param id the rule identifier
     * @return {@code 204 No Content}, or {@code 404} if not found
     */
    @Operation(summary = "Delete a rule")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Rule deleted"),
        @ApiResponse(responseCode = "404", description = "Rule not found")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> delete(
            @Parameter(description = "Rule ID") @PathVariable Long id) {
        if (ruleService.findById(id).isEmpty()) return ResponseEntity.notFound().build();
        ruleService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ── Action Management ─────────────────────────────────────────────────────

    /**
     * Lists all reward actions attached to the given rule.
     *
     * @param id the rule identifier
     * @return {@code 200 OK} with the actions list, or {@code 404} if the rule is not found
     */
    @Operation(summary = "List actions for a rule")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Actions returned"),
        @ApiResponse(responseCode = "404", description = "Rule not found")
    })
    @GetMapping("/{id}/actions")
    public ResponseEntity<List<RuleActionResponse>> getActions(
            @Parameter(description = "Rule ID") @PathVariable Long id) {
        if (ruleService.findById(id).isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(
                ruleActionService.findByRuleId(id).stream()
                        .map(ruleActionMapper::toResponse).toList()
        );
    }

    /**
     * Adds a reward action to the given rule.
     *
     * @param id      the rule identifier
     * @param request validated action creation payload
     * @return {@code 201 Created} with the new action, or {@code 404} if the rule is not found
     */
    @Operation(summary = "Add a reward action to a rule")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Action created"),
        @ApiResponse(responseCode = "404", description = "Rule not found")
    })
    @PostMapping("/{id}/actions")
    public ResponseEntity<RuleActionResponse> addAction(
            @Parameter(description = "Rule ID") @PathVariable Long id,
            @Valid @RequestBody RuleActionRequest request) {
        return ruleService.findById(id).map(rule -> {
            var action = ruleActionMapper.toEntity(request);
            action.setRule(rule);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ruleActionMapper.toResponse(ruleActionService.save(action)));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Updates an existing reward action.
     *
     * @param id       the rule identifier (used for 404 guard)
     * @param actionId the action identifier
     * @param request  validated action update payload
     * @return {@code 200 OK} with the updated action, or {@code 404} if either ID is not found
     */
    @Operation(summary = "Update a reward action")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Action updated"),
        @ApiResponse(responseCode = "404", description = "Rule or action not found")
    })
    @PutMapping("/{id}/actions/{actionId}")
    public ResponseEntity<RuleActionResponse> updateAction(
            @Parameter(description = "Rule ID") @PathVariable Long id,
            @Parameter(description = "Action ID") @PathVariable Long actionId,
            @Valid @RequestBody RuleActionRequest request) {
        if (ruleService.findById(id).isEmpty()) return ResponseEntity.notFound().build();
        return ruleActionService.findById(actionId).map(existing -> {
            ruleActionMapper.updateEntity(request, existing);
            return ResponseEntity.ok(ruleActionMapper.toResponse(ruleActionService.save(existing)));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Removes a reward action from a rule.
     *
     * @param id       the rule identifier
     * @param actionId the action identifier
     * @return {@code 204 No Content}, or {@code 404} if either ID is not found
     */
    @Operation(summary = "Delete a reward action")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Action deleted"),
        @ApiResponse(responseCode = "404", description = "Rule or action not found")
    })
    @DeleteMapping("/{id}/actions/{actionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteAction(
            @Parameter(description = "Rule ID") @PathVariable Long id,
            @Parameter(description = "Action ID") @PathVariable Long actionId) {
        if (ruleService.findById(id).isEmpty()
                || ruleActionService.findById(actionId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        ruleActionService.deleteById(actionId);
        return ResponseEntity.noContent().build();
    }
}
