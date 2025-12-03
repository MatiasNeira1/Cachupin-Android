package com.example.cachupin.backend.data.repository

import com.example.cachupin.domain.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun getProfileByUid(
        uid: String,
        onSuccess: (UserProfile) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                val email = doc.getString("email")
                    ?: auth.currentUser?.email
                    ?: ""

                val profile = UserProfile(
                    uid = uid,
                    nombre = doc.getString("nombre") ?: "",
                    email = email,
                    createdAt = doc.getLong("createdAt")
                )
                onSuccess(profile)
            }
            .addOnFailureListener { e -> onError(e) }
    }
}
