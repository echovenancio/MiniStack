package com.echovenancio.ministack.utils;

import com.fasterxml.jackson.annotation.JsonIgnore; 
import com.fasterxml.jackson.annotation.JsonProperty; 

public sealed interface Result<T, E> permits Result.Success, Result.Error {

    record Success<T, E>(@JsonProperty("value") T value) implements Result<T, E> {
    }

    record Error<T, E>(@JsonProperty("error") E error) implements Result<T, E> {
    }

    static <T, E> Result<T, E> error(E error) {
        return new Error<>(error);
    }

    static <T, E> Result<T, E> success(T value) {
        return new Success<>(value);
    }


    @JsonIgnore
    default boolean isSuccess() {
        return this instanceof Success<T, E>;
    }

    @JsonIgnore
    default boolean isError() {
        return this instanceof Error<T, E>;
    }

    @JsonIgnore
    default T getSuccess() {
        if (this instanceof Success<T, E> success) {
            return success.value();
        }
        throw new IllegalStateException("Called getSuccess() on an Error Result.");
    }

    @JsonIgnore
    default E getError() {
        if (this instanceof Error<T, E> error) {
            return error.error();
        }
        throw new IllegalStateException("Called getError() on a Success Result.");
    }

}
