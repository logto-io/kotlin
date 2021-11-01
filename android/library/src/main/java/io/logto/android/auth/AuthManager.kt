package io.logto.android.auth

import android.content.Context
import android.net.Uri

object AuthManager {
    internal var currentFlow: IFlow? = null

    fun start(context: Context, flow: IFlow) {
        currentFlow = flow
        flow.start(context)
    }

    fun onResult(data: Uri) {
        currentFlow?.onResult(data)
    }

    fun reset() {
        currentFlow = null
    }
}
