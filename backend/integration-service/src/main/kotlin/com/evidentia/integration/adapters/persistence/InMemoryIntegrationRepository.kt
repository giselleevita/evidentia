package com.evidentia.integration.adapters.persistence

import com.evidentia.common.domain.TenantId
import com.evidentia.integration.application.IntegrationRepository
import com.evidentia.integration.domain.Integration
import com.evidentia.integration.domain.IntegrationId
import com.evidentia.integration.domain.IntegrationType
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class InMemoryIntegrationRepository : IntegrationRepository {
    private val storage = ConcurrentHashMap<Pair<IntegrationId, TenantId>, Integration>()
    
    override fun save(integration: Integration): Integration {
        storage[Pair(integration.id, integration.tenantId)] = integration
        return integration
    }
    
    override fun findById(id: IntegrationId, tenantId: TenantId): Integration? {
        return storage[Pair(id, tenantId)]
    }
    
    override fun findAll(tenantId: TenantId): List<Integration> {
        return storage.values.filter { it.tenantId == tenantId }
    }
    
    override fun findByType(tenantId: TenantId, type: IntegrationType): List<Integration> {
        return storage.values.filter { it.tenantId == tenantId && it.type == type }
    }
    
    override fun delete(id: IntegrationId, tenantId: TenantId): Boolean {
        return storage.remove(Pair(id, tenantId)) != null
    }
}
