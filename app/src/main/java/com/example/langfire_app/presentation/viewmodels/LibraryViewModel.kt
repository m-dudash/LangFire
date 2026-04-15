package com.example.langfire_app.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.langfire_app.domain.model.LibraryLevelSection
import com.example.langfire_app.domain.usecase.GetLibraryContentUseCase
import com.example.langfire_app.domain.repository.SettingsRepository
import com.example.langfire_app.domain.repository.CourseRepository
import com.example.langfire_app.domain.model.Course
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val getLibraryContentUseCase: GetLibraryContentUseCase,
    private val settingsRepository: SettingsRepository,
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val _state = MutableStateFlow<List<LibraryLevelSection>>(emptyList())
    val state = _state.asStateFlow()

    private val _courseInfo = MutableStateFlow<Course?>(null)
    val courseInfo = _courseInfo.asStateFlow()

    fun loadLibrary(courseId: Int? = null) {
        viewModelScope.launch {
            try {
                val targetCourseId = courseId ?: settingsRepository.getCurrentCourseId()
                if (targetCourseId != null) {
                    _state.value = getLibraryContentUseCase(targetCourseId)
                    _courseInfo.value = courseRepository.getCourseById(targetCourseId)
                }
            } catch (e: Exception) {

            }
        }
    }
}