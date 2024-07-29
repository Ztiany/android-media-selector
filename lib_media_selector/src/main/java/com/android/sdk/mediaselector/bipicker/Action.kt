package com.android.sdk.mediaselector.bipicker

import com.android.sdk.mediaselector.processor.Processor
import com.android.sdk.mediaselector.utils.ActFragWrapper

sealed interface Action {

    fun assembleProcessors(host: ActFragWrapper): List<Processor>

}

/*
class CaptureVideo : Action {

}

class PickPhoto : Action {

}

class PickVideo : Action {

}

class PickFile : Action {

}*/
