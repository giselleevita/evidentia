package com.evidentia.incident.domain

import java.util.UUID

@JvmInline
value class IncidentId(val value: UUID = UUID.randomUUID())
