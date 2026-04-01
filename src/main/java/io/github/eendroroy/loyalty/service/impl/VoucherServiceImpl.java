package io.github.eendroroy.loyalty.service.impl;

import io.github.eendroroy.loyalty.entity.Voucher;
import io.github.eendroroy.loyalty.entity.VoucherInstance;
import io.github.eendroroy.loyalty.repository.VoucherInstanceRepository;
import io.github.eendroroy.loyalty.repository.VoucherRepository;
import io.github.eendroroy.loyalty.service.VoucherService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int SECRET_CODE_LENGTH = 7;
    private static final int MAX_GENERATION_ATTEMPTS = 20;

    private final VoucherRepository voucherRepository;
    private final VoucherInstanceRepository voucherInstanceRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional(readOnly = true)
    public List<Voucher> findAll() {
        return voucherRepository.findAllActive();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Voucher> findAllArchived() {
        return voucherRepository.findAllArchived();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Voucher> findById(Long id) {
        return voucherRepository.findByIdWithInstances(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Voucher> findByCode(String code) {
        return voucherRepository.findByCode(code);
    }

    @Override
    @Transactional
    public Voucher save(Voucher entity) {
        return voucherRepository.save(entity);
    }

    @Override
    @Transactional
    public void archiveById(Long id) {
        var voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Voucher not found: " + id));
        voucher.setArchived(true);
        voucher.setActive(false);
        voucherRepository.save(voucher);
    }

    @Override
    @Transactional
    public void purgeArchivedById(Long id) {
        var voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Voucher not found: " + id));
        if (!Boolean.TRUE.equals(voucher.getArchived())) {
            throw new IllegalStateException("Voucher must be archived before it can be purged");
        }
        voucherRepository.delete(voucher);
    }

    @Override
    @Transactional
    public VoucherInstance award(Long voucherId) {
        var voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new EntityNotFoundException("Voucher not found: " + voucherId));
        if (Boolean.TRUE.equals(voucher.getArchived())) {
            throw new IllegalStateException("Cannot award an archived voucher");
        }
        if (!Boolean.TRUE.equals(voucher.getActive())) {
            throw new IllegalStateException("Cannot award an inactive voucher");
        }
        long awarded = voucherRepository.countInstancesByVoucherId(voucherId);
        if (awarded >= voucher.getCount()) {
            throw new IllegalStateException(
                    "Instance cap of " + voucher.getCount() + " has been reached for voucher: " + voucher.getCode()
            );
        }
        var instance = new VoucherInstance();
        instance.setVoucher(voucher);
        instance.setSecretCode(generateUniqueSecretCode());
        return voucherInstanceRepository.save(instance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoucherInstance> findInstancesByVoucherId(Long voucherId) {
        return voucherInstanceRepository.findByVoucherId(voucherId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countInstances(Long voucherId) {
        return voucherRepository.countInstancesByVoucherId(voucherId);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String generateUniqueSecretCode() {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            var code = randomCode();
            if (!voucherInstanceRepository.existsBySecretCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Failed to generate a unique secret code after "
                + MAX_GENERATION_ATTEMPTS + " attempts");
    }

    private String randomCode() {
        var sb = new StringBuilder(SECRET_CODE_LENGTH);
        for (int i = 0; i < SECRET_CODE_LENGTH; i++) {
            sb.append(ALPHABET.charAt(secureRandom.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}

