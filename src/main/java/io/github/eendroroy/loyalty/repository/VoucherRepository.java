package io.github.eendroroy.loyalty.repository;

import io.github.eendroroy.loyalty.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    @Query("SELECT DISTINCT v FROM Voucher v WHERE v.archived = false ORDER BY v.createdAt DESC")
    List<Voucher> findAllActive();

    @Query("SELECT DISTINCT v FROM Voucher v WHERE v.archived = true ORDER BY v.createdAt DESC")
    List<Voucher> findAllArchived();

    @Query("SELECT v FROM Voucher v LEFT JOIN FETCH v.instances WHERE v.id = :id")
    Optional<Voucher> findByIdWithInstances(@Param("id") Long id);

    boolean existsByCode(String code);

    java.util.Optional<Voucher> findByCode(String code);

    @Query("SELECT COUNT(vi) FROM VoucherInstance vi WHERE vi.voucher.id = :voucherId")
    long countInstancesByVoucherId(@Param("voucherId") Long voucherId);
}

