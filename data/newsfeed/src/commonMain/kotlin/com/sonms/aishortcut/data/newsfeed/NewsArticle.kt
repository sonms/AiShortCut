package com.sonms.aishortcut.data.newsfeed

// Domain type the rest of the app consumes. No serialization / transport
// concern -- FeedParser turns raw feed XML straight into this. (The *Dto.kt
// split the other data modules follow is a kotlinx.serialization pattern;
// there's no XML serializer in play here, so there's no DTO layer to separate.)
data class NewsArticle(
    val title: String,
    val summary: String,
    val link: String,
    val source: String,
    // Raw date string as the feed publishes it (RFC-822 for RSS, ISO-8601 for
    // Atom). Left unparsed until something actually needs to sort or format it.
    val publishedAt: String?,
    // Topic tags, for keyword clustering on Home. HF daily papers carry these in
    // `ai_keywords`; the arXiv/RSS parser has no equivalent and leaves it empty.
    val keywords: List<String> = emptyList(),
)
