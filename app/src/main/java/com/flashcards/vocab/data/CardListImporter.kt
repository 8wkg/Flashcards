package com.flashcards.vocab.data

data class ParsedCard(val front: String, val back: String, val notes: String? = null)

/**
 * Parses an uploaded word list into cards. Accepts plain text where each line is a word
 * pair separated by a comma, tab, or semicolon (e.g. "ciao,hello" or "ciao\thello"), with an
 * optional third column used as a note. Blank lines, "#" comments, and an optional header
 * row (e.g. "word,translation") are ignored.
 */
object CardListImporter {
    private val headerWords = setOf(
        "word", "front", "italian", "translation", "back", "english", "meaning", "term", "definition"
    )

    fun parse(text: String): List<ParsedCard> {
        return text.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .mapNotNull(::parseLine)
    }

    private fun parseLine(line: String): ParsedCard? {
        val delimiter = when {
            line.contains('\t') -> '\t'
            line.contains(',') -> ','
            line.contains(';') -> ';'
            else -> return null
        }
        val parts = line.split(delimiter).map { it.trim() }
        if (parts.size < 2 || parts[0].isEmpty() || parts[1].isEmpty()) return null
        if (looksLikeHeader(parts)) return null

        return ParsedCard(
            front = parts[0],
            back = parts[1],
            notes = parts.getOrNull(2)?.takeIf { it.isNotEmpty() }
        )
    }

    private fun looksLikeHeader(parts: List<String>): Boolean =
        parts[0].lowercase() in headerWords && parts.getOrNull(1)?.lowercase() in headerWords
}
