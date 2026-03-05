package gasi.gps.auth.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "role_record_rules")
@IdClass(RoleRecordRuleId.class)
public class RoleRecordRuleEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity role;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_rule_id", nullable = false)
    private RecordRuleEntity recordRule;

    @Column(name = "is_negated", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isNegated;
}
