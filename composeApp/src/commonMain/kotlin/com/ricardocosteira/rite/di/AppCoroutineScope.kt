package com.ricardocosteira.rite.di

import kotlinx.coroutines.CoroutineScope

/**
 * A process-lifetime [CoroutineScope] for fire-and-forget work whose lifetime should
 * exceed any single ViewModel or screen — e.g., the [com.ricardocosteira.rite.domain.time.CurrentDateProvider]
 * background tickers.
 */
typealias AppCoroutineScope = CoroutineScope
