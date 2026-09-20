package com.skillbuilder.app.data.local

import com.skillbuilder.app.domain.model.User
import kotlinx.serialization.Serializable

@Serializable
data class UserAccount(
    val id: String,
    val email: String,
    val password: String = "",
    val name: String,
    val phone: String = "",
    val dob: String = "",
    val location: String = "",
    val skills: List<String> = emptyList(),
    val isMentor: Boolean = false,
    val avatarUrl: String? = null,
    val isGoogleUser: Boolean = false
) {
    fun toUser(): User {
        return User(
            id = id,
            name = name,
            email = email,
            avatarUrl = avatarUrl,
            phone = phone,
            dob = dob,
            location = location,
            bio = if (isMentor) "Passionate mentor ready to share knowledge and guide learners." else "Eager learner seeking skill exchange and mentorship.",
            rating = 5.0f,
            reviewCount = 0,
            isVerified = true,
            skillsTaught = if (isMentor) skills else emptyList(),
            skillsWanted = if (!isMentor) skills else emptyList(),
            isMentor = isMentor
        )
    }

    companion object {
        fun fromUser(user: User, password: String = "", isGoogleUser: Boolean = false): UserAccount {
            return UserAccount(
                id = user.id,
                email = user.email,
                password = password,
                name = user.name,
                phone = user.phone,
                dob = user.dob,
                location = user.location,
                skills = if (user.isMentor) user.skillsTaught else user.skillsWanted,
                isMentor = user.isMentor,
                avatarUrl = user.avatarUrl,
                isGoogleUser = isGoogleUser
            )
        }
    }
}
