package com.flashcards.vocab.data

import org.junit.Assert.assertEquals
import org.junit.Test

class CardListImporterTest {

    @Test
    fun `parses comma separated lines`() {
        val result = CardListImporter.parse("ciao,hello\ngrazie,thank you")
        assertEquals(2, result.size)
        assertEquals(ParsedCard("ciao", "hello"), result[0])
        assertEquals(ParsedCard("grazie", "thank you"), result[1])
    }

    @Test
    fun `parses tab separated lines`() {
        val result = CardListImporter.parse("ciao\thello")
        assertEquals(1, result.size)
        assertEquals(ParsedCard("ciao", "hello"), result[0])
    }

    @Test
    fun `captures optional third column as notes`() {
        val result = CardListImporter.parse("casa,house,noun")
        assertEquals(ParsedCard("casa", "house", "noun"), result[0])
    }

    @Test
    fun `skips blank lines and comments`() {
        val result = CardListImporter.parse("ciao,hello\n\n# a comment\ngrazie,thanks")
        assertEquals(2, result.size)
    }

    @Test
    fun `skips a header row`() {
        val result = CardListImporter.parse("word,translation\nciao,hello")
        assertEquals(1, result.size)
        assertEquals("ciao", result[0].front)
    }

    @Test
    fun `ignores lines without a delimiter or a second column`() {
        val result = CardListImporter.parse("just one word\nciao,")
        assertEquals(0, result.size)
    }
}
