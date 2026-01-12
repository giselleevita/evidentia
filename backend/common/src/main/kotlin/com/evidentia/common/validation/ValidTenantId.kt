package com.evidentia.common.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * Validates that a string is a valid tenant ID (non-blank UUID or identifier).
 */
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [TenantIdValidator::class])
annotation class ValidTenantId(
    val message: String = "Invalid tenant ID",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class TenantIdValidator : ConstraintValidator<ValidTenantId, String> {
    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        return value != null && value.isNotBlank()
    }
}
