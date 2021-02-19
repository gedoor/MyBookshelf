package com.kunfei.bookshelf.view.popupwindow

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kunfei.bookshelf.base.adapter.ItemViewHolder
import com.kunfei.bookshelf.base.adapter.RecyclerAdapter
import com.kunfei.bookshelf.databinding.ItemTextBinding
import com.kunfei.bookshelf.databinding.PopupKeyboardToolBinding
import org.jetbrains.anko.sdk27.listeners.onClick


class KeyboardToolPop(
        context: Context,
        private val chars: List<String>,
        val callBack: CallBack?
) : PopupWindow(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT) {

    private val binding = PopupKeyboardToolBinding.inflate(LayoutInflater.from(context))

    init {
        isTouchable = true
        isOutsideTouchable = false
        isFocusable = false
        inputMethodMode = INPUT_METHOD_NEEDED //解决遮盖输入法
        contentView = binding.root
        initRecyclerView()
    }

    private fun initRecyclerView() = with(contentView) {
        val adapter = Adapter(context)
        binding.recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        binding.recyclerView.adapter = adapter
        adapter.setItems(chars)
    }

    inner class Adapter(context: Context) :
            RecyclerAdapter<String, ItemTextBinding>(context) {

        override fun getViewBinding(parent: ViewGroup): ItemTextBinding {
            return ItemTextBinding.inflate(inflater, parent, false)
        }

        override fun convert(holder: ItemViewHolder, binding: ItemTextBinding, item: String, payloads: MutableList<Any>) {
            with(binding) {
                textView.text = item
                root.onClick { callBack?.sendText(item) }
            }
        }

        override fun registerListener(holder: ItemViewHolder, binding: ItemTextBinding) {

        }
    }

    interface CallBack {
        fun sendText(text: String)
    }

}
