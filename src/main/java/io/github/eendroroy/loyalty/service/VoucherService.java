package io.github.eendroroy.loyalty.service;

import io.github.eendroroy.loyalty.entity.Voucher;
import io.github.eendroroy.loyalty.entity.VoucherInstance;

import java.util.List;
import java.util.Optional;

public interface VoucherService {

    /** Returns all non-archived vouchers. */
    List<Voucher> findAll();

    /** Returns all archived vouchers. */
    List<Voucher> findAllArchived();

    Optional<Voucher> findById(Long id);

    Voucher save(Voucher entity);

    /**
     * Soft-deletes a voucher by setting {@code archived = true}.
     *
     * @throws jakarta.persistence.EntityNotFoundException if the voucher does not exist
     */
    void archiveById(Long id);

    /**
     * Hard-deletes an already-archived voucher.
     *
     * @throws jakarta.persistence.EntityNotFoundException if the voucher does not exist
     * @throws IllegalStateException                       if the voucher is not archived
     */
    void purgeArchivedById(Long id);

    /**
     * Awards a new instance of the voucher, generating a unique 7-character
     * alphanumeric (A-Z 0-9, all caps) secret code.
     *
     * @throws jakarta.persistence.EntityNotFoundException if the voucher does not exist
     * @throws IllegalStateException                       if the voucher is inactive or archived,
     *                                                     or if the instance cap has been reached
     */
    VoucherInstance award(Long voucherId);

    /** Returns all instances that have been awarded for a specific voucher. */
    List<VoucherInstance> findInstancesByVoucherId(Long voucherId);

    /** Returns the count of awarded instances for a voucher. */
    long countInstances(Long voucherId);
}

