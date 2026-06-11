package com.example.shoppinglist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shoppinglist.data.Product
import com.example.shoppinglist.ui.theme.ShoppingListTheme
import com.example.shoppinglist.viewmodel.ShoppingViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: ShoppingViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return ShoppingViewModel(applicationContext) as T
                    }
                }
            )
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()

            ShoppingListTheme(
                darkTheme = isDarkTheme
            ) {
                ShoppingListApp(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListApp(viewModel: ShoppingViewModel) {
    val products = viewModel.products
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }
    var newProductName by remember { mutableStateOf("") }
    var newProductQuantity by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📝 Список покупок") },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Настройки")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(products) { product ->
                    ProductItem(
                        product = product,
                        onToggleBought = { viewModel.toggleBought(product) },
                        onDelete = {
                            productToDelete = product
                            showDeleteDialog = true
                        },
                        onQuantityChange = { newQuantity ->
                            viewModel.updateQuantity(product, newQuantity)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // Диалог добавления продукта
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Новый продукт") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newProductName,
                            onValueChange = { newProductName = it },
                            label = { Text("Название") },
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newProductQuantity,
                            onValueChange = {
                                // Разрешаем только цифры
                                if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                    newProductQuantity = it
                                }
                            },
                            label = { Text("Количество") },
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newProductName.isNotBlank()) {
                                val quantity = newProductQuantity.toIntOrNull() ?: 1
                                viewModel.addProduct(newProductName, quantity)
                                newProductName = ""
                                newProductQuantity = ""
                                showDialog = false
                            }
                        }
                    ) {
                        Text("Добавить")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Отмена")
                    }
                }
            )
        }

        // Диалог настроек
        if (showSettingsDialog) {
            AlertDialog(
                onDismissRequest = { showSettingsDialog = false },
                title = { Text("Настройки") },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "🌙 Тёмная тема",
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { viewModel.toggleTheme(it) }
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSettingsDialog = false }) {
                        Text("Закрыть")
                    }
                }
            )
        }

        // Диалог подтверждения удаления
        if (showDeleteDialog && productToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    productToDelete = null
                },
                title = { Text("Удалить продукт") },
                text = { Text("Вы уверены, что хотите удалить \"${productToDelete?.name}\"?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            productToDelete?.let { viewModel.deleteProduct(it) }
                            showDeleteDialog = false
                            productToDelete = null
                        }
                    ) {
                        Text("Удалить")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        productToDelete = null
                    }) {
                        Text("Отмена")
                    }
                }
            )
        }
    }
}

@Composable
fun ProductItem(
    product: Product,
    onToggleBought: () -> Unit,
    onDelete: () -> Unit,
    onQuantityChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (product.isBought)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        if (product.quantity > 1) {
                            onQuantityChange(product.quantity - 1)
                        }
                    },
                    enabled = product.quantity > 1
                ) {
                    Text(
                        text = "-",
                        fontSize = 20.sp,
                        color = if (product.quantity > 1) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }

                Text(
                    text = "${product.quantity}",
                    modifier = Modifier.padding(horizontal = 8.dp),
                    fontSize = 16.sp
                )

                IconButton(onClick = { onQuantityChange(product.quantity + 1) }) {
                    Text(
                        text = "+",
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Checkbox(
                checked = product.isBought,
                onCheckedChange = { onToggleBought() }
            )

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить")
            }
        }
    }
}