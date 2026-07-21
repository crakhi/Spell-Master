package com.spell.master.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.spell.master.data.await
import com.spell.master.data.local.dao.LevelProgressDao
import com.spell.master.data.local.entity.LevelProgressEntity

/**
 * Mirrors per-user level progress to Firestore under users/{userId}/progress/{levelId}
 * so it syncs across devices for the same account. Room stays the offline source of
 * truth for gameplay -- this is best-effort on top of it:
 *  - [pushProgress] fires after every local progress update. Firestore's Android SDK
 *    has offline persistence enabled by default, so a push made while offline is queued
 *    locally and flushes automatically once connectivity returns; we don't need our own
 *    retry/queue logic.
 *  - [pullAndMerge] runs once at sign-in (or app start if already signed in) and merges
 *    remote progress into Room, taking the best of each side so replaying progress on
 *    one device never regresses what another device already achieved.
 */
class FirestoreSyncRepository(private val levelProgressDao: LevelProgressDao) {

    private val firestore = FirebaseFirestore.getInstance()

    private fun progressCollection(userId: String) =
        firestore.collection("users").document(userId).collection("progress")

    fun pushProgress(userId: String, progress: LevelProgressEntity) {
        val data = mapOf(
            "gradeId" to progress.gradeId,
            "isUnlocked" to progress.isUnlocked,
            "stars" to progress.stars,
            "bestCorrectCount" to progress.bestCorrectCount,
            "updatedAt" to progress.updatedAt
        )
        // Intentionally not awaited: Firestore queues this locally and syncs when
        // online, so blocking gameplay on network state would defeat the offline-first
        // point of the feature.
        progressCollection(userId).document(progress.levelId).set(data)
    }

    suspend fun pullAndMerge(userId: String) {
        try {
            val snapshot = progressCollection(userId).get().await()
            for (doc in snapshot.documents) {
                val levelId = doc.id
                val gradeId = doc.getLong("gradeId")?.toInt() ?: continue
                val remoteUnlocked = doc.getBoolean("isUnlocked") ?: false
                val remoteStars = doc.getLong("stars")?.toInt() ?: -1
                val remoteCorrect = doc.getLong("bestCorrectCount")?.toInt() ?: 0
                val remoteUpdatedAt = doc.getLong("updatedAt") ?: 0L

                val local = levelProgressDao.getProgress(userId, levelId)
                val merged = LevelProgressEntity(
                    userId = userId,
                    levelId = levelId,
                    gradeId = gradeId,
                    isUnlocked = (local?.isUnlocked ?: false) || remoteUnlocked,
                    stars = maxOf(local?.stars ?: -1, remoteStars),
                    bestCorrectCount = maxOf(local?.bestCorrectCount ?: 0, remoteCorrect),
                    updatedAt = maxOf(local?.updatedAt ?: 0L, remoteUpdatedAt)
                )
                levelProgressDao.upsert(merged)
            }
        } catch (_: Exception) {
            // Offline, or first-ever login with nothing remote yet -- Room already
            // reflects whatever progress exists locally; the next successful sync
            // will reconcile, so failing silently here is the right call.
        }
    }
}
