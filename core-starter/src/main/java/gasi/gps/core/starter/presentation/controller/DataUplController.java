package gasi.gps.core.starter.presentation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gasi.gps.core.api.domain.port.inbound.DataUplService;

@RestController
@RequestMapping("/api/v1/{resource}/upl")
public class DataUplController extends BaseUplController {

    public DataUplController(DataUplService service) {
        super(service);
    }
}
