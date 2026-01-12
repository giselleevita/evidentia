package com.evidentia.common.domain

@JvmInline
value class TenantId(val value: String) {
    init {
        require(value.isNotBlank()) { "TenantId cannot be blank" }
    }
}
