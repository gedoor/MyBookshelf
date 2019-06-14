package com.kunfei.bookshelf.help.permission

interface OnPermissionsDeniedCallback {
    fun onPermissionsDenied(requestCode: Int, deniedPermissions: Array<String>)
}
