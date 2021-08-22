package com.kunfei.bookshelf.help.glide

import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.kunfei.bookshelf.base.BaseModelImpl
import java.io.InputStream


object OkHttpModeLoaderFactory : ModelLoaderFactory<GlideUrl?, InputStream?> {

    override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<GlideUrl?, InputStream?> {
        return OkHttpModelLoader(BaseModelImpl.getClient())
    }

    override fun teardown() {
        // Do nothing, this instance doesn't own the client.
    }

}