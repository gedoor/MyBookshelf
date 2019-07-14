package com.kunfei.bookshelf.widget.page

class TxtLine {

    var charsData: List<TxtChar>? = null

    fun getLineData(): String {
        var linedata = ""
        if (charsData == null) return linedata
        charsData?.let {
            if (it.isEmpty()) return linedata
            for (c in it) {
                linedata += c.chardata
            }
        }
        return linedata
    }

    override fun toString(): String {
        return "ShowLine [Linedata=" + getLineData() + "]"
    }

}