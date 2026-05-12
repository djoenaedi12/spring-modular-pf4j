package gasi.gps.core.api.id;

import java.util.List;

/**
 * Contract for converting internal numeric IDs to public identifiers.
 *
 * <p>The implementation may use Sqids, Hashids, or another reversible encoding
 * strategy. This interface belongs in {@code core-api} so application code can
 * depend on the contract without depending on a specific encoding library.</p>
 *
 * @since 1.0.0
 */
public interface IdCodec {

    /**
     * Encodes a database identifier for public API responses.
     *
     * @param id internal numeric identifier
     * @return encoded identifier, or {@code null} when {@code id} is {@code null}
     */
    String encode(Long id);

    /**
     * Decodes a public identifier back to its internal numeric value.
     *
     * @param encodedId public identifier from a request
     * @return internal numeric identifier, or {@code null} when
     *         {@code encodedId} is blank or {@code null}
     * @throws IllegalArgumentException if the encoded value is malformed
     */
    Long decode(String encodedId);

    /**
     * Encodes a list of database identifiers.
     *
     * @param ids internal numeric identifiers
     * @return encoded identifiers, or {@code null} when {@code ids} is
     *         {@code null}
     */
    List<String> encodeList(List<Long> ids);

    /**
     * Decodes a list of public identifiers.
     *
     * @param encodedIds public identifiers
     * @return internal numeric identifiers, or {@code null} when
     *         {@code encodedIds} is {@code null}
     * @throws IllegalArgumentException if any encoded value is malformed
     */
    List<Long> decodeList(List<String> encodedIds);
}
