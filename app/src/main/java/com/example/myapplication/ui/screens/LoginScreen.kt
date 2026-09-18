package com.example.myapplication.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.AppOutlinedTextFieldColors
import com.example.myapplication.ui.theme.LoginTheme
import com.example.myapplication.ui.viewmodel.LoginUiState
import com.example.myapplication.ui.viewmodel.LoginViewModel
import kotlin.math.PI
import kotlin.math.sin

private data class BackgroundStar(
    val x: Float,
    val y: Float,
    val phase: Float,
)

private val backgroundStars = listOf(
    BackgroundStar(0.08f, 0.13f, 0.0f),
    BackgroundStar(0.18f, 0.38f, 0.9f),
    BackgroundStar(0.31f, 0.19f, 1.8f),
    BackgroundStar(0.42f, 0.72f, 2.7f),
    BackgroundStar(0.54f, 0.10f, 3.6f),
    BackgroundStar(0.63f, 0.58f, 4.5f),
    BackgroundStar(0.72f, 0.28f, 5.4f),
    BackgroundStar(0.81f, 0.82f, 0.6f),
    BackgroundStar(0.91f, 0.48f, 1.5f),
    BackgroundStar(0.13f, 0.88f, 2.4f),
    BackgroundStar(0.48f, 0.43f, 3.3f),
    BackgroundStar(0.88f, 0.08f, 4.2f),
)

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Se conserva la navegación existente después de un login exitoso.
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onLoginSuccess()
        }
    }

    LoginTheme {
        LoginScreenContent(
            uiState = uiState,
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onLogin = viewModel::login,
            onForgotPassword = { viewModel.forgotPassword(uiState.email) },
        )
    }
}

@Composable
private fun LoginScreenContent(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onForgotPassword: () -> Unit,
    animateEntrance: Boolean = true,
) {
    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600 ||
        configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val scrollState = rememberScrollState()
    var contentVisible by remember(animateEntrance) {
        mutableStateOf(!animateEntrance)
    }

    LaunchedEffect(animateEntrance) {
        contentVisible = true
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedBackground(modifier = Modifier.matchParentSize())

            if (isWideScreen) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 32.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AnimatedVisibility(
                        visible = contentVisible,
                        modifier = Modifier
                            .weight(1f)
                            .widthIn(max = 520.dp),
                        enter = fadeIn(
                            animationSpec = tween(
                                durationMillis = 700,
                                delayMillis = 80,
                            ),
                        ) + slideInVertically(
                            animationSpec = tween(
                                durationMillis = 700,
                                delayMillis = 80,
                                easing = LinearOutSlowInEasing,
                            ),
                            initialOffsetY = { -it / 10 },
                        ),
                    ) {
                        BrandHeader(
                            modifier = Modifier.fillMaxWidth(),
                            compact = false,
                            animate = animateEntrance,
                        )
                    }

                    AnimatedVisibility(
                        visible = contentVisible,
                        enter = fadeIn(
                            animationSpec = tween(
                                durationMillis = 800,
                                delayMillis = 260,
                            ),
                        ),
                    ) {
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(360.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(MaterialTheme.colorScheme.outlineVariant),
                        )
                    }

                    AnimatedVisibility(
                        visible = contentVisible,
                        modifier = Modifier
                            .weight(1f)
                            .widthIn(max = 520.dp),
                        enter = fadeIn(
                            animationSpec = tween(
                                durationMillis = 700,
                                delayMillis = 200,
                            ),
                        ) + slideInVertically(
                            animationSpec = tween(
                                durationMillis = 700,
                                delayMillis = 200,
                                easing = LinearOutSlowInEasing,
                            ),
                            initialOffsetY = { it / 8 },
                        ),
                    ) {
                        LoginForm(
                            modifier = Modifier.fillMaxWidth(),
                            uiState = uiState,
                            onEmailChange = onEmailChange,
                            onPasswordChange = onPasswordChange,
                            onLogin = onLogin,
                            onForgotPassword = onForgotPassword,
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(28.dp),
                ) {
                    AnimatedVisibility(
                        visible = contentVisible,
                        modifier = Modifier.fillMaxWidth(),
                        enter = fadeIn(
                            animationSpec = tween(
                                durationMillis = 650,
                                delayMillis = 60,
                            ),
                        ) + slideInVertically(
                            animationSpec = tween(
                                durationMillis = 650,
                                delayMillis = 60,
                                easing = LinearOutSlowInEasing,
                            ),
                            initialOffsetY = { -it / 10 },
                        ),
                    ) {
                        BrandHeader(
                            modifier = Modifier.fillMaxWidth(),
                            compact = true,
                            animate = animateEntrance,
                        )
                    }

                    AnimatedVisibility(
                        visible = contentVisible,
                        modifier = Modifier.fillMaxWidth(),
                        enter = fadeIn(
                            animationSpec = tween(
                                durationMillis = 700,
                                delayMillis = 180,
                            ),
                        ) + slideInVertically(
                            animationSpec = tween(
                                durationMillis = 700,
                                delayMillis = 180,
                                easing = LinearOutSlowInEasing,
                            ),
                            initialOffsetY = { it / 8 },
                        ),
                    ) {
                        LoginForm(
                            modifier = Modifier.fillMaxWidth(),
                            uiState = uiState,
                            onEmailChange = onEmailChange,
                            onPasswordChange = onPasswordChange,
                            onLogin = onLogin,
                            onForgotPassword = onForgotPassword,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedBackground(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "loginBackground")
    val orangeProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 14_000,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "orangeGlow",
    )
    val blueProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 18_000,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "blueGlow",
    )
    val purpleProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 22_000,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "purpleGlow",
    )
    val twinklePhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 6_000,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "backgroundTwinkle",
    )

    val orange = MaterialTheme.colorScheme.primary
    val blue = MaterialTheme.colorScheme.tertiary
    val purple = MaterialTheme.colorScheme.secondary

    Canvas(modifier = modifier) {
        val maxDimension = maxOf(size.width, size.height)

        fun animatedOffset(
            startX: Float,
            startY: Float,
            endX: Float,
            endY: Float,
            progress: Float,
        ) = Offset(
            x = size.width * (startX + (endX - startX) * progress),
            y = size.height * (startY + (endY - startY) * progress),
        )

        fun drawGlow(
            color: Color,
            center: Offset,
            radius: Float,
            alpha: Float,
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = alpha),
                        color.copy(alpha = alpha * 0.35f),
                        Color.Transparent,
                    ),
                    center = center,
                    radius = radius,
                ),
                center = center,
                radius = radius,
            )
        }

        val orangeCenter = animatedOffset(
            startX = -0.10f,
            startY = 0.08f,
            endX = 0.30f,
            endY = 0.26f,
            progress = orangeProgress,
        )
        val blueCenter = animatedOffset(
            startX = 0.95f,
            startY = 0.18f,
            endX = 0.62f,
            endY = 0.46f,
            progress = blueProgress,
        )
        val purpleCenter = animatedOffset(
            startX = 0.18f,
            startY = 0.96f,
            endX = 0.78f,
            endY = 0.74f,
            progress = purpleProgress,
        )

        drawGlow(
            color = orange,
            center = orangeCenter,
            radius = maxDimension * 0.42f,
            alpha = 0.18f,
        )
        drawGlow(
            color = blue,
            center = blueCenter,
            radius = maxDimension * 0.38f,
            alpha = 0.13f,
        )
        drawGlow(
            color = purple,
            center = purpleCenter,
            radius = maxDimension * 0.36f,
            alpha = 0.11f,
        )

        drawCircle(
            color = orange.copy(alpha = 0.10f),
            center = orangeCenter,
            radius = maxDimension * 0.23f,
            style = Stroke(width = 1.dp.toPx()),
        )
        drawCircle(
            color = blue.copy(alpha = 0.08f),
            center = blueCenter,
            radius = maxDimension * 0.20f,
            style = Stroke(width = 1.dp.toPx()),
        )

        backgroundStars.forEachIndexed { index, star ->
            val pulse = (
                sin((twinklePhase + star.phase).toDouble()).toFloat() + 1f
                ) / 2f
            drawCircle(
                color = Color.White.copy(alpha = 0.05f + pulse * 0.20f),
                radius = (0.8f + (index % 3) * 0.45f).dp.toPx(),
                center = Offset(
                    x = size.width * star.x,
                    y = size.height * star.y,
                ),
            )
        }
    }
}

@Composable
private fun BrandHeader(
    modifier: Modifier,
    compact: Boolean,
    animate: Boolean,
) {
    var featuresVisible by remember(animate) {
        mutableStateOf(!animate)
    }

    LaunchedEffect(animate) {
        featuresVisible = true
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(if (compact) 16.dp else 24.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "SGTM",
                style = if (compact) {
                    MaterialTheme.typography.titleLarge
                } else {
                    MaterialTheme.typography.headlineMedium
                },
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold,
            )
        }

        Text(
            text = "Tu motocicleta en las mejores manos.",
            style = if (compact) {
                MaterialTheme.typography.headlineMedium
            } else {
                MaterialTheme.typography.headlineLarge
            },
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.ExtraBold,
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FeatureItem(
                text = "Gestión de clientes y motocicletas.",
                visible = featuresVisible,
                delayMillis = 180,
            )
            FeatureItem(
                text = "Control de órdenes de trabajo.",
                visible = featuresVisible,
                delayMillis = 320,
            )
            FeatureItem(
                text = "Inventario y repuestos en tiempo real.",
                visible = featuresVisible,
                delayMillis = 460,
            )
        }
    }
}

@Composable
private fun FeatureItem(
    text: String,
    visible: Boolean,
    delayMillis: Int,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 480,
                delayMillis = delayMillis,
            ),
        ) + slideInHorizontally(
            animationSpec = tween(
                durationMillis = 480,
                delayMillis = delayMillis,
                easing = LinearOutSlowInEasing,
            ),
            initialOffsetX = { -it / 5 },
        ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                modifier = Modifier.size(22.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LoginForm(
    modifier: Modifier,
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onForgotPassword: () -> Unit,
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var emailFocused by remember { mutableStateOf(false) }
    var passwordFocused by remember { mutableStateOf(false) }
    val primary = MaterialTheme.colorScheme.primary

    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Bienvenido de vuelta",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Accede a tu panel administrativo para gestionar el taller.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f),
                    lineHeight = 20.sp
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = onEmailChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { emailFocused = it.isFocused },
                    label = { Text("Correo electrónico", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = if (emailFocused) primary else Color.White.copy(alpha = 0.4f)
                        )
                    },
                    singleLine = true,
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedLabelColor = primary,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.4f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                    ),
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = onPasswordChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { passwordFocused = it.isFocused },
                        label = { Text("Contraseña", fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (passwordFocused) primary else Color.White.copy(alpha = 0.4f)
                            )
                        },
                        trailingIcon = {
                            TextButton(
                                onClick = { passwordVisible = !passwordVisible },
                                enabled = !uiState.isLoading,
                            ) {
                                Text(
                                    text = if (passwordVisible) "Ocultar" else "Mostrar",
                                    color = primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        },
                        singleLine = true,
                        enabled = !uiState.isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedLabelColor = primary,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.4f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        visualTransformation = if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                    )

                    Text(
                        text = "¿Olvidaste tu contraseña?",
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable(
                                enabled = !uiState.isLoading,
                                onClick = onForgotPassword,
                            ),
                        style = MaterialTheme.typography.labelMedium,
                        color = primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Button(
                onClick = onLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primary,
                    contentColor = Color.White,
                    disabledContainerColor = primary.copy(alpha = 0.5f),
                    disabledContentColor = Color.White.copy(alpha = 0.7f),
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 2.dp
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp,
                        color = Color.White,
                    )
                } else {
                    Text(
                        text = "Entrar al Sistema",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            AnimatedVisibility(
                visible = uiState.errorMessage != null,
                enter = fadeIn(tween(durationMillis = 220)) + slideInVertically(
                    animationSpec = tween(durationMillis = 220),
                    initialOffsetY = { it / 4 },
                ),
                exit = fadeOut(tween(durationMillis = 160)),
            ) {
                Text(
                    text = uiState.errorMessage.orEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Preview(
    name = "Login celular",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun LoginScreenPhonePreview() {
    LoginTheme {
        LoginScreenContent(
            uiState = LoginUiState(),
            onEmailChange = {},
            onPasswordChange = {},
            onLogin = {},
            onForgotPassword = {},
            animateEntrance = false,
        )
    }
}

@Preview(
    name = "Login pantalla grande",
    showBackground = true,
    widthDp = 900,
    heightDp = 600,
)
@Composable
private fun LoginScreenLargePreview() {
    LoginTheme {
        LoginScreenContent(
            uiState = LoginUiState(),
            onEmailChange = {},
            onPasswordChange = {},
            onLogin = {},
            onForgotPassword = {},
            animateEntrance = false,
        )
    }
}
