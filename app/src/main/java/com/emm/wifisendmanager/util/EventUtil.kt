package com.emm.wifisendmanager.util

import com.emm.wifisendmanager.bean.TextBean
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * @author dengjie
 * date 2023.01.29
 * description
 */
object EventUtil {
    val onReceiveDataFlow = MutableSharedFlow<TextBean>()
}