package moe.rainbowyang.parser

import java.util.*

fun main() {
    val tokens = Lexer.process("a+b+c")
    ParserLL1().parse(tokens)
}

class ParserLL1(productions: List<Production> = productionsExample) : Parser(productions) {
    companion object {
        private val productionsExample = listOf(
            E.`→`(T, E1),
            E1.`→`(A, T, E1),
            E1.`→`(empty),
            T.`→`(F, T1),
            T1.`→`(M, F, T1),
            T1.`→`(empty),
            F.`→`(left, E, right),
            F.`→`(id),
            A.`→`(add),
            A.`→`(sub),
            M.`→`(times),
            M.`→`(div)
        )
    }

    val nullable by lazy(::generateNullable)
    val first by lazy(::generateFirst)
    val follow by lazy(::generateFollow)
    val select by lazy(::generateSelect)
    val analysisTable by lazy(::generateAnalysisTable)

    operator fun Production.component1() = this.head
    operator fun Production.component2() = this.body

    private fun generateNullable() =
        mutableSetOf<NonTerminalToken>().apply {
            whileChanged { changing ->
                productions.filter { (head, body) ->
                    body.all { (it in this || it == empty) && head !in this }
                }.forEach {
                    add(it.head)
                    changing()
                }
            }
        }.toSet()

    private fun generateFirst() =
        mutableMapOf<NonTerminalToken, MutableSet<Token>>().apply {
            productions.forEach { (head, body) ->
                body.subList(0, body.indexOfFirst { it !in nullable } + 1).forEach { token ->
                    getOrPut(head, ::mutableSetOf).add(token) // 如果不存在 就先初始化put
                }
            }
            // 将所有的NonTerminalToken全部替代
            whileChanged { changing ->
                forEach { (ntt, tokens) ->
                    tokens.filterIsInstance<NonTerminalToken>().forEach {
                        this[ntt]?.remove(it)
                        this[ntt]?.addAll(this[it]!!)
                        changing()
                    }
                }
            }
        }.toMap()

    private fun generateFollow() =
        mutableMapOf(productions[0].head to mutableSetOf(end as Token)).apply {
            productions.forEach { (start, ends) ->
                var tmp = mutableSetOf(start as Token)
                ends.reversed().forEach { token ->
                    if (token is TerminalToken) {
                        tmp = mutableSetOf(token)
                    } else if (token is NonTerminalToken) {
                        getOrPut(token, ::mutableSetOf).addAll(tmp)
                        if (token !in nullable) {
                            tmp = first[token]?.toMutableSet() ?: error("token$token is not in first $first")
                        } else {
                            tmp.addAll(first[token] ?: error("token$token is not in first $first"))
                            tmp.remove(empty)
                        }
                    }
                }
            }
            whileChanged { changing ->
                forEach { (ntt, tokens) ->
                    tokens.filterIsInstance<NonTerminalToken>().forEach {
                        this[ntt]?.remove(it)
                        this[ntt]?.addAll(this[it]!!)
                        changing()
                    }
                }
            }
        }.toMap()

    private fun generateSelect() =
        mutableMapOf<Production, MutableSet<Token>>().apply {
            productions.forEach { production ->
                val (start, ends) = production
                val thisSelect = getOrPut(production, ::mutableSetOf)
                val token = ends[0]

                if (token is NonTerminalToken) {
                    thisSelect.addAll(first[token] ?: error("token$token is not in first$first"))
                } else if (token is TerminalToken) {
                    thisSelect.add(token)
                }

                if (ends.all { it in nullable || it == empty }) {
                    thisSelect.addAll(follow[start] ?: error("token$start is not in follow$follow"))
                    thisSelect.remove(empty)
                }
            }
        }.toMap()

    private fun generateAnalysisTable() =
        mutableMapOf<NonTerminalToken, MutableMap<TerminalToken, Production>>().apply {
            select.forEach { (production, tokens) ->
                tokens.forEach { token ->
                    getOrPut(production.head, ::mutableMapOf)[token as TerminalToken] = production
                }
            }
        }.toMap()

    override fun parse(tokens: List<Token>): Boolean {
        println(analysisTable)
        val stack = Stack<Token>().apply { push(E) }
        var index = 0
        while (stack.isNotEmpty()) {
            println(stack)
            val input = tokens.getOrNull(index) ?: end

            when (val top = stack.last()) {
                is TerminalToken -> {
                    if (top == input) {
                        stack.pop()
                        index++
                        println(input)
                    } else {
                        println("Wrong input $input")
                        return false
                    }
                }
                is NonTerminalToken -> {
                    val production = analysisTable[top]?.get(input)
                    if (production != null) {
                        stack.pop()
                        stack.addAll(production.body.filter { it !is EmptyToken }.reversed())
                        println(production)
                    } else {
                        println()
                        return false
                    }
                }
            }
        }
        println(stack)
        return true
    }
}
