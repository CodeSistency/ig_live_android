package com.plcoding.daggerhiltcourse

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioMetadata
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradient
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.plcoding.daggerhiltcourse.ui.theme.DaggerHiltCourseTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DaggerHiltCourseTheme {
//                val viewModel = hiltViewModel<MyViewModel>()
                Router()
            }
        }
    }


    @Preview
    @Composable
    fun Router(){
        val navController = rememberNavController()
        val viewModel = hiltViewModel<MyViewModel>()
        NavHost(navController = navController, startDestination = "home") {
            composable("home") { Home(navController, viewModel) }
            composable("live") { Live(navController, viewModel) }
            /*...*/
        }
    }

    @Composable
    fun Home(navController: NavHostController, viewModel: MyViewModel) {

        var name by remember {
            mutableStateOf("")
        }


        Column {
            Text(text = "Bienvenido")
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "Ingrese su nombre")
            TextField(value = name, onValueChange = {
                name = it
            })
            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = {
                if (name.isNullOrBlank()){
                    Toast.makeText(applicationContext, "Ingrese un nombre", Toast.LENGTH_SHORT)
                }else{
                    viewModel.nombre = name
                    navController.navigate("live")
                }
            }) {
                Text(text = "Continuar")
            }

        }
    }

    @Composable
    fun Live(navController: NavHostController, viewModel: MyViewModel) {

        var comment by remember {
            mutableStateOf("")
        }
        var items by remember { mutableStateOf(List(10) { "comment$it" }) }

        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current

        var hasCamPermission by remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            )
        }

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { granted ->
                hasCamPermission = granted

                if (!granted) {
                    // User denied the permission, show a toast or handle accordingly
                    Toast.makeText(
                        context,
                        "Permiso de la camara denegado, vaya a los ajustes",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )

//        var seguidores = viewModel.quantity?.let { viewModel.simulateFluctuation(it, 5) }
        LaunchedEffect(viewModel) {
            while (true) {
                delay(1500L)
                viewModel.updateViewerCount()
            }
        }


        LaunchedEffect(key1 = true) {
            if (!hasCamPermission) {
                launcher.launch(Manifest.permission.CAMERA)
            }
        }

        LaunchedEffect(items) {
            while (true) {
                delay(1000) // Delay for one second
                items = items.drop(1) + items.take(1)
            }
        }

            AndroidView(
                { context ->
                    val cameraExecutor = Executors.newSingleThreadExecutor()
                    val previewView = PreviewView(context).also {
                        it.scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                    cameraProviderFuture.addListener({
                        val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                        val preview = androidx.camera.core.Preview.Builder()
                            .build()
                            .also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }



                        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                        try {
                            // Unbind use cases before rebinding
                            cameraProvider.unbindAll()

                            // Bind use cases to camera
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner, cameraSelector, preview
                            )

                        } catch (exc: Exception) {
                            Log.e("DEBUG", "Use case binding failed", exc)
                        }
                    }, ContextCompat.getMainExecutor(context))
                    previewView
                },
                modifier = Modifier
                    .fillMaxSize()
            )


        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp, top = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,

                ) {
                    Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null,
                        modifier = Modifier.size(50.dp))
                    Text(text = viewModel.nombre.toString())
//                    Icon(painterResource(id = R.drawable.ic_verified), contentDescription = null,
//                        modifier = Modifier.size(20.dp))
                    Image(painterResource(id = R.drawable.ic_verified), contentDescription = null,
                        modifier = Modifier.size(20.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colors.primaryVariant,
                                    MaterialTheme.colors.secondaryVariant
                                ),

//                                start = Offset(0f, 0f), // Diagonal start point
//                                end = Offset(1f, 1f)    // Diagonal end point
                            ),
                            shape = RoundedCornerShape(4.dp)
                        )

                        .height(30.dp)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        ,

                        contentAlignment = Alignment.Center
                        ){
                        Text(text = "LIVE", color = Color.White,
                            fontSize = 12.sp,
                            )
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    Box(modifier = Modifier
                        .background(
                            Color(0f, 0f, 0f, 0.5f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .height(30.dp)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ){
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(painterResource(id = R.drawable.ic_eye), contentDescription = null,
                                modifier = Modifier.size(15.dp))
                            Text(text = viewModel.quantity.toString(),
                                color = Color.White,
                                fontSize = 12.sp,

                                )
                        }
                    }
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                            )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Column {
                Box {
//                    LazyColumn(
//                        modifier = Modifier
//                            .fillMaxWidth(0.5f)
//                            .height(250.dp)
//
//
//                    ){
//                        items(10){ index ->
//                            Row(
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null,
//                                    modifier = Modifier.size(50.dp))
//                                Column {
//                                    Text(text = "user${index}")
//                                    Text(text = "comentario")
//
//                                }
//                            }
//                        }
//                    }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(250.dp)


                    ){
                        items(items.size){ index ->
                            AnimatedItem(index = index, item = items[index], onDismiss = {
                                items = items.drop(1) + items.take(1)
                            })
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, top = 10.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
//                
                    OutlinedTextField(value = comment, onValueChange = {
                        comment = it
                    },
                        shape = RoundedCornerShape(percent = 50),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color.LightGray,
                            unfocusedBorderColor = Color.LightGray),
                        placeholder = {
                            Text(text = "Comentario",
                                color = Color.White
                            )
                        },
                        trailingIcon = {
                            Icon(painterResource(id = R.drawable.ic_more), contentDescription = null,
                                tint = Color.LightGray
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(imageVector = Icons.Default.FavoriteBorder, contentDescription = null,
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                                .size(40.dp),
                            tint = Color.LightGray
                        )
                    }
                }
            }
        }
    }


    @Composable
    fun AnimatedItem(index: Int, item: String, onDismiss: () -> Unit) {
        var dismissed by remember { mutableStateOf(false) }
        val scrollPosition = remember { Animatable(initialValue = 0f) }

        if (!dismissed) {
            LaunchedEffect(index) {
                delay(index * 1000L) // Delay based on the index

                // Animate the item going up and disappearing
                launch {
                    scrollPosition.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 500, easing = LinearEasing)
                    )
                    onDismiss()
                }
            }

            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .offset(y = (-scrollPosition.value * 50).dp)
                    .alpha(1 - scrollPosition.value)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(MaterialTheme.shapes.small)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "user$index",
                            color = Color.Black
                        )
                        Text(text = item)
                    }
                }
            }
        }
    }


    fun generateDummyData(): List<String> {
        return List(10) { "comment$it" }
    }

    @Composable
    fun GradientOverlay() {
        val gradientHeight = 40.dp

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(gradientHeight)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Unspecified),
                        startY = 0f,
                        endY = 1f
                    )
                )
        )
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

       val viewModel by viewModels<MyViewModel>()

        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> viewModel.quantity = viewModel.quantity?.minus(1)
            KeyEvent.KEYCODE_VOLUME_UP -> viewModel.quantity = viewModel.quantity?.plus(1)
//            KeyEvent.KEYCODE_BACK -> Toast.makeText(applicationContext, "Back Key Pressed", Toast.LENGTH_SHORT).show()
        }
        return true
    }
}