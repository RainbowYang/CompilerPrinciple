package moe.rainbowyang.parser

/**
 * @author: Rainbow Yang
 * @create: 2020-05-21 16:33
 **/
data class Production(
    val head: NonTerminalToken,
    val body: List<Token>,
    val at: Int = -1
) {
    constructor(start: NonTerminalToken, vararg ends: Token) : this(start, ends.toList())

    fun next() = Production(head, body, at + 1)
    val atToken get() = body[at]
    fun isAcceptable() = head == S1 && body[0] == S && at == 1
    fun isFinished() = at >= body.size
    fun isNotFinished() = 0 <= at && at < body.size


    override fun toString() =
        when (at) {
            -1 -> " ${head.value} -> ${body.joinToString(" ") { it.value }} "
            else -> {
                val front = body.map { it.value }.subList(0, at).joinToString(" ")
                val end = body.map { it.value }.subList(at, body.size).joinToString(" ")
                " ${head.value} -> $front·$end "
            }
        }

}

fun NonTerminalToken.`→`(
    vararg ends: Token
) =
    Production(this, ends.toList())

