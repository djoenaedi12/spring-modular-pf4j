package gasi.gps.auth.domain.model;

import gasi.gps.core.api.domain.model.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Domain model representing an OAuth2/application client.
 *
 * <p>
 * Note: This model extends {@code BaseModel} to include common fields like
 * {@code id}, {@code createdAt}, and {@code updatedAt}. However,
 * the {@code app_clients} table uses {@code client_id} as primary key
 * and does not follow the standard default_columns pattern.
 * </p>
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AppClient extends BaseModel {

    private String clientId;
    private String clientSecret;
    private String[] grantTypes;
    private String redirectUris;
    private Integer accessTokenValidity;
    private Integer refreshTokenValidity;
    private String[] scopes;
}
