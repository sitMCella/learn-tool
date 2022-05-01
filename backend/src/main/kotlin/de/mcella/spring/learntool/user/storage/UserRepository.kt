package de.mcella.spring.learntool.user.storage

import java.util.Optional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<UserEntity, Long> {

    fun findByEmail(email: String): Optional<UserEntity>

    fun existsByEmail(email: String): Boolean
}
