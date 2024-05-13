package at.fh.lp.licenseclient.licensing;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import at.fh.lp.licenseclient.licensing.entity.LicenseFingerprint;
import at.fh.lp.licenseclient.rest.entity.LicenseInfo;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;

@Slf4j
@Component
public class LicenseManager {

    private final RestTemplate restTemplate;
    @Getter
    private LicenseInfo currentLicenseInfo;
    private int minutesSinceLastSuccessfulLicenseRequest = 0;

    @Autowired
    public LicenseManager(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        log.info("Requesting license from license server");
        minutesSinceLastSuccessfulLicenseRequest = 0;
        requestLicenseInfoFromLicenseServer();
    }

    @Scheduled(initialDelay = 60000, fixedRate = 60000)
    public void checkLicense() {
        log.info("Checking license information");
        if (requestLicenseInfoFromLicenseServer()) {
            minutesSinceLastSuccessfulLicenseRequest = 0;
        } else {
            minutesSinceLastSuccessfulLicenseRequest += 1;
            log.info("{} minutes since last successful license request", minutesSinceLastSuccessfulLicenseRequest);
        }
        if (minutesSinceLastSuccessfulLicenseRequest >= 10) {
            log.info("License is invalidated because {} minutes since last successful license request", minutesSinceLastSuccessfulLicenseRequest);
            if (currentLicenseInfo != null) {
                currentLicenseInfo.setValid(false);
            }
        }
    }

    private boolean requestLicenseInfoFromLicenseServer() {
        try {
            String fingerprint = calculateHardwareFingerprint();
            log.info("Requesting license information from fingerprint: {}", fingerprint);
            RestClient defaultClient = RestClient.create(restTemplate);
            currentLicenseInfo = defaultClient.method(HttpMethod.GET)
                .uri("https://localhost:8081/license")
                .body(LicenseFingerprint.builder().fingerprint(fingerprint).build())
                .accept(APPLICATION_JSON)
                .retrieve()
                .body(LicenseInfo.class);
            log.info("Current license info: {}", currentLicenseInfo);
            return true;
        } catch(Exception exception) {
            log.error("Unable to request license info!", exception);
            return false;
        }
    }

    private String calculateHardwareFingerprint() throws Exception{
        // https://stackoverflow.com/a/38342964
        try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            InetAddress localHost = socket.getLocalAddress();
            String ipAddress = localHost.getHostAddress();
            log.info("IP address: {}", ipAddress);
            NetworkInterface ni = NetworkInterface.getByInetAddress(localHost);
            byte[] hardwareAddress = ni.getHardwareAddress();
            String[] hexadecimal = new String[hardwareAddress.length];
            for (int i = 0; i < hardwareAddress.length; i++) {
                hexadecimal[i] = String.format("%02X", hardwareAddress[i]);
            }
            String macAddress = String.join("-", hexadecimal);
            log.info("MAC address: {}", macAddress);
            // https://stackoverflow.com/a/37705082
            SystemInfo si = new SystemInfo();
            HardwareAbstractionLayer hal = si.getHardware();
            String processorId = hal.getProcessor().getProcessorIdentifier().getProcessorID();
            log.info("Processor ID: {}", processorId);
            return DigestUtils.md5Hex(macAddress + ipAddress + processorId);
        }
    }
}
