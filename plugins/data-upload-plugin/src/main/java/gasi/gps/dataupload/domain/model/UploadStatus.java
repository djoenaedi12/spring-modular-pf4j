package gasi.gps.dataupload.domain.model;

public enum UploadStatus {
    UPLOADING,
    UPLOADED,
    VALIDATING,
    VALIDATED,
    COMMITTING,
    PENDING_APPROVAL,
    COMMITTED,
    REJECTED,
    FAILED,
}
