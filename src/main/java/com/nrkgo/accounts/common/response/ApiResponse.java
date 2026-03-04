package com.nrkgo.accounts.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.domain.Page;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private PageableInfo pageable;
    private Integer totalPages;

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PageableInfo {
        private int perPage;
        private long count; // Elements on current page
        private Integer nextPage;
        private boolean moreRecords;

        public int getPerPage() {
            return perPage;
        }

        public void setPerPage(int perPage) {
            this.perPage = perPage;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }

        public Integer getNextPage() {
            return nextPage;
        }

        public void setNextPage(Integer nextPage) {
            this.nextPage = nextPage;
        }

        public boolean isMoreRecords() {
            return moreRecords;
        }

        public void setMoreRecords(boolean moreRecords) {
            this.moreRecords = moreRecords;
        }
    }

    // Manual No-Args Constructor
    public ApiResponse() {
    }

    // Manual All-Args Constructor
    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // Manual Getters and Setters (Required for Jackson JSON serialization)
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public PageableInfo getPageable() {
        return pageable;
    }

    public void setPageable(PageableInfo pageable) {
        this.pageable = pageable;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    // Static Factory Methods
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }

    public static <T> ApiResponse<java.util.List<T>> paginatedSuccess(String message, Page<T> page) {
        ApiResponse<java.util.List<T>> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage(message);
        response.setData(page.getContent());
        response.setTotalPages(page.getTotalPages());

        PageableInfo info = new PageableInfo();
        info.setPerPage(page.getSize());
        info.setCount(page.getNumberOfElements());
        info.setMoreRecords(page.hasNext());

        // Return 1-based page indexing for next_page to match user request
        if (page.hasNext()) {
            info.setNextPage(page.getNumber() + 2); // page.getNumber() is 0-based current, +1 is 1-based current, +2 is
                                                    // 1-based next
        }

        response.setPageable(info);
        return response;
    }
}
