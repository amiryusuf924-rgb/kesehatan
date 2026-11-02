package id.dietku.app.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.dietku.app.data.ProfileRepository
import id.dietku.app.data.local.UserProfileEntity
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
) : ViewModel() {
    val profile = repository.observeProfile().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    init {
        viewModelScope.launch {
            if (repository.getProfile() == null) {
                repository.saveProfile(UserProfileEntity())
            }
        }
    }

    fun saveProfile(profile: UserProfileEntity) {
        viewModelScope.launch { repository.saveProfile(profile) }
    }
}