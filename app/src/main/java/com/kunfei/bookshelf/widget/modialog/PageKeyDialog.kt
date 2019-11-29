package com.kunfei.bookshelf.widget.modialog

import android.content.Context
import android.view.KeyEvent
import com.kunfei.bookshelf.MApplication
import com.kunfei.bookshelf.R
import com.kunfei.bookshelf.utils.SoftInputUtil
import kotlinx.android.synthetic.main.dialog_page_key.*
import org.jetbrains.anko.sdk27.listeners.onClick


class PageKeyDialog(context: Context) : BaseDialog(context) {

    init {
        setContentView(R.layout.dialog_page_key)
        et_prev.setText(MApplication.getConfigPreferences().getInt("prevKeyCode", 0).toString())
        et_next.setText(MApplication.getConfigPreferences().getInt("nextKeyCode", 0).toString())
        tv_ok.onClick {
            val edit = MApplication.getConfigPreferences().edit()
            et_prev.text?.let {
                edit.putInt("prevKeyCode", it.toString().toInt())
            }
            et_next.text?.let {
                edit.putInt("nextKeyCode", it.toString().toInt())
            }
            edit.apply()
            dismiss()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode != KeyEvent.KEYCODE_BACK) {
            if (et_prev.hasFocus()) {
                et_prev.setText(keyCode.toString())
            } else if (et_next.hasFocus()) {
                et_next.setText(keyCode.toString())
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