package at.fh.lp.licenseclient.licensing.entity;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class LicenseFingerprint {
    private String fingerprint;
}
