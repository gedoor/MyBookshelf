package com.kunfei.bookshelf.widget.page

import java.util.*
import kotlin.math.max
import kotlin.math.min

/**
 * 章节
 */

class TxtChapter internal constructor(val position: Int) {
    val txtPageList = ArrayList<TxtPage>()
    val txtPageLengthList = ArrayList<Int>()
    val paragraphLengthList = ArrayList<Int>()
    var status = Status.LOADING
    var msg: String? = null

    val pageSize: Int
        get() = txtPageList.size

    fun addPage(txtPage: TxtPage) {
        txtPageList.add(txtPage)
    }

    fun getPage(page: Int): TxtPage? {
        return if (txtPageList.isNotEmpty()) {
            txtPageList[max(0, min(page, txtPageList.size - 1))]
        } else null
    }

    fun getPageLength(position: Int): Int {
        return if (position >= 0 && position < txtPageLengthList.size) {
            txtPageLengthList[position]
        } else -1
    }

    fun addTxtPageLength(length: Int) {
        txtPageLengthList.add(length)
    }

    fun addParagraphLength(length: Int) {
        paragraphLengthList.add(length)
    }

    fun getParagraphIndex(length: Int): Int {
        for (i in paragraphLengthList.indices) {
            if ((i == 0 || paragraphLengthList[i - 1] < length) && length <= paragraphLengthList[i]) {
                return i
            }
        }
        return -1
    }

    enum class Status {
        LOADING, FINISH, ERROR, EMPTY, CATEGORY_EMPTY, CHANGE_SOURCE
    }
}
