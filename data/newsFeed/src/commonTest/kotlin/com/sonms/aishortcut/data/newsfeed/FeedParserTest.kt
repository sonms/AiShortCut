package com.sonms.aishortcut.data.newsfeed

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

// arXiv-style Atom: two entries, second has the multi-<link> shape (alternate +
// pdf) that the parser has to pick the alternate href out of.
private val ATOM = """
<?xml version="1.0" encoding="UTF-8"?>
<feed xmlns="http://www.w3.org/2005/Atom">
  <entry>
    <id>http://arxiv.org/abs/2401.00001v1</id>
    <published>2024-01-02T12:00:00Z</published>
    <updated>2024-01-03T09:00:00Z</updated>
    <title>Scaling Laws &amp; Emergent Behaviour</title>
    <summary>  We study
      scaling   in large models.
    </summary>
    <link href="http://arxiv.org/abs/2401.00001v1" rel="alternate" type="text/html"/>
  </entry>
  <entry>
    <id>http://arxiv.org/abs/2401.00002v1</id>
    <published>2024-01-05T12:00:00Z</published>
    <title>A Second Paper</title>
    <summary>Short abstract.</summary>
    <link href="http://arxiv.org/abs/2401.00002v1" rel="alternate" type="text/html"/>
    <link title="pdf" href="http://arxiv.org/pdf/2401.00002v1" rel="related" type="application/pdf"/>
  </entry>
</feed>
"""

private val RSS = """
<?xml version="1.0"?>
<rss version="2.0"><channel>
  <title>Example AI News</title>
  <item>
    <title><![CDATA[GPT-5 rumours <b>heat up</b>]]></title>
    <link>https://example.com/gpt5</link>
    <description><![CDATA[<p>Sources say &amp; more.</p>]]></description>
    <pubDate>Mon, 23 Jan 2024 18:00:00 GMT</pubDate>
  </item>
</channel></rss>
"""

class FeedParserTest {

    @Test
    fun parsesAtomEntriesAndPicksAlternateLink() {
        val articles = FeedParser.parse(ATOM, "arXiv cs.AI")

        assertEquals(2, articles.size)
        val first = articles[0]
        assertEquals("Scaling Laws & Emergent Behaviour", first.title)
        assertEquals("We study scaling in large models.", first.summary)
        assertEquals("http://arxiv.org/abs/2401.00001v1", first.link)
        assertEquals("2024-01-02T12:00:00Z", first.publishedAt)
        assertEquals("arXiv cs.AI", first.source)

        // Second entry: the <link rel="alternate"> comes first, so that's the one.
        assertEquals("http://arxiv.org/abs/2401.00002v1", articles[1].link)
    }

    @Test
    fun parsesRssItemStrippingCdataAndHtml() {
        val articles = FeedParser.parse(RSS, "Example AI News")

        assertEquals(1, articles.size)
        val item = articles[0]
        assertEquals("GPT-5 rumours heat up", item.title)
        assertEquals("Sources say & more.", item.summary)
        assertEquals("https://example.com/gpt5", item.link)
        assertEquals("Mon, 23 Jan 2024 18:00:00 GMT", item.publishedAt)
    }

    @Test
    fun dropsEntriesMissingTitleOrLinkAndHandlesEmptyFeed() {
        assertTrue(FeedParser.parse("<feed></feed>", "x").isEmpty())

        val partial = """
            <feed><entry><title>No link here</title></entry></feed>
        """
        assertTrue(FeedParser.parse(partial, "x").isEmpty())
    }

    @Test
    fun missingDateIsNull() {
        val noDate = """
            <rss><channel><item>
              <title>T</title><link>https://e.com/a</link>
            </item></channel></rss>
        """
        assertNull(FeedParser.parse(noDate, "x").single().publishedAt)
    }
}
