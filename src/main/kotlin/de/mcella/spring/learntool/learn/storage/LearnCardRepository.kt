package de.mcella.spring.learntool.learn.storage

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LearnCardRepository : JpaRepository<LearnCard, String>
