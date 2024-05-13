package at.fh.lp.licenseclient.rest;

import at.fh.lp.licenseclient.licensing.LicenseManager;
import at.fh.lp.licenseclient.rest.entity.LicenseInfo;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/license-info")
public class LicenseInfoEndpoint {

    private final LicenseManager licenseManager;

    @Autowired
    public LicenseInfoEndpoint(LicenseManager licenseManager) {
        this.licenseManager = licenseManager;
    }

    @GetMapping()
    public LicenseInfo getLicenseInfo() {
        return licenseManager.getCurrentLicenseInfo();
    }
}
