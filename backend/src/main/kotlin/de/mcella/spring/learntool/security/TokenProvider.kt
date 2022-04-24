package de.mcella.spring.learntool.security

import de.mcella.spring.learntool.config.AppProperties
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.SignatureException
import java.util.Date
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service

@Service
class TokenProvider(val appProperties: AppProperties) {
    fun createToken(authentication: Authentication): String {
        val userPrincipal: UserPrincipal = authentication.principal as UserPrincipal
        val now = Date()
        val expiryDate = Date(now.time + appProperties.auth.tokenExpirationMsec)
        return Jwts.builder()
                .setSubject((userPrincipal.id!!).toString())
                .setIssuedAt(Date())
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, appProperties.auth.tokenSecret)
                .compact()
    }

    fun getUserIdFromToken(token: String): String? {
        val claims = Jwts.parser()
                .setSigningKey(appProperties.auth.tokenSecret)
                .parseClaimsJws(token)
                .body
        return claims.subject
    }

    fun validateToken(authToken: String): Boolean {
        try {
            Jwts.parser().setSigningKey(appProperties.auth.tokenSecret).parseClaimsJws(authToken)
            return true
        } catch (e: SignatureException) {
            logger.error("Invalid JWT signature.", e)
        } catch (e: MalformedJwtException) {
            logger.error("Invalid JWT token.", e)
        } catch (e: ExpiredJwtException) {
            logger.error("Expired JWT token.", e)
        } catch (e: UnsupportedJwtException) {
            logger.error("Unsupported JWT token.", e)
        } catch (e: IllegalArgumentException) {
            logger.error("JWT claims string is empty.", e)
        }
        return false
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}
