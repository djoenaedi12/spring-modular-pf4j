package gasi.gps.core.api.domain.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Framework-neutral page result.
 *
 * <p>This type keeps pagination contracts out of Spring Data so services and
 * plugin contracts can stay independent from a specific persistence framework.</p>
 *
 * @param <T> the content type
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    /**
     * Indicates whether there is another page after the current page.
     *
     * @return {@code true} when a next page exists
     */
    public boolean hasNext() {
        return page < totalPages - 1;
    }

    /**
     * Indicates whether there is a page before the current page.
     *
     * @return {@code true} when a previous page exists
     */
    public boolean hasPrevious() {
        return page > 0;
    }
}
