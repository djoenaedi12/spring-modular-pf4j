package gasi.gps.auth.infrastructure.entity;

import gasi.gps.auth.domain.model.MenuType;
import gasi.gps.core.api.domain.model.Platform;
import gasi.gps.core.api.infrastructure.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "menus")
public class MenuEntity extends BaseEntity {

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "title", length = 150)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private MenuEntity parent;

    @Column(name = "path", length = 255)
    private String path;

    @Column(name = "icon", length = 50)
    private String icon;

    @Column(name = "sort_order", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer sortOrder;

    @Column(name = "type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MenuType type;

    @Column(name = "platform", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Platform platform;

    @Column(name = "module", length = 50)
    private String module;

    @Column(name = "is_visible", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isVisible;

    @Column(name = "help_url", length = 255)
    private String helpUrl;
}
