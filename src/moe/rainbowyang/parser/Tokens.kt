package moe.rainbowyang.parser

import moe.rainbowyang.parser.TokenType.*

abstract class Token(val type: TokenType, val value: String = "") {
    override fun toString() = "<$type, $value>"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Token) return false

        if (type != other.type) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }
}

class NonTerminalToken(element: String) : Token(NON_TERMINAL, element) {
    override fun toString(): String {
        return value
    }
}

abstract class TerminalToken(type: TokenType, value: String = "") : Token(type, value)

class KeywordToken(keyword: String) : TerminalToken(KEYWORD, keyword)

open class IDToken(id: String) : TerminalToken(ID, id) {
    companion object {
        val Wildcard = IDTokenWildcard
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IDToken) return false
        if (this is IDTokenWildcard || other is IDTokenWildcard) return true
        if (!super.equals(other)) return false
        return true
    }

    override fun hashCode() = 0
}

object IDTokenWildcard : IDToken("id")

class NumToken(num: String) : TerminalToken(NUM, num)

class SymbolToken(symbol: String) : TerminalToken(SYMBOL, symbol)
class DelimiterToken(symbol: String) : TerminalToken(DELIMITER, symbol)

object EmptyToken : TerminalToken(EMPTY, "ε")
object EndToken : TerminalToken(END, "$")

enum class TokenType(val type: String) {
    KEYWORD("Keyword"),
    ID("ID"),
    NUM("Num"),
    SYMBOL("Symbol"),
    DELIMITER("Delimiter"),
    NON_TERMINAL("Non-terminal"),
    EMPTY("Empty"),
    END("End")
}

val E = NonTerminalToken("E")
val E1 = NonTerminalToken("E'")
val T = NonTerminalToken("T")
val T1 = NonTerminalToken("T'")
val F = NonTerminalToken("F")
val A = NonTerminalToken("A")
val M = NonTerminalToken("M")
val S = NonTerminalToken("S")
val S1 = NonTerminalToken("S'")

val left = DelimiterToken("(")
val right = DelimiterToken(")")
val add = SymbolToken("+")
val sub = SymbolToken("-")
val times = SymbolToken("*")
val div = SymbolToken("/")
val assign = SymbolToken(":=")

val id = IDToken.Wildcard
val empty = EmptyToken
val end = EndToken

