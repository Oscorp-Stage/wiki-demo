package org.wikipedia.search

import kotlinx.serialization.Serializable
import org.wikipedia.dataclient.WikiSite
import org.wikipedia.dataclient.mwapi.MwQueryPage
import org.wikipedia.dataclient.mwapi.MwQueryResponse

@Serializable
data class SearchResults constructor(var results: MutableList<SearchResult> = mutableListOf(),
                                     var continuation: MwQueryResponse.Continue? = null) {
    constructor(pages: List<MwQueryPage>, wiki: WikiSite, continuation: MwQueryResponse.Continue?) : this() {
        // Sort the array based on the "index" property
        results.addAll(pages.sortedBy { it.index }.map { SearchResult(it, wiki, it.coordinates) })
        this.continuation = continuation
    }
}
