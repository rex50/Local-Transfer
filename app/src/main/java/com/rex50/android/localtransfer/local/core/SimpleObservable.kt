package com.rex50.android.localtransfer.local.core

import java.util.concurrent.LinkedBlockingDeque

interface SimpleObservable<Observer> {

    val observers: LinkedBlockingDeque<Observer>

    fun addObserver(o: Observer) {
        observers.add(o)
    }

    fun removeObserver(o: Observer) {
        observers.remove(o)
    }

    fun clearObserves() {
        observers.clear()
    }
}