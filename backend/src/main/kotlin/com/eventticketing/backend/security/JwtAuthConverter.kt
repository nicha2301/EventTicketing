package com.eventticketing.backend.security

import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component

@Component
class JwtAuthConverter : Converter<Jwt, Collection<GrantedAuthority>> {
    
    override fun convert(jwt: Jwt): Collection<GrantedAuthority> {
        val authorities = mutableListOf<GrantedAuthority>()
        
        val realmAccess = jwt.claims["realm_access"] as? Map<*, *>
        val roles = realmAccess?.get("roles") as? List<*>
        roles?.forEach { role ->
            authorities.add(SimpleGrantedAuthority("ROLE_${role.toString().uppercase()}"))
        }
        
        val resourceAccess = jwt.claims["resource_access"] as? Map<*, *>
        val clientAccess = resourceAccess?.get("event-ticketing-client") as? Map<*, *>
        val clientRoles = clientAccess?.get("roles") as? List<*>
        clientRoles?.forEach { role ->
            authorities.add(SimpleGrantedAuthority("ROLE_${role.toString().uppercase()}"))
        }
        
        if (authorities.isEmpty()) {
            authorities.add(SimpleGrantedAuthority("ROLE_USER"))
        }
        
        return authorities
    }
} 