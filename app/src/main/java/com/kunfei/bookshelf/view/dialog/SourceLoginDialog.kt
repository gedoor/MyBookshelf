package com.kunfei.bookshelf.view.dialog

import android.os.Bundle
import android.text.InputType
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputLayout
import com.kunfei.bookshelf.R
import com.kunfei.bookshelf.bean.CookieBean
import com.kunfei.bookshelf.bean.LoginRule
import com.kunfei.bookshelf.constant.AppConstant
import com.kunfei.bookshelf.databinding.DialogLoginBinding
import com.kunfei.bookshelf.utils.EncoderUtils
import com.kunfei.bookshelf.utils.GSON
import com.kunfei.bookshelf.utils.theme.ThemeStore
import com.kunfei.bookshelf.utils.viewbindingdelegate.viewBinding
import com.kunfei.bookshelf.widget.views.ATEEditText

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
        binding.toolBar.setBackgroundColor(ThemeStore.primaryColor(requireContext()))
        val sourceUrl = arguments?.getString("sourceUrl")
        val loginRule = arguments?.getParcelable<LoginRule>("loginRule")
        loginRule?.ui?.forEachIndexed { index, rowUi ->
            when (rowUi.type) {
                "text" -> layoutInflater.inflate(R.layout.item_source_edit, binding.root)
                    .apply {
                        id = index
                    }
                "password" -> layoutInflater.inflate(R.layout.item_source_edit, binding.root)
                    .apply {
                        id = index
                        findViewById<ATEEditText>(R.id.editText)?.inputType =
                            InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT
                    }
            }
        }
        binding.toolBar.inflateMenu(R.menu.menu_source_login)
        binding.toolBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_check -> {
                    val loginData = hashMapOf<String, String?>()
                    loginRule?.ui?.forEachIndexed { index, rowUi ->
                        when (rowUi.type) {
                            "text", "password" -> {
                                val value = binding.root.findViewById<TextInputLayout>(index)
                                    .findViewById<EditText>(R.id.editText).text?.toString()
                                loginData[rowUi.name] = value
                            }
                        }
                    }
                    val data = Base64.encodeToString(
                        EncoderUtils.decryptAES(
                            GSON.toJson(loginData).toByteArray(),
                            AppConstant.androidId(requireContext()).toByteArray()
                        ),
                        Base64.DEFAULT
                    )
                    CookieBean("login_$sourceUrl", data)
                    dismiss()
                }
            }
            return@setOnMenuItemClickListener true
        }
    }


}