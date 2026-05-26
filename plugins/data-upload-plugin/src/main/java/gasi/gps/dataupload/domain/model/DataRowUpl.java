package gasi.gps.dataupload.domain.model;

import gasi.gps.core.api.domain.model.BaseModel;
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
public class DataRowUpl extends BaseModel {
    private DataUpl dataUpl;
    private int rowNumber;
    private String rowData;
    private UploadRowStatus rowStatus;
    private String identifier;
    private String lookupValue1;
    private String lookupValue2;
    private String lookupValue3;
    private String errorMessage;
}
