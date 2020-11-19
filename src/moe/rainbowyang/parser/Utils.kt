package moe.rainbowyang.parser

/**
 * @author: Rainbow Yang
 * @create: 2020-05-22 15:36
 **/

inline fun whileChanged(func: (changing: () -> Unit) -> Unit) {
    var changed = true
    while (changed) {
        changed = false
        func { changed = true }
    }
}