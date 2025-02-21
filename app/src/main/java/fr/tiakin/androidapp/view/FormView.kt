package fr.tiakin.androidapp.view

import android.app.AlertDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.ResultBackNavigator
import fr.tiakin.androidapp.R
import fr.tiakin.androidapp.data.Product

enum class ProductType {
    Consumable,
    Durable,
    Other
}

@Destination
@Composable
fun FormView(navigator: DestinationsNavigator, resultBackNavigator: ResultBackNavigator<Product>) {
    var id by rememberSaveable() { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var origin by remember { mutableStateOf("") }
    var isFavorite by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf(ProductType.Other) }

    var message = "";
    val shouldShowDialog = remember { mutableStateOf(false) }

    fun newId(): Int {
        id += 1
        return id
    }

    if (shouldShowDialog.value) {
        MyAlertDialog(shouldShowDialog = shouldShowDialog, message)
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Image(
            painter = painterResource(id = R.drawable.style),
            contentDescription = "Product Image",
            modifier = Modifier.size(100.dp)
        )

        Text("Nom du produit*")
        BasicTextField(value = name, onValueChange = { name = it })

        Text("Date d'achat*")
        BasicTextField(value = date, onValueChange = { date = it })

        Text("Couleur")
        BasicTextField(value = color, onValueChange = { color = it })

        Text("Pays d'origine")
        BasicTextField(value = origin, onValueChange = { origin = it })

        Row {
            RadioButton(selected = selectedType == ProductType.Consumable, onClick = { selectedType = ProductType.Consumable })
            Text("Consommable")
            RadioButton(selected = selectedType == ProductType.Durable, onClick = { selectedType = ProductType.Durable })
            Text("Durable")
            RadioButton(selected = selectedType == ProductType.Other, onClick = { selectedType = ProductType.Other })
            Text("Autre")
        }

        Row {
            Checkbox(checked = isFavorite, onCheckedChange = { isFavorite = it })
            Text("Ajouter aux favoris")
        }

        var selectedImage by remember { mutableStateOf(R.drawable.ic_launcher_background) }

        Image(
            painter = painterResource(id = selectedImage),
            contentDescription = "Product Image",
            modifier = Modifier.size(100.dp)
        )

        Button(onClick = {
            selectedImage = R.drawable.ic_launcher_foreground
        }) {
            Text("Choisir une image")
        }


        Button(onClick = {
            message = "Type: $selectedType\nNom: $name\nDate: $date\nCouleur: $color\nOrigine: $origin\nFavoris: $isFavorite"
            shouldShowDialog.value = true
            val prod = Product(newId(), name, date, color, origin, isFavorite, selectedType, selectedImage)

            resultBackNavigator.navigateBack(prod)
        }) {
            Text("Valider")
        }
    }
}
@Composable
fun MyAlertDialog(shouldShowDialog: MutableState<Boolean>,message: String) {
    if (shouldShowDialog.value) { // 2
        AlertDialog( // 3
            onDismissRequest = { // 4
                shouldShowDialog.value = false
            },
            // 5
            title = { Text(text = "Détails du produit") },
            text = { Text(text = message) },
            confirmButton = { // 6
                Button(
                    onClick = {
                        shouldShowDialog.value = false
                    }
                ) {
                    Text(
                        text = "Confirm",
                        color = Color.White
                    )
                }
            }
        )
    }
}
