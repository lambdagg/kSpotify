package xyz.lambdagg.kspotify

import com.serjltt.moshi.adapters.FallbackEnum
import com.squareup.moshi.Moshi

internal val MOSHI = Moshi.Builder()
    .add(FallbackEnum.ADAPTER_FACTORY) // TODO do this ourselves
    .build()
