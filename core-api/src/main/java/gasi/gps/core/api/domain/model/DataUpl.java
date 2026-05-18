package gasi.gps.core.api.domain.model;

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
public class DataUpl extends BaseModel {
    private String resource;
    private String fileName;
    private int totalRows;
    private int validRows;
    private int invalidRows;
    private int committedRows;
    private UploadStatus uploadStatus;
    private String errorMessage;
}
