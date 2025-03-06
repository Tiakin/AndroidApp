package fr.tiakin.androidapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.data.ContextCache
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.NavResult
import com.ramcosta.composedestinations.result.ResultRecipient
import fr.tiakin.androidapp.data.Product
import fr.tiakin.androidapp.destinations.FormViewDestination
import fr.tiakin.androidapp.ui.theme.AndroidAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidAppTheme  {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DestinationsNavHost(
                        navGraph = NavGraphs.root,
                        modifier = Modifier.padding(innerPadding)

                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume")
    }
}
@Destination(start = true)
@Composable
fun Greeting(navigator: DestinationsNavigator, resultRecipient: ResultRecipient<FormViewDestination, Product>) {
    var liste by rememberSaveable {
        mutableStateOf(emptyList<Product>())
    }
    resultRecipient.onNavResult {
        if (it is NavResult.Value) {
            val newProduct = it.value
            val existingIndex = liste.indexOfFirst { product -> product.id == newProduct.id }

            liste = if (existingIndex >= 0) {
                Log.d("MainActivity", "existingIndex")
                liste.toMutableList().apply {
                    set(existingIndex, newProduct)
                }
            } else {
                Log.d("MainActivity", "not existingIndex")
                liste + newProduct
            }
        }
    }

    val scrollState = rememberScrollState()

    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(16.dp)) {
        var searchQuery by remember { mutableStateOf("") }

        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Rechercher un produit") },
            modifier = Modifier.fillMaxWidth()
        )

        LazyRow {
            items(liste.filter { it.isFavorite }) { product ->
                Text(
                    product.name,
                    modifier = Modifier.padding(2.dp)
                )
            }
        }

        val filteredProducts = liste.filter {
            if(searchQuery.isEmpty()) {
                return@filter true
            }
            it.name.contains(searchQuery, ignoreCase = true)
        }
        ListeProduit(produits = filteredProducts,
            onDelete = { product -> liste = liste - product },
            onEdit = { product -> navigator.navigate(FormViewDestination(product))
        })
        Button(onClick = {
            navigator.navigate(FormViewDestination())
        }) {
            Text("Form")
        }

    }
}

@Composable
fun ListeProduit(produits: List<Product>, onDelete: (Product) -> Unit, onEdit: (Product) -> Unit) {
    Column {
        produits.forEach { product ->
            MessageRow(product, onEdit , onDelete)
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageRow(product: Product, onEdit: (Product) -> Unit, onDelete: (Product) -> Unit) {
    val context = LocalContext.current

    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
        .combinedClickable(
            onClick = {
                Toast.makeText(context, "Id: ${product.id} Nom: ${product.name} Date: ${product.date} Couleur: ${product.color} Origine: ${product.origin} Favori: ${product.isFavorite} Type: ${product.type}", Toast.LENGTH_LONG).show()
                onEdit(product)
            },
            onLongClick = { onDelete(product) }
        )
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .padding(vertical = 8.dp)
        ) {
            if (product.imageUri != null) {
                AsyncImage(
                    model = product.imageUri,
                    contentDescription = "photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = "image par défaut",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(product.name)
        }
    }
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AndroidAppTheme {
        val painter = painterResource(R.drawable.style)
        Image(
            painter = painter,
            "Le contenu de fou"
        )
    }
}