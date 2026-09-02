package com.evidentia.common.security

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.web.client.RestClient

fun interface ServiceAccessTokenProvider {
    fun getAccessToken(clientRegistrationId: String): String
}

class AuthenticatedServiceRestClientFactory(
    private val accessTokenProvider: ServiceAccessTokenProvider,
) {
    fun create(baseUrl: String, clientRegistrationId: String): RestClient =
        RestClient.builder()
            .baseUrl(baseUrl)
            .requestInterceptor { request, body, execution ->
                request.headers.setBearerAuth(accessTokenProvider.getAccessToken(clientRegistrationId))
                execution.execute(request, body)
            }
            .build()
}

@Configuration
class ServiceClientSecurity {
    @Bean
    @ConditionalOnProperty(name = ["evidentia.service-auth.enabled"], havingValue = "true")
    fun serviceAuthorizedClientManager(
        clientRegistrationRepository: ClientRegistrationRepository,
        authorizedClientService: OAuth2AuthorizedClientService,
    ): OAuth2AuthorizedClientManager {
        val provider = OAuth2AuthorizedClientProviderBuilder.builder()
            .clientCredentials()
            .build()
        return AuthorizedClientServiceOAuth2AuthorizedClientManager(
            clientRegistrationRepository,
            authorizedClientService,
        ).also { it.setAuthorizedClientProvider(provider) }
    }

    @Bean
    @ConditionalOnProperty(name = ["evidentia.service-auth.enabled"], havingValue = "true")
    fun oauth2ServiceAccessTokenProvider(
        authorizedClientManager: OAuth2AuthorizedClientManager,
    ): ServiceAccessTokenProvider = ServiceAccessTokenProvider { clientRegistrationId ->
        val request = OAuth2AuthorizeRequest.withClientRegistrationId(clientRegistrationId)
            .principal("evidentia-service")
            .build()
        authorizedClientManager.authorize(request)?.accessToken?.tokenValue
            ?: throw IllegalStateException("Unable to authorize service client '$clientRegistrationId'")
    }

    @Bean
    @ConditionalOnMissingBean(ServiceAccessTokenProvider::class)
    fun disabledServiceAccessTokenProvider(): ServiceAccessTokenProvider =
        ServiceAccessTokenProvider {
            throw IllegalStateException(
                "Service authentication is disabled; set EVIDENTIA_SERVICE_AUTH_ENABLED=true and configure OAuth2 client credentials",
            )
        }

    @Bean
    fun authenticatedServiceRestClientFactory(
        accessTokenProvider: ServiceAccessTokenProvider,
    ) = AuthenticatedServiceRestClientFactory(accessTokenProvider)
}
