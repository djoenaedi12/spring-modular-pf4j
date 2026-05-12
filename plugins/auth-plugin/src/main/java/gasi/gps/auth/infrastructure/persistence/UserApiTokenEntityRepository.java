package gasi.gps.auth.infrastructure.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import gasi.gps.auth.infrastructure.entity.UserApiTokenEntity;

/**
 * Spring Data JPA repository for UserApiTokenEntity.
 */
@Repository
public interface UserApiTokenEntityRepository extends JpaRepository<UserApiTokenEntity, Long>,
        JpaSpecificationExecutor<UserApiTokenEntity> {

    List<UserApiTokenEntity> findByUser_IdOrderByCreatedAtDesc(Long userId);

    Optional<UserApiTokenEntity> findByIdAndUser_Id(Long id, Long userId);

    @Query("""
            select distinct t
            from UserApiTokenEntity t
            join fetch t.user u
            left join fetch u.roles
            where t.tokenHash = :tokenHash
              and (t.expiresAt is null or t.expiresAt > :now)
            """)
    Optional<UserApiTokenEntity> findActiveWithUserByTokenHash(@Param("tokenHash") String tokenHash,
            @Param("now") Instant now);
}
