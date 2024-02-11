package com.rex50.android.localtransfer.local.core

import java.util.concurrent.atomic.AtomicReference

interface SimpleStateable<State> {
    val state: AtomicReference<State>

    fun newState(s: State) {
        val oldState = state.get()
        if (s != oldState) {
            onNewState(s)
            state.set(s)
        }
    }

    fun getCurrentState(): State = state.get()

    fun onNewState(s: State) {

    }
}