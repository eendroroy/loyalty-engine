package io.github.eendroroy.loyalty.controller;

import io.github.eendroroy.loyalty.dto.request.VoucherRequest;
import io.github.eendroroy.loyalty.dto.response.VoucherInstanceResponse;
import io.github.eendroroy.loyalty.dto.response.VoucherResponse;
import io.github.eendroroy.loyalty.mapper.VoucherMapper;
import io.github.eendroroy.loyalty.service.VoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing {@link io.github.eendroroy.loyalty.entity.Voucher} resources
 * and awarding individual {@link io.github.eendroroy.loyalty.entity.VoucherInstance}s.
 *
 * <p>Base path: {@code /api/admin/vouchers}
 */
@Tag(name = "Vouchers", description = "Define, manage, and award discount vouchers")
@RestController
@RequestMapping("/api/admin/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;
    private final VoucherMapper voucherMapper;

    // ── Voucher CRUD ──────────────────────────────────────────────────────────

    @Operation(summary = "List all active vouchers", description = "Returns all non-archived vouchers.")
    @GetMapping
    public List<VoucherResponse> getAll() {
        return voucherService.findAll().stream()
                .map(v -> {
                    var response = voucherMapper.toResponse(v);
                    long count = voucherService.countInstances(v.getId());
                    return VoucherResponse.builder()
                            .id(response.getId())
                            .name(response.getName())
                            .code(response.getCode())
                            .description(response.getDescription())
                            .voucherType(response.getVoucherType())
                            .count(response.getCount())
                            .active(response.getActive())
                            .archived(response.getArchived())
                            .instanceCount(count)
                            .createdAt(response.getCreatedAt())
                            .updatedAt(response.getUpdatedAt())
                            .build();
                })
                .toList();
    }

    @Operation(summary = "List all archived vouchers", description = "Returns all soft-deleted vouchers.")
    @GetMapping("/archived")
    public List<VoucherResponse> getAllArchived() {
        return voucherService.findAllArchived().stream()
                .map(v -> {
                    var response = voucherMapper.toResponse(v);
                    long count = voucherService.countInstances(v.getId());
                    return VoucherResponse.builder()
                            .id(response.getId())
                            .name(response.getName())
                            .code(response.getCode())
                            .description(response.getDescription())
                            .voucherType(response.getVoucherType())
                            .count(response.getCount())
                            .active(response.getActive())
                            .archived(response.getArchived())
                            .instanceCount(count)
                            .createdAt(response.getCreatedAt())
                            .updatedAt(response.getUpdatedAt())
                            .build();
                })
                .toList();
    }

    @Operation(summary = "Get voucher by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Voucher found"),
        @ApiResponse(responseCode = "404", description = "Voucher not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<VoucherResponse> getById(
            @Parameter(description = "Voucher ID") @PathVariable Long id) {
        return voucherService.findById(id)
                .map(v -> {
                    var response = voucherMapper.toResponse(v);
                    long count = voucherService.countInstances(v.getId());
                    return VoucherResponse.builder()
                            .id(response.getId())
                            .name(response.getName())
                            .code(response.getCode())
                            .description(response.getDescription())
                            .voucherType(response.getVoucherType())
                            .count(response.getCount())
                            .active(response.getActive())
                            .archived(response.getArchived())
                            .instanceCount(count)
                            .createdAt(response.getCreatedAt())
                            .updatedAt(response.getUpdatedAt())
                            .build();
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create a voucher", description = "Creates a new voucher definition.")
    @ApiResponse(responseCode = "201", description = "Voucher created")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VoucherResponse create(@Valid @RequestBody VoucherRequest request) {
        var saved = voucherService.save(voucherMapper.toEntity(request));
        return voucherMapper.toResponse(saved);
    }

    @Operation(summary = "Update a voucher",
               description = "Partially updates a voucher. Null fields leave stored values unchanged.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Voucher updated"),
        @ApiResponse(responseCode = "404", description = "Voucher not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<VoucherResponse> update(
            @Parameter(description = "Voucher ID") @PathVariable Long id,
            @Valid @RequestBody VoucherRequest request) {
        return voucherService.findById(id).map(existing -> {
            voucherMapper.updateEntity(request, existing);
            return ResponseEntity.ok(voucherMapper.toResponse(voucherService.save(existing)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Archive a voucher",
               description = "Soft-deletes a voucher. Sets archived=true and active=false.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Voucher archived"),
        @ApiResponse(responseCode = "404", description = "Voucher not found")
    })
    @PostMapping("/{id}/archive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> archive(
            @Parameter(description = "Voucher ID") @PathVariable Long id) {
        try {
            voucherService.archiveById(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Purge an archived voucher",
               description = "Hard-deletes an archived voucher and all its instances. Irreversible.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Voucher purged"),
        @ApiResponse(responseCode = "404", description = "Voucher not found"),
        @ApiResponse(responseCode = "409", description = "Voucher is not archived")
    })
    @DeleteMapping("/{id}/purge")
    public ResponseEntity<Void> purge(
            @Parameter(description = "Voucher ID") @PathVariable Long id) {
        try {
            voucherService.purgeArchivedById(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    // ── Direct delete is not supported ────────────────────────────────────────

    @Operation(summary = "Delete not supported",
               description = "Direct deletion is not allowed. Use /archive and /purge instead.")
    @ApiResponse(responseCode = "405", description = "Method not allowed — use /archive + /purge")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public void deleteNotAllowed(@PathVariable Long id) {
        // intentionally empty — method not allowed
    }

    // ── Voucher Instances ─────────────────────────────────────────────────────

    @Operation(summary = "List awarded instances",
               description = "Returns all secret-code instances that have been awarded for a voucher.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Instance list returned"),
        @ApiResponse(responseCode = "404", description = "Voucher not found")
    })
    @GetMapping("/{id}/instances")
    public ResponseEntity<List<VoucherInstanceResponse>> getInstances(
            @Parameter(description = "Voucher ID") @PathVariable Long id) {
        return voucherService.findById(id)
                .map(v -> ResponseEntity.ok(
                        voucherService.findInstancesByVoucherId(id).stream()
                                .map(voucherMapper::toInstanceResponse)
                                .toList()))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Award a voucher instance",
               description = "Generates a unique 7-character alphanumeric secret code and records an awarded instance.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Instance awarded"),
        @ApiResponse(responseCode = "404", description = "Voucher not found"),
        @ApiResponse(responseCode = "409", description = "Voucher is inactive, archived, or instance cap reached")
    })
    @PostMapping("/{id}/award")
    public ResponseEntity<VoucherInstanceResponse> award(
            @Parameter(description = "Voucher ID") @PathVariable Long id) {
        try {
            var instance = voucherService.award(id);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(voucherMapper.toInstanceResponse(instance));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}

