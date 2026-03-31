package io.github.eendroroy.loyalty.repository;

import io.github.eendroroy.loyalty.entity.VoucherInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VoucherInstanceRepository extends JpaRepository<VoucherInstance, Long> {

    @Query("SELECT vi FROM VoucherInstance vi WHERE vi.voucher.id = :voucherId ORDER BY vi.createdAt DESC")
    List<VoucherInstance> findByVoucherId(@Param("voucherId") Long voucherId);

    boolean existsBySecretCode(String secretCode);
}

