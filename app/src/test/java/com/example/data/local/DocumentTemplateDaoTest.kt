package com.example.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.dao.DocumentTemplateDao
import com.example.data.local.entity.DocumentTemplateEntity
import com.example.data.repository.DocumentTemplateRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class DocumentTemplateDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: DocumentTemplateDao
    private lateinit var repository: DocumentTemplateRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.documentTemplateDao()
        repository = DocumentTemplateRepository(dao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveTemplate() = runBlocking {
        val template = DocumentTemplateEntity(
            title = "Test Non-Disclosure Agreement",
            docType = "NDA",
            jurisdiction = "California",
            description = "Confidentiality terms",
            content = "CONFIDENTIALITY CLAUSE: Both parties agree to protect proprietary materials.",
            isBuiltIn = false
        )

        val id = repository.saveTemplate(template)
        assertTrue(id > 0)

        val retrieved = repository.getTemplateById(id.toInt())
        assertNotNull(retrieved)
        assertEquals("Test Non-Disclosure Agreement", retrieved?.title)
        assertEquals("NDA", retrieved?.docType)
        assertEquals("California", retrieved?.jurisdiction)
    }

    @Test
    fun observeAllTemplates() = runBlocking {
        val t1 = DocumentTemplateEntity(
            title = "Contract A",
            docType = "CONTRACT",
            jurisdiction = "Delaware",
            description = "Commercial Contract",
            content = "Terms A",
            isBuiltIn = false
        )
        val t2 = DocumentTemplateEntity(
            title = "Motion B",
            docType = "MOTION",
            jurisdiction = "Federal",
            description = "Civil Motion",
            content = "Terms B",
            isBuiltIn = true
        )

        repository.saveTemplate(t1)
        repository.saveTemplate(t2)

        val list = repository.allTemplates.first()
        assertEquals(2, list.size)
        // isBuiltIn = true should be first due to ORDER BY isBuiltIn DESC
        assertEquals("Motion B", list[0].title)
        assertEquals("Contract A", list[1].title)
    }

    @Test
    fun filterTemplatesByType() = runBlocking {
        val t1 = DocumentTemplateEntity(
            title = "NDA Doc",
            docType = "NDA",
            jurisdiction = "California",
            description = "NDA Desc",
            content = "NDA Content",
            isBuiltIn = false
        )
        val t2 = DocumentTemplateEntity(
            title = "Contract Doc",
            docType = "CONTRACT",
            jurisdiction = "New York",
            description = "Contract Desc",
            content = "Contract Content",
            isBuiltIn = false
        )

        repository.saveTemplate(t1)
        repository.saveTemplate(t2)

        val ndas = repository.getTemplatesByType("NDA").first()
        assertEquals(1, ndas.size)
        assertEquals("NDA Doc", ndas[0].title)
    }

    @Test
    fun deleteTemplate() = runBlocking {
        val template = DocumentTemplateEntity(
            title = "To Delete",
            docType = "BRIEF",
            jurisdiction = "US",
            description = "Desc",
            content = "Content",
            isBuiltIn = false
        )

        val id = repository.saveTemplate(template)
        val inserted = repository.getTemplateById(id.toInt())
        assertNotNull(inserted)

        repository.deleteTemplate(inserted!!)
        val afterDelete = repository.getTemplateById(id.toInt())
        assertNull(afterDelete)
    }

    @Test
    fun seedDefaultsIfEmpty() = runBlocking {
        assertEquals(0, dao.getCount())

        repository.seedDefaultsIfEmpty()

        val count = dao.getCount()
        assertTrue(count >= 5)

        val all = repository.allTemplates.first()
        assertTrue(all.any { it.docType == "NDA" })
        assertTrue(all.any { it.docType == "CONTRACT" })
        assertTrue(all.any { it.docType == "MOTION" })
    }

    @Test
    fun searchTemplatesByTitleAndKeywords() = runBlocking {
        repository.seedDefaultsIfEmpty()

        // Search by title keyword "Mutual"
        val titleMatch = repository.searchTemplates("Mutual").first()
        assertTrue(titleMatch.isNotEmpty())
        assertTrue(titleMatch.any { it.title.contains("Mutual", ignoreCase = true) })

        // Search by content keyword "indemnify"
        val contentMatch = repository.searchTemplates("indemnify").first()
        assertTrue(contentMatch.isNotEmpty())
        assertTrue(contentMatch.any { it.content.contains("indemnify", ignoreCase = true) })

        // Search by jurisdiction keyword "Delaware"
        val jurisdictionMatch = repository.searchTemplates("Delaware").first()
        assertTrue(jurisdictionMatch.isNotEmpty())
        assertTrue(jurisdictionMatch.any { it.jurisdiction.contains("Delaware", ignoreCase = true) })

        // Search with non-matching query
        val emptyMatch = repository.searchTemplates("xyznonexistentterm999").first()
        assertTrue(emptyMatch.isEmpty())
    }
}
