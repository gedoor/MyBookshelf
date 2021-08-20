package com.kunfei.bookshelf.help

import com.kunfei.bookshelf.MApplication
import com.kunfei.bookshelf.bean.BookSourceBean
import com.kunfei.bookshelf.utils.GSON
import com.kunfei.bookshelf.utils.fromJsonObject
import java.io.File

object DefaultValueHelper {

    val xxlSource: BookSourceBean by lazy {
        val json = String(
            MApplication.getInstance().assets.open("data${File.separator}BookSourceXxl.json")
                .readBytes()
        )
        GSON.fromJsonObject(json)!!
    }

}