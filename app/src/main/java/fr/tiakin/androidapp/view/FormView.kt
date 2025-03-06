package fr.tiakin.androidapp.view

import android.Manifest
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.ResultBackNavigator
import fr.tiakin.androidapp.R
import fr.tiakin.androidapp.data.Product
import coil.compose.AsyncImage
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


enum class ProductType {
    Consumable,
    Durable,
    Other
}

private object FormView {
    var idCounter = 0
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Destination
@Composable
fun FormView(navigator: DestinationsNavigator, resultBackNavigator: ResultBackNavigator<Product>, product: Product? = null) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var date by remember { mutableStateOf(product?.date ?: "") }
    var color by remember { mutableStateOf(product?.color ?: "") }
    var origin by remember { mutableStateOf(product?.origin ?: "") }
    var isFavorite by remember { mutableStateOf(product?.isFavorite ?: false) }
    var selectedType by remember { mutableStateOf(product?.type ?: ProductType.Other) }
    var photoUri by remember { mutableStateOf<Uri?>(product?.imageUri?.let { Uri.parse(it) }) }

    var produit by remember { mutableStateOf<Product?>(null) }

    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    var shouldShowDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showImagePicker by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val context = LocalContext.current

    fun newId(): Int {
        FormView.idCounter++
        return FormView.idCounter
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Image(
            painter = painterResource(id = R.drawable.style),
            contentDescription = "Product Image",
            modifier = Modifier.size(100.dp)
        )

        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nom du produit*") },
            modifier = Modifier.fillMaxWidth()
        )

        Text("Date d'achat*")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .border(1.dp, Color.Gray, MaterialTheme.shapes.small)
                .clickable { showDatePicker = true }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (date.isEmpty()) "Sélectionner une date" else date,
                color = Color.Gray
            )
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()

            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    Button(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            date = sdf.format(Date(millis))
                        }
                        showDatePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDatePicker = false }) {
                        Text("Annuler")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        Column {
            val currentColor = try {
                if (color.isNotEmpty() && color.startsWith("#"))
                    Color(android.graphics.Color.parseColor(color))
                else Color.Gray
            } catch (e: Exception) { Color.Gray }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clickable { showColorPicker = true }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(currentColor)
                        .border(1.dp, Color.Black)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(color.ifEmpty { "Clique pour choisir une couleur" })
            }

            if (showColorPicker) {
                Dialog(onDismissRequest = { showColorPicker = false }) {
                    Surface(
                        modifier = Modifier.padding(16.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val controller = rememberColorPickerController()

                            HsvColorPicker(
                                controller = controller,
                                onColorChanged = { colorEnvelope ->
                                    // Store hex color code
                                    color = "#${colorEnvelope.hexCode}"
                                },
                                modifier = Modifier
                                    .height(300.dp)
                                    .fillMaxWidth()
                            )

                            BrightnessSlider(
                                controller = controller,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = { showColorPicker = false },
                                    modifier = Modifier.padding(top = 16.dp)
                                ) {
                                    Text("Confirmer")
                                }
                            }
                        }
                    }
                }
            }
        }

        TextField(
            value = origin,
            onValueChange = { origin = it },
            label = { Text("Pays d'origine") },
            modifier = Modifier.fillMaxWidth()
        )

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

        val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

        val getTmpFileUri = { context: Context ->
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val photoFile = File.createTempFile(
                "PHOTO_${timeStamp}_",
                ".jpg",
                context.cacheDir
            )
            FileProvider.getUriForFile(context, "${context.packageName}.provider", photoFile)
        }

        val galleryLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let {
                photoUri = it
                Toast.makeText(context, "Image sélectionnée depuis la galerie", Toast.LENGTH_SHORT).show()
            }
        }

        val cameraLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture()
        ) { success ->
            if (success) {
                photoUri = tempPhotoUri
                Toast.makeText(context, "Photo prise avec succès", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Erreur lors de la prise de photo", Toast.LENGTH_LONG).show()
            }
        }

        Box(
            modifier = Modifier
                .size(100.dp)
                .padding(vertical = 8.dp)
        ) {
            if (photoUri != null) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "Captured photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = "default Image",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Button(onClick = {
            showImagePicker = true
        }) {
            Text("Prendre/choisir une photo")
        }

        if (showImagePicker) {
            AlertDialog(
                onDismissRequest = { showImagePicker = false },
                title = { Text("Sélectionner une image") },
                text = { Text("Comment souhaitez-vous ajouter une image ?") },
                confirmButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Button(
                            onClick = {
                                showImagePicker = false
                                if (cameraPermissionState.status.isGranted) {
                                    try {
                                        tempPhotoUri = getTmpFileUri(context)
                                        cameraLauncher.launch(tempPhotoUri)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Erreur: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    cameraPermissionState.launchPermissionRequest()
                                }
                            }
                        ) {
                            Text("Caméra")
                        }

                        Button(
                            onClick = {
                                showImagePicker = false
                                galleryLauncher.launch("image/*")
                            }
                        ) {
                            Text("Galerie")
                        }
                    }
                },
                dismissButton = {
                    Button(onClick = { showImagePicker = false }) {
                        Text("Annuler")
                    }
                }
            )
        }



        Button(onClick = {
            if(name.isEmpty() || date.isEmpty()) {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        "Veuillez remplir les champs obligatoires",
                        duration = SnackbarDuration.Short)
                }
            } else {
                val updatedProduct = if (product != null) {
                    product.copy(
                        name = name,
                        date = date,
                        color = color,
                        origin = origin,
                        isFavorite = isFavorite,
                        type = selectedType,
                        imageUri = photoUri?.toString()
                    )
                } else {
                    Product(
                        newId(),
                        name,
                        date,
                        color,
                        origin,
                        isFavorite,
                        selectedType,
                        photoUri?.toString()
                    )
                }
                produit = updatedProduct
                shouldShowDialog = true
            }
        }) {
            Text(if (product == null) "Valider" else "Modifier")
        }
    }

    if (shouldShowDialog) {
        AlertDialog(
            onDismissRequest = {
                shouldShowDialog = false
            },
            title = { Text(if (product == null) "Détails du produit" else "Modifications du produit") },
            text = { Text(text = "Type: $selectedType\nNom: $name\nDate: $date\nCouleur: $color\nOrigine: $origin\nFavoris: $isFavorite") },
            confirmButton = {
                Button(
                    onClick = {
                        shouldShowDialog = false
                        produit?.let {
                            resultBackNavigator.navigateBack(it)
                        }
                    }
                ) {
                    Text(
                        text = "Confirmer"
                    )
                }
            }
        )
    }

    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier
            .padding(16.dp)
    )
}
