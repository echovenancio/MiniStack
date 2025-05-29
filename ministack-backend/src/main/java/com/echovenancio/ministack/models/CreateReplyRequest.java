package com.echovenancio.ministack.models;

import java.util.Optional;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
public class CreateReplyRequest {

    @Size(min = 10, message = "Body must be at least 10 characters long")
    private String body;

    private Long parentReplyId; // Optional, for nested replies

    public CreateReplyRequest() {
    }

    public CreateReplyRequest(String body, Long parentReplyId) {
        this.body = body;
        this.parentReplyId = parentReplyId;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Long getParentReplyId() {
        return parentReplyId;
    }

    public void setParentReplyId(Long parentReplyId) {
        this.parentReplyId = parentReplyId;
    }

}
