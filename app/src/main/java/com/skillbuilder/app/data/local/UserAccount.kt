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
    val skillsWanted: List<String> = emptyList(),
    val isMentor: Boolean = false,
    val avatarUrl: String? = null,
    val isGoogleUser: Boolean = false
) {
    fun toUser(): User {
        val taught = if (isMentor) skills else emptyList()
        val wanted = if (skillsWanted.isNotEmpty()) {
            skillsWanted
        } else if (!isMentor) {
            skills
        } else {
            listOf("Artisan Sourdough", "Acoustic Guitar", "Mobile App Architecture")
        }
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
            skillsTaught = taught,
            skillsWanted = wanted,
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
                skillsWanted = user.skillsWanted,
                isMentor = user.isMentor,
                avatarUrl = user.avatarUrl,
                isGoogleUser = isGoogleUser
            )
        }
    }
}
