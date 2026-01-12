package com.evidentia.evidence.domain

import java.util.UUID

@JvmInline
value class EvidenceId(val value: UUID = UUID.randomUUID())
