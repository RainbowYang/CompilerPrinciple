package moe.rainbowyang.parser

import java.util.*

fun main() {
    val tokens = Lexer.process("i:=i-i")
    println(ParserSLR1().parse(tokens))
}

class ParserSLR1(productions: List<Production> = productionsExample) : Parser(productions) {
    companion object {
        private val productionsExample = listOf(
            S1.`→`(S),
            S.`→`(id, assign, E),
            E.`→`(E, add, T),
            E.`→`(E, sub, T),
            E.`→`(T),
            T.`→`(T, times, F),
            T.`→`(T, div, F),
            T.`→`(F),
            F.`→`(left, E, right),
            F.`→`(id)
        )
        val closureCache = linkedMapOf<Items, Items>()
    }


    val follow = ParserLL1(productions).follow
    val NonTerminalToken.follow get() = this@ParserSLR1.follow[this] ?: error("")

    val action get() = actionAndGoto.first
    val goto get() = actionAndGoto.second

    val actionAndGoto =
        generateActionAndGoto()

    fun generateActionAndGoto():
            Pair<MutableMap<Items, MutableMap<TerminalToken, ActionFunction>>,
                    MutableMap<Items, MutableMap<NonTerminalToken, Items>>> {
        val startItems = Items(productions[0].next()).closure

        val allItems = mutableSetOf(startItems)
        val processedItems = mutableSetOf<Items>()

        val action =
            mutableMapOf<Items, MutableMap<TerminalToken, ActionFunction>>()
        val goto =
            mutableMapOf<Items, MutableMap<NonTerminalToken, Items>>()

        whileChanged { changing ->
            allItems.filter { it !in processedItems }.forEach { items ->
                changing()
                processedItems.add(items)

                val thisAction = action.getOrPut(items, ::mutableMapOf)
                val thisGoto = goto.getOrPut(items, ::mutableMapOf)

                items.items.run {
                    filter { it.isFinished() }.forEach {
                        it.head.follow.forEach { passToken ->
                            thisAction[passToken as TerminalToken] = if (it.isAcceptable()) Accept() else Reduce(it)
                        }
                    }
                    filter { it.isNotFinished() }.map { it.atToken }.toSet().forEach { passToken ->
                        val passItems = items.go(passToken)
                        allItems.add(passItems)
                        when (passToken) {
                            is NonTerminalToken -> thisGoto[passToken] = passItems
                            is TerminalToken -> thisAction[passToken] = Shift(passToken, passItems)
                        }
                    }
                }
            }
        }

        println("All Items:")
        println(allItems.joinToString("\n"))
        println("------")

        println("Action:")
        println(action.toList().joinToString("\n"))
        println("------")

        println("Goto:")
        println(goto.toList().joinToString("\n"))
        println("------")

        return action to goto
    }

    override fun parse(tokens: List<Token>): Boolean {
        val tokenStack = Stack<Token>()
        val statusStack = Stack<Items>()

        var step = 0
        statusStack.push(closureCache.values.first())
        for (token in listOf(*tokens.toTypedArray(), end)) {
            do {
                println("---${step++}---")
                println("statusStack: \n\t${statusStack.joinToString("\n\t")}")
                println("tokenStack: \n\t${tokenStack.joinToString("\n\t")}")

                println("Token: $token")
                val actionFunction = action[statusStack.peek()]?.get(token)!!
                println("Do:    $actionFunction")
                actionFunction(statusStack, tokenStack)

                if (actionFunction is Accept) {
                    return true
                }
            } while (actionFunction is Reduce)
        }
        return false
    }

    interface ActionFunction {
        operator fun invoke(statusStack: Stack<Items>, tokenStack: Stack<Token>)
    }

    inner class Accept : ActionFunction {
        override fun invoke(statusStack: Stack<Items>, tokenStack: Stack<Token>) {}

        override fun toString(): String {
            return "Accept()"
        }

    }

    inner class Shift(val passToken: TerminalToken, val passItems: Items) : ActionFunction {
        override fun invoke(statusStack: Stack<Items>, tokenStack: Stack<Token>) {
            tokenStack.push(passToken)
            statusStack.push(passItems)
        }

        override fun toString(): String {
            return "Shift(passToken=$passToken, passItems=$passItems)"
        }
    }

    inner class Reduce(val production: Production) : ActionFunction {
        override fun invoke(statusStack: Stack<Items>, tokenStack: Stack<Token>) {
            val body = mutableListOf<Token>()
            repeat(production.body.size) {
                body.add(tokenStack.pop())
                statusStack.pop()
            }
            tokenStack.push(production.head)
            statusStack.push((goto[statusStack.peek()] ?: error(""))[production.head])
        }

        override fun toString(): String {
            return "Reduce(production=$production)"
        }
    }

    inner class Items(val items: Set<Production>) {
        constructor(vararg items: Production) : this(items.toSet())
        constructor(init: MutableSet<Production>.() -> Unit) : this(mutableSetOf<Production>().apply { init(this) })

        val closure by lazy {
            closureCache.getOrPut(this) {
                Items {
                    // 初始化：所给的所有项目都属于闭包
                    addAll(items)
                    // 扩充
                    val added = mutableSetOf<NonTerminalToken>()
                    whileChanged { changing ->
                        filter { it.isNotFinished() }.forEach { item ->
                            val token = item.atToken
                            if (token is NonTerminalToken && token !in added) {
                                productions.filter { it.head == token }.forEach { add(it.next()) }
                                added.add(token)
                                changing()
                            }
                        }
                    }
                }
            }
        }

        fun go(passToken: Token) =
            Items {
                addAll(items.filter { it.isNotFinished() && it.atToken == passToken }.map { it.next() })
            }.closure


        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Items) return false

            if (items != other.items) return false

            return true
        }

        override fun hashCode(): Int {
            return items.hashCode()
        }

        override fun toString(): String {
            return "Items${closureCache.values.toList().indexOf(this)}($items)"
        }
    }
}
