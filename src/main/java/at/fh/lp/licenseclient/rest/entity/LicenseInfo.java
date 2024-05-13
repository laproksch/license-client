package at.fh.lp.licenseclient.rest.entity;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LicenseInfo {
    private String name;
    private boolean valid;
}
