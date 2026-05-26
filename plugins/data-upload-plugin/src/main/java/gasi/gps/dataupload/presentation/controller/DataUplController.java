package gasi.gps.dataupload.presentation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gasi.gps.dataupload.domain.port.inbound.DataUplService;

@RestController
@RequestMapping("/api/v1/{resource}/upload")
public class DataUplController extends BaseUplController {

    public DataUplController(DataUplService service) {
        super(service);
    }
}
