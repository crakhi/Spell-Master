package com.spell.master.data.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.spell.master.R
import com.spell.master.data.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Wraps Firebase Auth + Credential Manager's Google ID flow. Firebase itself persists
 * the signed-in session across app restarts, so callers only need [currentUserId] /
 * [authStateFlow] to know whether the sign-in gate can be skipped.
 */
class AuthRepository(private val appContext: Context) {

    private val firebaseAuth = FirebaseAuth.getInstance()

    val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    /** Emits immediately with the current state, then again on every sign-in/sign-out. */
    fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth -> trySend(auth.currentUser) }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    /**
     * Launches the Credential Manager Google sign-in bottom sheet and completes the
     * Firebase sign-in. [activityContext] should be the current Activity (or a Compose
     * LocalContext) so the picker UI can attach correctly.
     */
    suspend fun signInWithGoogle(activityContext: Context): Result<FirebaseUser> = try {
        val webClientId = appContext.getString(R.string.default_web_client_id)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(true)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credentialManager = CredentialManager.create(activityContext)
        val response = credentialManager.getCredential(context = activityContext, request = request)
        val credential = response.credential

        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
            val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()
            val user = authResult.user
            if (user != null) Result.success(user) else Result.failure(IllegalStateException("Sign-in succeeded but no user was returned"))
        } else {
            Result.failure(IllegalStateException("Unexpected credential type from Credential Manager"))
        }
    } catch (t: Throwable) {
        Result.failure(t)
    }

    fun signOut() {
        firebaseAuth.signOut()
    }
}
