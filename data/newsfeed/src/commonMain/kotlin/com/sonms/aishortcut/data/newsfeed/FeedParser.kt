package com.sonms.aishortcut.data.newsfeed

// ponytail: regex feed reader, not a real XML parser. Handles well-formed
// RSS 2.0 <item> and Atom <entry> with CDATA sections and the standard XML
// entities -- enough for arXiv and mainstream news feeds. If a feed shows up
// that this mangles, swap in a KMP XML library (nl.adaptivity.xmlutil) behind
// this same `parse` signature.
//
// `[\s\S]` rather than `.` throughout: RegexOption.DOT_MATCHES_ALL is JVM-only,
// and this parser lives in commonMain.
internal object FeedParser {

    private val ITEM = Regex("<(item|entry)\\b[^>]*>([\\s\\S]*?)</\\1>", RegexOption.IGNORE_CASE)
    private val LINK_HREF = Regex("""<link\b[^>]*\bhref=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
    private val CDATA = Regex("<!\\[CDATA\\[([\\s\\S]*?)]]>")
    private val HTML_TAG = Regex("<[^>]+>")
    private val WHITESPACE = Regex("\\s+")

    fun parse(xml: String, source: String): List<NewsArticle> =
        ITEM.findAll(xml).mapNotNull { match ->
            val body = match.groupValues[2]
            val title = firstTag(body, "title").clean()
            val link = LINK_HREF.find(body)?.groupValues?.get(1)?.trim()
                ?: firstTag(body, "link").clean()
            if (title.isEmpty() || link.isEmpty()) return@mapNotNull null

            NewsArticle(
                title = title,
                summary = firstTag(body, "summary")
                    .ifEmpty { firstTag(body, "description") }
                    .ifEmpty { firstTag(body, "content") }
                    .clean(),
                link = link,
                source = source,
                publishedAt = firstTag(body, "pubDate")
                    .ifEmpty { firstTag(body, "published") }
                    .ifEmpty { firstTag(body, "updated") }
                    .trim()
                    .ifEmpty { null },
            )
        }.toList()

    private fun firstTag(body: String, name: String): String =
        Regex("<$name\\b[^>]*>([\\s\\S]*?)</$name>", RegexOption.IGNORE_CASE)
            .find(body)?.groupValues?.get(1).orEmpty()

    private fun String.clean(): String {
        var s = CDATA.replace(trim()) { it.groupValues[1] }
        s = HTML_TAG.replace(s, "")          // strip literal HTML (Atom summaries carry it)
        s = s.decodeXmlEntities()
        s = HTML_TAG.replace(s, "")          // and again for HTML that was entity-escaped
        return WHITESPACE.replace(s, " ").trim()
    }

    private fun String.decodeXmlEntities(): String =
        replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            .replace("&amp;", "&")           // last, so "&amp;lt;" decodes to the text "&lt;"
}
