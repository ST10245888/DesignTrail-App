package vcmsa.projects.fkj_consultants.models

sealed class MaterialListItem {
    data class CategoryHeader(val categoryName: String) : MaterialListItem()
    data class MaterialEntry(val material: MaterialItem) : MaterialListItem()
}
