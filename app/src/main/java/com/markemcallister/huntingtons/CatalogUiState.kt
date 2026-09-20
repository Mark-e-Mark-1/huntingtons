package com.markemcallister.huntingtons

import com.markemcallister.huntingtons.data.Catalog

sealed class CatalogUiState {
    data object Loading : CatalogUiState()
    data class Ready(val catalog: Catalog) : CatalogUiState()
    data class Failed(val message: String) : CatalogUiState()
}
