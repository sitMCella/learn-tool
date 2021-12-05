package de.mcella.spring.learntool

import de.mcella.spring.learntool.card.storage.CardEntity
import javax.persistence.EntityManager
import org.hibernate.search.mapper.orm.Search
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class HibernateSearchInitialization(private val entityManager: EntityManager) : ApplicationListener<ContextRefreshedEvent> {

    @Transactional
    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        logger.warn("Hibernate Search initialization starting.")
        val searchSession = Search.session(entityManager)
        val indexer = searchSession
                .massIndexer(CardEntity::class.java)
                .threadsToLoadObjects(7)
        indexer.startAndWait()
        logger.warn("Hibernate Search initialization completed.")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}
