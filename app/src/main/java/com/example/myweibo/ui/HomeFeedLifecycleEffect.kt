package com.example.myweibo.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

@Composable
internal fun HomeFeedLifecycleEffect(
    lifecycleOwner: LifecycleOwner,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onDispose: () -> Unit,
) {
    val currentOnStart = rememberUpdatedState(onStart)
    val currentOnStop = rememberUpdatedState(onStop)
    val currentOnDispose = rememberUpdatedState(onDispose)
    // Re-registering when feed state changes replays ON_START while already in
    // the foreground. That looks like a resume and recreates the feed subtree.
    DisposableEffect(lifecycleOwner) {
        val lifecycle = lifecycleOwner.lifecycle
        var attaching = true
        val observer = LifecycleEventObserver { _, event ->
            // LifecycleRegistry catches up a newly added observer synchronously.
            if (attaching) return@LifecycleEventObserver
            when (event) {
                Lifecycle.Event.ON_START -> currentOnStart.value()
                Lifecycle.Event.ON_STOP -> currentOnStop.value()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        attaching = false
        onDispose {
            lifecycle.removeObserver(observer)
            currentOnDispose.value()
        }
    }
}
