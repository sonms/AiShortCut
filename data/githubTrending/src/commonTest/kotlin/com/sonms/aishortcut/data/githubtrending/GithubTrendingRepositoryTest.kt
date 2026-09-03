package com.sonms.aishortcut.data.githubtrending

import com.sonms.aishortcut.core.network.createHttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

// Trimmed from a real GET /search/repositories response. First item keeps the
// extra fields the API sends; second omits description/language/topics, which
// are all absent on some real repos.
private const val SAMPLE_RESPONSE = """
{"total_count":9473,"incomplete_results":false,"items":[
    {"id":614765452,"full_name":"Significant-Gravitas/AutoGPT","name":"AutoGPT","description":"Accessible AI for everyone","html_url":"https://github.com/Significant-Gravitas/AutoGPT","stargazers_count":187090,"forks_count":46041,"language":"Python","topics":["ai","artificial-intelligence","agents"],"owner":{"login":"Significant-Gravitas"}},
    {"id":42,"full_name":"acme/tiny-agent","html_url":"https://github.com/acme/tiny-agent","stargazers_count":312,"forks_count":9}
]}
"""

class GithubTrendingRepositoryTest {
    @Test
    fun searchesAiReposByRecencyAndStarsThenMapsToDomain() = runTest {
        lateinit var url: Url
        val engine = MockEngine { request ->
            url = request.url
            respond(
                content = SAMPLE_RESPONSE,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val repos = GithubTrendingRepository(createHttpClient(engine)).getTrendingRepos(limit = 10)

        val query = assertNotNull(url.parameters["q"])
        assertTrue("topic:artificial-intelligence" in query, query)
        assertTrue("created:>" in query, query)
        assertEquals("stars", url.parameters["sort"])
        assertEquals("10", url.parameters["per_page"])

        assertEquals(2, repos.size)
        assertEquals("Significant-Gravitas/AutoGPT", repos[0].fullName)
        assertEquals(listOf("ai", "artificial-intelligence", "agents"), repos[0].topics)
        assertNull(repos[1].description)
        assertEquals(emptyList(), repos[1].topics)
    }
}
