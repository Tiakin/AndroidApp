package fr.tiakin.androidapp.data

import fr.tiakin.androidapp.R
import fr.tiakin.androidapp.view.ProductType
import java.io.Serializable

data class Product(
    val id: Int,
    val name: String,
    val date: String,
    val color: String,
    val origin: String,
    val isFavorite: Boolean,
    val type: ProductType,
    val imageResId: Int = R.drawable.ic_launcher_background
)
 : Serializable


