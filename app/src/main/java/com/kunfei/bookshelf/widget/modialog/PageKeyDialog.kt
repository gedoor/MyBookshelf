package com.kunfei.bookshelf.widget.modialog

import android.content.Context
import android.view.KeyEvent
import android.view.LayoutInflater
import com.kunfei.bookshelf.MApplication
import com.kunfei.bookshelf.databinding.DialogPageKeyBinding
import com.kunfei.bookshelf.utils.SoftInputUtil
import org.jetbrains.anko.sdk27.listeners.onClick


class PageKeyDialog(context: Context) : BaseDialog(context) {

    val binding = DialogPageKeyBinding.inflate(LayoutInflater.from(context))

    init {
        setContentView(binding.root)
        binding.etPrev.setText(MApplication.getConfigPreferences().getInt("prevKeyCode", 0).toString())
        binding.etNext.setText(MApplication.getConfigPreferences().getInt("nextKeyCode", 0).toString())
        binding.tvOk.onClick {
            val edit = MApplication.getConfigPreferences().edit()
            binding.etPrev.text?.let {
                edit.putInt("prevKeyCode", it.toString().toInt())
            }
            binding.etNext.text?.let {
                edit.putInt("nextKeyCode", it.toString().toInt())
            }
            edit.apply()
            dismiss()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode != KeyEvent.KEYCODE_BACK) {
            if (binding.etPrev.hasFocus()) {
                binding.etPrev.setText(keyCode.toString())
            } else if (binding.etNext.hasFocus()) {
                binding.etNext.setText(keyCode.toString())
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun dismiss() {
        super.dismiss()
        SoftInputUtil.hideIMM(currentFocus)
    }

}