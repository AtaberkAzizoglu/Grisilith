package com.azizoglu.grisilith.online

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.JsonReader

/** Client preview only. The same versioned rules are enforced by PostgreSQL. */
class PlayerNamePolicy(bannedWords: List<String>, allowedWords: List<String>) {
    private val banned = bannedWords.map(::filterText).distinct()
    private val whitelist = allowedWords.map(::filterText).distinct()

    fun error(input: String): String? {
        val name = uppercase(input).trim(' ')
        if (name.isEmpty()) return null // Reset to one's immutable public UUID.
        if (name.length !in 3..24) return "Use 3–24 characters."
        if (name.any { !isLetterOrDigit(it) && it !in SYMBOLS }) return "Use English/Turkish letters, numbers or supported symbols."
        if (name.none(::isLetterOrDigit)) return "Include at least one letter or number."
        if (isBlocked(name)) return "This name contains a blocked expression."
        return null
    }

    fun isBlocked(input: String): Boolean {
        val text = filterText(input)
        val tokens = text.split(' ').filter(String::isNotEmpty)
        val candidates = mutableListOf(text)
        var shortRun = ""
        fun flush() { if (shortRun.isNotEmpty()) candidates += shortRun; shortRun = "" }
        for (token in tokens) {
            if (token.length in 1..2) shortRun += token else flush()
        }
        flush()
        return candidates.any { candidate ->
            banned.any { word ->
                occurrences(candidate, word).any { start ->
                    if (word.contains(' ') &&
                        ((start>0 && isLetterOrDigit(candidate[start-1])) ||
                         (start+word.length<candidate.length && isLetterOrDigit(candidate[start+word.length])))) return@any false
                    whitelist.none { safe ->
                        occurrences(candidate, safe).any { safeStart ->
                            // Also supports compound handles such as DARKPHOENIX.
                            // A safe word never exempts a blocked span outside itself.
                            val end = safeStart + safe.length
                            start >= safeStart && start + word.length <= end
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val SYMBOLS = " ._-|!+()[]#@,;:/*\"'"
        fun uppercase(value: String) = value.map { c -> when(c) {
            in 'a'..'z' -> c.uppercaseChar()
            'ç' -> 'Ç'; 'ğ' -> 'Ğ'; 'ı' -> 'I'; 'ö' -> 'Ö'; 'ş' -> 'Ş'; 'ü' -> 'Ü'
            else -> c
        } }.joinToString("")
        private fun isLetter(c: Char) = c in 'A'..'Z' || c in "ÇĞİÖŞÜ"
        private fun isLetterOrDigit(c: Char) = isLetter(c) || c in '0'..'9'
        fun filterText(value: String): String = uppercase(value).replace('İ', 'I')
            .filter { isLetterOrDigit(it) || it == ' ' }.split(' ').filter(String::isNotEmpty).joinToString(" ")
        private fun occurrences(text: String, part: String): Sequence<Int> = sequence {
            var from = 0
            while (from <= text.length - part.length) {
                val at = text.indexOf(part, from)
                if (at < 0) break
                yield(at); from = at + 1
            }
        }
        val packaged: PlayerNamePolicy by lazy {
            val data = JsonReader().parse(Gdx.files.internal("online/name-policy.json"))
            PlayerNamePolicy(data.get("banned").map { it.asString() }, data.get("whitelist").map { it.asString() })
        }
    }
}
