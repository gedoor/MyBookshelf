package com.kunfei.bookshelf.view.dialog

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.setPadding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.textfield.TextInputLayout
import com.kunfei.bookshelf.R
import com.kunfei.bookshelf.databinding.DialogLoginBinding
import com.kunfei.bookshelf.model.BookSourceManager
import com.kunfei.bookshelf.utils.*
import com.kunfei.bookshelf.utils.theme.ThemeStore
import com.kunfei.bookshelf.utils.viewbindingdelegate.viewBinding
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable
import org.jetbrains.anko.sdk27.listeners.onClick

class SourceLoginDialog : DialogFragment() {

    companion object {
        fun start(fragmentManager: FragmentManager, sourceUrl: String) {
            SourceLoginDialog().apply {
                arguments = bundleOf(
                    Pair("sourceUrl", sourceUrl)
                )
            }.show(fragmentManager, "sourceLoginDialog")
        }
    }

    val binding by viewBinding(DialogLoginBinding::bind)

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

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
        binding.toolBar.title = getString(R.string.login)
        val sourceUrl = arguments?.getString("sourceUrl")
        val source = BookSourceManager.getBookSourceByUrl(sourceUrl)
        source ?: let {
            dismiss()
            return
        }
        binding.toolBar.title = getString(R.string.login_source, source.bookSourceName)
        val loginInfo = source.loginInfoMap
        val loginUi = GSON.fromJsonArray<RowUi>(source.loginUi)
        loginUi?.forEachIndexed { index, rowUi ->
            when (rowUi.type) {
                "text" -> layoutInflater.inflate(R.layout.item_source_edit, binding.root, false)
                    .let {
                        binding.listView.addView(it)
                        it.id = index
                        (it as TextInputLayout).hint = rowUi.name
                        it.findViewById<EditText>(R.id.editText).apply {
                            setText(loginInfo?.get(rowUi.name))
                        }
                    }
                "password" -> layoutInflater.inflate(R.layout.item_source_edit, binding.root, false)
                    .let {
                        binding.listView.addView(it)
                        it.id = index
                        (it as TextInputLayout).hint = rowUi.name
                        it.findViewById<EditText>(R.id.editText).apply {
                            inputType =
                                InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT
                            setText(loginInfo?.get(rowUi.name))
                        }
                    }
                "button" -> layoutInflater.inflate(
                    R.layout.item_find2_childer_view,
                    binding.root,
                    false
                )
                    .let {
                        binding.listView.addView(it)
                        it.id = index
                        (it as TextView).let { textView ->
                            textView.text = rowUi.name
                            textView.setPadding(DensityUtil.dp2px(requireContext(), 16f))
                        }
                        it.onClick {
                            if (rowUi.action.isAbsUrl()) {
                                context?.openUrl(rowUi.action!!)
                            }
                        }
                    }
            }
        }
        binding.toolBar.inflateMenu(R.menu.menu_source_login)
        binding.toolBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_check -> {
                    val loginData = hashMapOf<String, String?>()
                    loginUi?.forEachIndexed { index, rowUi ->
                        when (rowUi.type) {
                            "text", "password" -> {
                                val value = binding.listView.findViewById<TextInputLayout>(index)
                                    .findViewById<EditText>(R.id.editText).text?.toString()
                                loginData[rowUi.name] = value
                            }

                        }
                    }
                    source.putLoginInfo(loginData)
                    Single.create<String> { emitter ->
                        source.loginUrl?.let { loginUrl ->
                            emitter.onSuccess(source.evalJS(loginUrl).toString())
                        } ?: let {
                            emitter.onError(Throwable(""))
                        }
                    }.compose(RxUtils::toSimpleSingle)
                        .subscribe(object : SingleObserver<String> {

                            override fun onSubscribe(d: Disposable) {

                            }

                            override fun onSuccess(t: String) {
                                dismiss()
                            }

                            override fun onError(e: Throwable) {

                            }
                        })
                }
            }
            return@setOnMenuItemClickListener true
        }
    }

    data class RowUi(
        var name: String,
        var type: String,
        var action: String?
    )

}