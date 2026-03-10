package gasi.gps.auth.domain.model;

import java.time.Instant;

import gasi.gps.core.api.domain.model.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Domain model for user device records.
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserDevice extends BaseModel {

    private User user;
    private AppClient appClient;
    private String deviceId;
    private String deviceModel;
    private Instant trustedExpiresAt;
}
