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
public class UpdateReplyRequest {

    @Size(min = 10, message = "Body must be at least 10 characters long")
    private String body;

}
