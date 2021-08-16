package com.kunfei.bookshelf.view.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.kunfei.bookshelf.R
import com.kunfei.bookshelf.bean.LoginRule
import com.kunfei.bookshelf.databinding.DialogLoginBinding
import com.kunfei.bookshelf.utils.viewbindingdelegate.viewBinding

class SourceLoginDialog : DialogFragment() {

    val binding by viewBinding(DialogLoginBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_login, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getParcelable<LoginRule>("loginRule")?.let { loginRule ->
            loginRule.ui?.forEachIndexed { index, ui ->
                when (ui.type) {
                    "text" -> layoutInflater.inflate(R.layout.item_source_edit, binding.root)
                    "password" -> layoutInflater.inflate(R.layout.item_source_edit, binding.root)
                    "button" -> layoutInflater.inflate(R.layout.item_source_edit, binding.root)
                    else -> null
                }?.let {
                    it.id = index
                    binding.root.addView(it)
                }
            }
        }
    }


}