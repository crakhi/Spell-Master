package com.spell.master.data

import com.google.android.gms.tasks.Task
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

/** Bridges a Play Services [Task] into a suspend call without pulling in kotlinx-coroutines-play-services. */
internal suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
    addOnCompleteListener { task ->
        val exception = task.exception
        when {
            task.isCanceled -> cont.resumeWithException(java.util.concurrent.CancellationException("Task was cancelled"))
            exception != null -> cont.resumeWithException(exception)
            else -> cont.resume(task.result)
        }
    }
}
