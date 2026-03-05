package gasi.gps.core.api.infrastructure.util;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.sqids.Sqids;

/**
 * Utility for encoding/decoding Long IDs to/from URL-safe hashed strings.
 *
 * <p>
 * Uses <a href="https://sqids.org">Sqids</a> library for reversible,
 * non-sequential ID obfuscation.
 * </p>
 */
@Component
public class IdEncoder {

    private final Sqids sqids;

    /**
     * Constructs an IdHasher with default settings
     * (minLength=8, default alphabet).
     */
    public IdEncoder(@Value("${app.id.salt}") String alphabet) {
        this.sqids = Sqids.builder()
                .minLength(8)
                .alphabet(alphabet)
                .build();
    }

    /**
     * Encodes a Long ID to a hashed string.
     *
     * @param id the numeric ID to encode
     * @return the hashed string representation
     */
    @Named("encodeId")
    public String encode(Long id) {
        if (id == null) {
            return null;
        }
        return sqids.encode(List.of(id));
    }

    /**
     * Decodes a hashed string back to a Long ID.
     *
     * @param hash the hashed string to decode
     * @return the original numeric ID
     * @throws IllegalArgumentException if the hash is invalid
     */
    @Named("decodeId")
    public Long decode(String hash) {
        if (hash == null || hash.isEmpty()) {
            return null;
        }
        List<Long> decoded = sqids.decode(hash);
        if (decoded.isEmpty()) {
            throw new IllegalArgumentException("Invalid hash ID: " + hash);
        }
        return decoded.get(0);
    }

    @Named("decodeIds")
    public List<Long> decodeList(List<String> encodedIds) {
        if (encodedIds == null) {
            return null;
        }
        return encodedIds.stream()
                .map(this::decode)
                .collect(Collectors.toList());
    }

    @Named("encodeIds")
    public List<String> encodeList(List<Long> ids) {
        if (ids == null) {
            return null;
        }
        return ids.stream()
                .map(this::encode)
                .collect(Collectors.toList());
    }
}
