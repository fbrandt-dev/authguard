package io.cscenter.authguard.http.response;

import java.util.Map;

import org.springframework.http.HttpStatus;

import io.cscenter.shared.dto.enums.HttpStatusType;
import lombok.Builder;

@Builder
public class ServiceFailure {

    private String languageKey;
    private HttpStatusType statusType;
    private Map<String, Object> data;
    private HttpStatus status;
    private String traceIdentifier;

    /**
     * @return String return the languageKey
     */
    public String getLanguageKey() {
        return languageKey;
    }

    /**
     * @param languageKey the languageKey to set
     */
    public void setLanguageKey(String languageKey) {
        this.languageKey = languageKey;
    }

    /**
     * @return StatusType return the statusType
     */
    public HttpStatusType getStatusType() {
        return statusType;
    }

    /**
     * @param statusType the statusType to set
     */
    public void setHttpStatusType(HttpStatusType statusType) {
        this.statusType = statusType;
    }

    /**
     * @return Map<String, String> return the data
     */
    public Map<String, Object> getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    /**
     * @return HttpStatus return the status
     */
    public Integer getStatusCode() {
        return status.value();
    }

    /**
     * @param status the status to set
     */
    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    /**
     * @return String return the traceIdentifier
     */
    public String getTraceIdentifier() {
        return traceIdentifier;
    }

    /**
     * @param traceIdentifier the traceIdentifier to set
     */
    public void setTraceIdentifier(String traceIdentifier) {
        this.traceIdentifier = traceIdentifier;
    }

}
