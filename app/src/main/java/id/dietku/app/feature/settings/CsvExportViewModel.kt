package id.dietku.app.feature.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.dietku.app.data.CsvExportRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CsvExportViewModel @Inject constructor(
    private val repository: CsvExportRepository,
) : ViewModel() {
    private val _exportedFiles = MutableSharedFlow<Uri>(extraBufferCapacity = 1)
    val exportedFiles = _exportedFiles.asSharedFlow()

    fun export() {
        viewModelScope.launch {
            _exportedFiles.emit(repository.exportAll())
        }
    }
}