package io.logto.sdk.core.exception

class UriConstructionException(
    type: Type,
    cause: Throwable? = null,
) : RuntimeException(type.name, cause) {
    enum class Type {
        INVALID_ENDPOINT,
    }
}
