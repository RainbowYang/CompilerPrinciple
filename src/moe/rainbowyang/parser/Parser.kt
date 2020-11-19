package moe.rainbowyang.parser

/**
 * @author: Rainbow Yang
 * @create: 2020-05-25 23:16
 **/
abstract class Parser(val productions: List<Production>) {
    abstract fun parse(tokens: List<Token>): Boolean
}

fun main() {
    val tokens = Lexer.process("i*(i-i)/(i+i)*((i+i-i*i/i))")
    println(ParserLL1().parse(tokens))
}