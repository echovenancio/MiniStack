package com.echovenancio.ministack.models;

import java.util.Optional;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateReplyRequest {
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters long")
    private String title;
    @Size(min = 10, message = "Body must be at least 10 characters long")
    private String body;
    @Size(min = 1, max = 5, message = "Tags must be between 1 and 5 items")
    private String[] tags;

    private Long parentReplyId; // Optional, for nested replies
}
