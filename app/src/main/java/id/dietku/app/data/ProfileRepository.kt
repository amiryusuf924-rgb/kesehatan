package id.dietku.app.data

import id.dietku.app.data.local.UserProfileDao
import id.dietku.app.data.local.UserProfileEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class ProfileRepository @Inject constructor(
    private val profileDao: UserProfileDao,
) {
    fun observeProfile(): Flow<UserProfileEntity?> = profileDao.observeProfile()

    suspend fun getProfile(): UserProfileEntity? = profileDao.getProfile()

    suspend fun saveProfile(profile: UserProfileEntity) = profileDao.saveProfile(profile)

    suspend fun updateCurrentWeight(weightKg: Double) {
        profileDao.getProfile()?.let { profile ->
            profileDao.saveProfile(profile.copy(currentWeightKg = weightKg))
        }
    }
}