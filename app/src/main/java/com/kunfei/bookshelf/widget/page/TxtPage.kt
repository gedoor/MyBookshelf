package com.kunfei.bookshelf.widget.page

import java.util.*

/**
 * 页面
 */

class TxtPage(val position: Int) {
    var title: String? = null
    var titleLines: Int = 0 //当前 lines 中为 title 的行数。
    private val lines = ArrayList<String>()
    //存放每个字的位置
    var txtLists: List<TxtLine>? = null

    val content: String
        get() {
            val s = StringBuilder()
            for (i in lines.indices) {
                s.append(lines[i])
            }
            return s.toString()
        }

    fun addLine(line: String) {
        lines.add(line)
    }

    fun addLines(lines: List<String>) {
        this.lines.addAll(lines)
    }

    fun getLine(i: Int): String {
        return lines[i]
    }

    fun getLines(): List<String> {
        return lines
    }

    fun size(): Int {
        return lines.size
    }
}
