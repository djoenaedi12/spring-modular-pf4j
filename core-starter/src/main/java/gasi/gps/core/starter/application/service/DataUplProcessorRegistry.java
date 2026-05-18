package gasi.gps.core.starter.application.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import gasi.gps.core.api.application.exception.BusinessException;
import gasi.gps.core.api.domain.port.inbound.DataUplProcessor;

/**
 * Registry for resource-specific upload processors.
 *
 * @since 1.0.0
 */
@Component
public class DataUplProcessorRegistry {

    private final Map<String, DataUplProcessor> processors;

    /**
     * Creates a registry from all processor beans.
     *
     * @param processors upload processors contributed by modules
     */
    public DataUplProcessorRegistry(List<DataUplProcessor> processors) {
        this.processors = processors.stream()
                .collect(Collectors.toMap(
                        processor -> normalize(processor.resource()),
                        Function.identity()));
    }

    /**
     * Finds a processor for a resource.
     *
     * @param resource resource code
     * @return matching upload processor
     */
    public DataUplProcessor get(String resource) {
        DataUplProcessor processor = processors.get(normalize(resource));
        if (processor == null) {
            throw new BusinessException("Unsupported upload resource: " + resource);
        }
        return processor;
    }

    private String normalize(String resource) {
        return resource == null ? "" : resource.trim().toLowerCase();
    }
}
