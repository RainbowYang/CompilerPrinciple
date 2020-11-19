package moe.rainbowyang.parser

fun main() {
    Lexer.process(
        """
        while(a==b)begin
        a:=a+1;#zhushi
        b:=b-1;
        c=c*d;
        d=c/d;
        if(a>b) then  c=C else C=c;
        end
    """.trimIndent()
    )

    Lexer.process(
        """
        for(int i=1;i<=10;i++) begin
        a_b++;#zszszszszs
        b_C--;#zszszszszs
        B123:=1234567;
        a=@;
        123a=0;
        a.b;
        end
    """.trimIndent()
    )
}

object Lexer {
    val keywords =
        listOf("begin", "end", "if", "then", "else", "for", "while", "do", "and", "or", "not")

    val symbols =
        listOf(":=", ">=", "<=", "<>", "==", "++", "--", "(", ")", "+", "-", "*", "/", ">", "<", "=", ";")

    val delimiterRegex = Regex("[();]")
    val idRegex = Regex("[_a-zA-Z][_a-zA-Z0-9]*")
    val numRegex = Regex("[0-9]+")

    fun process(codes: String): List<Token> {
        val tokens = mutableListOf<Token>()

        // 按照 \n 切分
        val codeLines = codes.split("\\s*\n\\s*".toRegex())

        codeLines.forEach { codeLine ->
            tokens.addAll(
                processLine(
                    codeLine.split("#")[0] // 忽略 # 和 # 之后的内容，一直到 \n
                )
            )
        }

        println("Tokens:")
        tokens.forEach(::println)
        println("------")
        return tokens
    }

    private fun processLine(codeLine: String): List<Token> {
        if (codeLine.isBlank()) {
            return listOf()
        }

        val tokens = mutableListOf<Token>()

        // 按照空白格分割
        val codeWords = codeLine.split(Regex("\\s+"))

        codeWords.forEach { codeWord ->
            println("--->$codeWord")

            // find id
            val words = idRegex.findAll(codeWord).map { it.value }.toList()

            // expect id
            idRegex.split(codeWord).forEachIndexed { index, other ->

                // find num
                val nums = numRegex.findAll(other).map { it.value }.toList()

                // expect id and num
                numRegex.split(other).forEachIndexed { index, other ->

                    val delimiters = delimiterRegex.findAll(other).map { it.value }.toList()

                    delimiterRegex.split(other).forEachIndexed { index, other ->

                        if (other in symbols) {
                            tokens.add(SymbolToken(other))
                        } else if (other.isNotBlank()) {
                            println("[Warning] $other is illegal")
                        }

                        if (index != delimiters.size)
                            tokens.add(DelimiterToken(delimiters[index]))
                    }
                    if (index != nums.size)
                        tokens.add(NumToken(nums[index]))
                }
                if (index != words.size)
                    if (words[index] in keywords) {
                        tokens.add(KeywordToken(words[index]))
                    } else {
                        tokens.add(IDToken(words[index]))
                    }
            }
        }
        return tokens
    }
}