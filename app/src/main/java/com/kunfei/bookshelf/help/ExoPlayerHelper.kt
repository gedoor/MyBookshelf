package com.kunfei.bookshelf.help

import android.net.Uri
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.util.Util.inferContentType
import com.kunfei.bookshelf.base.BaseModelImpl


object ExoPlayerHelper {

    fun createMediaSource(uri: Uri, overrideExtension: String?): MediaSource {
        val mediaItem = MediaItem.fromUri(uri)
        val dataSourceFactory = OkHttpDataSource.Factory(BaseModelImpl.getClient())
        val mediaSourceFactory = when (inferContentType(uri, overrideExtension)) {
            C.TYPE_SS -> SsMediaSource.Factory(dataSourceFactory)
            C.TYPE_DASH -> DashMediaSource.Factory(dataSourceFactory)
            C.TYPE_HLS -> HlsMediaSource.Factory(dataSourceFactory)
            else -> ProgressiveMediaSource.Factory(dataSourceFactory)
        }
        return mediaSourceFactory.createMediaSource(mediaItem)

    }

}