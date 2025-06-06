package org.wikipedia.commons

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.wikipedia.Constants
import org.wikipedia.dataclient.ServiceFactory
import org.wikipedia.descriptions.DescriptionEditActivity
import org.wikipedia.suggestededits.PageSummaryForEdit
import org.wikipedia.util.Resource

class ImagePreviewViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val handler = CoroutineExceptionHandler { _, throwable ->
        _uiState.value = Resource.Error(throwable)
    }
    var pageSummaryForEdit = savedStateHandle.get<PageSummaryForEdit>(ImagePreviewDialog.ARG_SUMMARY)!!
    var action = savedStateHandle.get<DescriptionEditActivity.Action>(ImagePreviewDialog.ARG_ACTION)

    private val _uiState = MutableStateFlow(Resource<FilePage>())
    val uiState = _uiState.asStateFlow()

    init {
        loadImageInfo()
    }

    private fun loadImageInfo() {
        _uiState.value = Resource.Loading()
        viewModelScope.launch(handler) {
            var isFromCommons = false
            var firstPage = ServiceFactory.get(Constants.commonsWikiSite)
                .getImageInfo(pageSummaryForEdit.title, pageSummaryForEdit.lang).query?.firstPage()

            if (firstPage?.imageInfo() == null) {
                // If file page originally comes from *.wikipedia.org (i.e. movie posters), it will not have imageInfo and pageId.
                firstPage = ServiceFactory.get(pageSummaryForEdit.pageTitle.wikiSite)
                    .getImageInfo(pageSummaryForEdit.title, pageSummaryForEdit.lang).query?.firstPage()
            } else {
                // Fetch API from commons.wikimedia.org and check whether if it is not a "shared" image.
                isFromCommons = !(firstPage.isImageShared)
            }

            firstPage?.imageInfo()?.let { imageInfo ->
                pageSummaryForEdit.timestamp = imageInfo.timestamp
                pageSummaryForEdit.user = imageInfo.user
                pageSummaryForEdit.metadata = imageInfo.metadata

                val imageTagsResponse = async { ImageTagsProvider.getImageTags(firstPage.pageId, pageSummaryForEdit.lang) }

                val filePage = FilePage(
                    imageFromCommons = isFromCommons,
                    page = firstPage,
                    imageTags = imageTagsResponse.await(),
                    thumbnailWidth = imageInfo.thumbWidth,
                    thumbnailHeight = imageInfo.thumbHeight,
                )

                _uiState.value = Resource.Success(filePage)
            } ?: run {
                _uiState.value = Resource.Error(Throwable("No image info found."))
            }
        }
    }
}
