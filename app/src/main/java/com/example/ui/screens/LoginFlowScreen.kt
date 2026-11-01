package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Diversity1
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.AuthCredentialsConfig
import com.example.ui.components.glassmorphic
import com.example.ui.theme.MayaColorError
import com.example.ui.theme.MayaTheme
import kotlinx.coroutines.delay

enum class LoginStep {
    STANDARD_NAME,
    STANDARD_TERMS,
    RRR_FAMILY_LOGIN,
    RRR_FAMILY_NAME_TERMS,
    CEO_PANEL_LOGIN
}

@Composable
fun LoginFlowScreen(
    onStandardLogin: (String) -> Unit,
    onFamilyLogin: (String) -> Unit,
    onCeoLogin: (String) -> Unit,
    onVerifyFamilyCredentials: (String, String) -> com.example.data.model.FamilyMemberAccount? = { id, pass ->
        if (AuthCredentialsConfig.isValidFamilyCredentials(id, pass)) {
            com.example.data.model.FamilyMemberAccount(id = id, password = pass, memberName = "")
        } else null
    },
    initialLockoutMillis: Long = 0L,
    onRecordFailedAttempt: () -> Long = { 0L },
    onResetFailedAttempts: () -> Unit = {},
    hapticEnabled: Boolean = true
) {
    val theme = MayaTheme.colors
    var currentStep by rememberSaveable { mutableStateOf(LoginStep.STANDARD_NAME) }

    // Active User State for Navigation across steps
    var activeStandardName by rememberSaveable { mutableStateOf("") }
    var activeFamilyMemberName by rememberSaveable { mutableStateOf("") }
    var familyErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var ceoErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    // Countdown Lockout State
    var remainingLockoutSeconds by rememberSaveable {
        mutableLongStateOf(initialLockoutMillis / 1000L)
    }

    LaunchedEffect(remainingLockoutSeconds) {
        if (remainingLockoutSeconds > 0) {
            delay(1000L)
            remainingLockoutSeconds -= 1
            if (remainingLockoutSeconds == 0L) {
                familyErrorMessage = null
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.backgroundGradient)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        AnimatedContent(
            targetState = currentStep,
            contentKey = { it },
            transitionSpec = {
                slideInHorizontally { width -> width / 3 } + fadeIn() togetherWith
                        slideOutHorizontally { width -> -width / 3 } + fadeOut()
            },
            label = "LoginStepsAnimation"
        ) { step ->
            when (step) {
                LoginStep.STANDARD_NAME -> {
                    StandardNameScreen(
                        initialName = activeStandardName,
                        onNext = { name ->
                            activeStandardName = name
                            currentStep = LoginStep.STANDARD_TERMS
                        },
                        onNavigateToFamilyLogin = {
                            familyErrorMessage = null
                            currentStep = LoginStep.RRR_FAMILY_LOGIN
                        }
                    )
                }

                LoginStep.STANDARD_TERMS -> {
                    StandardTermsScreen(
                        userName = activeStandardName,
                        onBack = { currentStep = LoginStep.STANDARD_NAME },
                        onConfirm = {
                            if (activeStandardName.trim().isNotEmpty()) {
                                onStandardLogin(activeStandardName.trim())
                            }
                        }
                    )
                }

                LoginStep.RRR_FAMILY_LOGIN -> {
                    RrrFamilyLoginScreen(
                        errorMessage = familyErrorMessage,
                        remainingLockoutSeconds = remainingLockoutSeconds,
                        onLoginSubmit = { familyId, password ->
                            val matchedAccount = onVerifyFamilyCredentials(familyId, password)
                            if (matchedAccount != null) {
                                onResetFailedAttempts()
                                familyErrorMessage = null
                                activeFamilyMemberName = if (
                                    matchedAccount.memberName.isNotBlank() &&
                                    !matchedAccount.memberName.equals("RRR Family Master", ignoreCase = true) &&
                                    !matchedAccount.memberName.equals("RRR Family Member", ignoreCase = true)
                                ) {
                                    matchedAccount.memberName
                                } else {
                                    ""
                                }
                                currentStep = LoginStep.RRR_FAMILY_NAME_TERMS
                            } else {
                                val lockoutMs = onRecordFailedAttempt()
                                if (lockoutMs > 0) {
                                    remainingLockoutSeconds = lockoutMs / 1000L
                                }
                                familyErrorMessage = "Invalid ID or Password"
                            }
                        },
                        onOpenCeoPanel = {
                            ceoErrorMessage = null
                            currentStep = LoginStep.CEO_PANEL_LOGIN
                        },
                        onBackToStandard = {
                            currentStep = LoginStep.STANDARD_NAME
                        }
                    )
                }

                LoginStep.RRR_FAMILY_NAME_TERMS -> {
                    RrrFamilyNameTermsScreen(
                        initialMemberName = activeFamilyMemberName,
                        onBack = { currentStep = LoginStep.RRR_FAMILY_LOGIN },
                        onConfirm = { memberName ->
                            if (memberName.trim().isNotEmpty()) {
                                onFamilyLogin(memberName.trim())
                            }
                        }
                    )
                }

                LoginStep.CEO_PANEL_LOGIN -> {
                    CeoPanelLoginScreen(
                        errorMessage = ceoErrorMessage,
                        onLoginSubmit = { ceoId, password ->
                            if (AuthCredentialsConfig.isValidCeoCredentials(ceoId, password)) {
                                ceoErrorMessage = null
                                // Founder & CEO: Direct login as "Rizwan RR" without asking for name
                                onCeoLogin("Rizwan RR")
                            } else {
                                ceoErrorMessage = "Invalid CEO ID or Password"
                            }
                        },
                        onBackToFamily = {
                            currentStep = LoginStep.RRR_FAMILY_LOGIN
                        }
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// STEP 1: Standard Name Input Screen (Self-Contained Local Input State)
// -------------------------------------------------------------------------
@Composable
private fun StandardNameScreen(
    initialName: String,
    onNext: (String) -> Unit,
    onNavigateToFamilyLogin: () -> Unit
) {
    val theme = MayaTheme.colors
    var name by rememberSaveable { mutableStateOf(initialName) }
    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            // Official Maya Connect Logo Emblem
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F172A))
                    .border(2.dp, Color(0xFF38BDF8).copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.maya_connect_official_logo),
                    contentDescription = "Maya Connect Official Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Maya Connect",
                style = TextStyle(
                    color = theme.textPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp
                )
            )

            Text(
                text = "by REXLIFY",
                style = TextStyle(
                    color = theme.accentPrimary,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(38.dp))

            // Card Container for Name Entry
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .glassmorphic(
                        shape = RoundedCornerShape(24.dp),
                        backgroundColor = theme.surfaceGlass,
                        borderColor = theme.borderGlass,
                        borderWidth = 1.dp
                    )
                    .padding(22.dp)
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "Enter Your Name",
                        style = TextStyle(
                            color = theme.textPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Please enter your name to set up your personal space in Maya Connect.",
                        style = TextStyle(
                            color = theme.textSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (showError && it.isNotBlank()) showError = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("standard_name_input"),
                        label = { Text("Your Name") },
                        placeholder = { Text("Enter your name") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = theme.accentPrimary
                            )
                        },
                        isError = showError,
                        supportingText = if (showError) {
                            { Text("Name cannot be empty", color = MayaColorError) }
                        } else null,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (name.trim().isEmpty()) {
                                    showError = true
                                } else {
                                    onNext(name.trim())
                                }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = theme.accentPrimary,
                            unfocusedBorderColor = theme.borderGlass,
                            focusedTextColor = theme.textPrimary,
                            unfocusedTextColor = theme.textPrimary,
                            cursorColor = theme.accentPrimary
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    Button(
                        onClick = {
                            if (name.trim().isEmpty()) {
                                showError = true
                            } else {
                                onNext(name.trim())
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("standard_name_next_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = theme.accentPrimary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        enabled = name.trim().isNotEmpty()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Next",
                                style = TextStyle(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Bottom Secondary Link for RRR Family Access
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onNavigateToFamilyLogin() }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("rrr_family_link_btn")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Diversity1,
                        contentDescription = null,
                        tint = theme.accentSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Welcome to RRR Family",
                        style = TextStyle(
                            color = theme.accentSecondary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// STEP 2: Standard Terms & Conditions Approval Screen
// -------------------------------------------------------------------------
@Composable
private fun StandardTermsScreen(
    userName: String,
    onBack: () -> Unit,
    onConfirm: () -> Unit
) {
    val theme = MayaTheme.colors
    var isAgreed by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(theme.surfaceGlass)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = theme.textPrimary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Terms & Privacy",
                    style = TextStyle(
                        color = theme.textPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Welcome, $userName",
                    style = TextStyle(
                        color = theme.accentPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Scrollable Terms Container
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .glassmorphic(
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = theme.surfaceGlass,
                    borderColor = theme.borderGlass,
                    borderWidth = 1.dp
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "MAYA CONNECT — TERMS & CONDITIONS",
                    style = TextStyle(
                        color = theme.accentPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Last Updated: October 27, 2026 • Developer: REXLIFY",
                    style = TextStyle(
                        color = theme.textSecondary,
                        fontSize = 11.sp
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "1. Voluntary Use & Freedom\n" +
                            "Maya Connect is an ecosystem launcher and dashboard developed by REXLIFY. Users choose voluntarily to install and use this application. Users may stop using it or uninstall it at any time with ZERO exit fees or penalties.\n\n" +
                            "2. User Responsibility & Device Security\n" +
                            "Users are responsible for their device security and submitted entries. REXLIFY takes reasonable technical safeguards appropriate to its services.\n\n" +
                            "3. Data Privacy & Local Control\n" +
                            "You maintain complete ownership of your personal data. We do not sell your personal data to third parties. Complete on-device data deletion is provided inside Me > Settings at any time.\n\n" +
                            "4. Governing Law & Support\n" +
                            "These terms are governed by the laws of West Bengal, India. Official support is available via ${AuthCredentialsConfig.OFFICIAL_SUPPORT_EMAIL}.",
                    style = TextStyle(
                        color = theme.textPrimary.copy(alpha = 0.9f),
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Mandatory Agreement Checkbox
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(theme.surfaceGlass.copy(alpha = 0.6f))
                .clickable { isAgreed = !isAgreed }
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .testTag("standard_terms_checkbox_row"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isAgreed,
                onCheckedChange = { isAgreed = it },
                modifier = Modifier.testTag("standard_terms_checkbox"),
                colors = CheckboxDefaults.colors(
                    checkedColor = theme.accentPrimary,
                    uncheckedColor = theme.textSecondary,
                    checkmarkColor = Color.White
                )
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "I agree to the Terms & Conditions and Privacy Policy.",
                style = TextStyle(
                    color = theme.textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Confirm Button
        Button(
            onClick = onConfirm,
            enabled = isAgreed,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("standard_terms_agree_btn"),
            colors = ButtonDefaults.buttonColors(
                containerColor = theme.accentPrimary,
                disabledContainerColor = theme.accentPrimary.copy(alpha = 0.35f)
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Agree & Continue",
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }
    }
}

// -------------------------------------------------------------------------
// STEP 3: RRR Family Login Screen (Self-Contained Local Input State - Zero Text Doubling)
// -------------------------------------------------------------------------
@Composable
private fun RrrFamilyLoginScreen(
    errorMessage: String?,
    remainingLockoutSeconds: Long,
    onLoginSubmit: (id: String, pass: String) -> Unit,
    onOpenCeoPanel: () -> Unit,
    onBackToStandard: () -> Unit
) {
    val theme = MayaTheme.colors
    val isLockedOut = remainingLockoutSeconds > 0

    // Local self-contained input states to guarantee 1 keypress = 1 character
    var familyId by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackToStandard,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(theme.surfaceGlass)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = theme.textPrimary
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Back to Standard Sign In",
                style = TextStyle(
                    color = theme.textSecondary,
                    fontSize = 13.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Official Brand Logo
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, Color(0xFFA78BFA), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.maya_connect_official_logo),
                contentDescription = "Maya Connect Official Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "RRR Family Access",
            style = TextStyle(
                color = theme.textPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Text(
            text = "Exclusive access portal for verified RRR family members",
            style = TextStyle(
                color = theme.textSecondary,
                fontSize = 12.5.sp,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Lockout Warning Box
        if (isLockedOut) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MayaColorError.copy(alpha = 0.15f))
                    .border(1.dp, MayaColorError.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = MayaColorError,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Access Locked",
                            style = TextStyle(
                                color = MayaColorError,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Too many failed attempts. Try again in ${formatLockout(remainingLockoutSeconds)}",
                            style = TextStyle(
                                color = MayaColorError.copy(alpha = 0.9f),
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Login Card with Strictly "Enter ID" and "Enter Password"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .glassmorphic(
                    shape = RoundedCornerShape(22.dp),
                    backgroundColor = theme.surfaceGlass,
                    borderColor = theme.borderGlass,
                    borderWidth = 1.dp
                )
                .padding(20.dp)
        ) {
            Column {
                OutlinedTextField(
                    value = familyId,
                    onValueChange = { familyId = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rrr_family_id_input"),
                    label = { Text("Enter ID") },
                    placeholder = { Text("Enter ID") },
                    singleLine = true,
                    enabled = !isLockedOut,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = theme.accentSecondary
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = theme.accentSecondary,
                        unfocusedBorderColor = theme.borderGlass,
                        focusedTextColor = theme.textPrimary,
                        unfocusedTextColor = theme.textPrimary,
                        cursorColor = theme.accentSecondary
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rrr_family_password_input"),
                    label = { Text("Enter Password") },
                    placeholder = { Text("Enter Password") },
                    singleLine = true,
                    enabled = !isLockedOut,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = theme.accentSecondary
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Password Visibility",
                                tint = theme.textSecondary
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = theme.accentSecondary,
                        unfocusedBorderColor = theme.borderGlass,
                        focusedTextColor = theme.textPrimary,
                        unfocusedTextColor = theme.textPrimary,
                        cursorColor = theme.accentSecondary
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                if (errorMessage != null && !isLockedOut) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        style = TextStyle(
                            color = MayaColorError,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { onLoginSubmit(familyId, password) },
                    enabled = !isLockedOut && familyId.trim().isNotEmpty() && password.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("rrr_family_login_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.accentSecondary,
                        disabledContainerColor = theme.accentSecondary.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = if (isLockedOut) "Locked (${formatLockout(remainingLockoutSeconds)})" else "Next",
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Prominent CEO Panel Button at the bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                    )
                )
                .border(1.2.dp, Color(0xFF38BDF8).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .clickable { onOpenCeoPanel() }
                .padding(16.dp)
                .testTag("rrr_ceo_panel_entry_btn")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0284C7).copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "CEO Panel",
                            style = TextStyle(
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Executive clearance portal for Rizwan RR",
                            style = TextStyle(
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------------------
// STEP 4: RRR Family Member Name & Terms Acceptance Screen
// -------------------------------------------------------------------------
@Composable
private fun RrrFamilyNameTermsScreen(
    initialMemberName: String = "",
    onBack: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val theme = MayaTheme.colors
    var memberName by rememberSaveable(initialMemberName) { mutableStateOf(initialMemberName) }
    var isAgreed by rememberSaveable { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(theme.surfaceGlass)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = theme.textPrimary
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Back to Family Login",
                style = TextStyle(
                    color = theme.textSecondary,
                    fontSize = 13.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))
                    )
                )
                .border(2.dp, Color(0xFFA78BFA), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.VerifiedUser,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(34.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Enter Member Name",
            style = TextStyle(
                color = theme.textPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Text(
            text = "Set your personal member name for RRR Family clearance",
            style = TextStyle(
                color = theme.textSecondary,
                fontSize = 12.5.sp,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .glassmorphic(
                    shape = RoundedCornerShape(22.dp),
                    backgroundColor = theme.surfaceGlass,
                    borderColor = theme.borderGlass,
                    borderWidth = 1.dp
                )
                .padding(20.dp)
        ) {
            Column {
                OutlinedTextField(
                    value = memberName,
                    onValueChange = {
                        memberName = it
                        if (showError && it.isNotBlank()) showError = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rrr_family_name_input"),
                    label = { Text("Member Name") },
                    placeholder = { Text("Enter your name") },
                    singleLine = true,
                    isError = showError,
                    supportingText = if (showError) {
                        { Text("Name cannot be empty", color = MayaColorError) }
                    } else null,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = theme.accentSecondary
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = theme.accentSecondary,
                        unfocusedBorderColor = theme.borderGlass,
                        focusedTextColor = theme.textPrimary,
                        unfocusedTextColor = theme.textPrimary,
                        cursorColor = theme.accentSecondary
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .clickable { isAgreed = !isAgreed }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                        .testTag("rrr_family_terms_row"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isAgreed,
                        onCheckedChange = { isAgreed = it },
                        modifier = Modifier.testTag("rrr_family_terms_checkbox"),
                        colors = CheckboxDefaults.colors(
                            checkedColor = theme.accentSecondary,
                            uncheckedColor = theme.textSecondary,
                            checkmarkColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "I agree to the Terms & Conditions and Privacy Policy.",
                        style = TextStyle(
                            color = theme.textPrimary,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (memberName.trim().isEmpty()) {
                            showError = true
                        } else if (isAgreed) {
                            onConfirm(memberName.trim())
                        }
                    },
                    enabled = isAgreed && memberName.trim().isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("rrr_family_confirm_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.accentSecondary,
                        disabledContainerColor = theme.accentSecondary.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Enter Maya Connect (RRR Family)",
                            style = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// STEP 5: CEO Panel Login Screen (Strictly "Enter ID" & "Enter Password", Self-Contained Local Input State)
// -------------------------------------------------------------------------
@Composable
private fun CeoPanelLoginScreen(
    errorMessage: String?,
    onLoginSubmit: (ceoId: String, pass: String) -> Unit,
    onBackToFamily: () -> Unit
) {
    val theme = MayaTheme.colors

    // Local self-contained input state
    var ceoId by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackToFamily,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(theme.surfaceGlass)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = theme.textPrimary
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Back to Family Page",
                style = TextStyle(
                    color = theme.textSecondary,
                    fontSize = 13.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, Color(0xFF38BDF8), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.maya_connect_official_logo),
                contentDescription = "Maya Connect Official Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "CEO Panel",
            style = TextStyle(
                color = theme.textPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Text(
            text = "Executive credentials verification for Creator & CEO Rizwan RR",
            style = TextStyle(
                color = theme.textSecondary,
                fontSize = 12.5.sp,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .glassmorphic(
                    shape = RoundedCornerShape(22.dp),
                    backgroundColor = theme.surfaceGlass,
                    borderColor = theme.borderGlass,
                    borderWidth = 1.dp
                )
                .padding(20.dp)
        ) {
            Column {
                OutlinedTextField(
                    value = ceoId,
                    onValueChange = { ceoId = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ceo_panel_id_input"),
                    label = { Text("Enter ID") },
                    placeholder = { Text("Enter ID") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = theme.borderGlass,
                        focusedTextColor = theme.textPrimary,
                        unfocusedTextColor = theme.textPrimary,
                        cursorColor = Color(0xFF38BDF8)
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ceo_panel_password_input"),
                    label = { Text("Enter Password") },
                    placeholder = { Text("Enter Password") },
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Password Visibility",
                                tint = theme.textSecondary
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = theme.borderGlass,
                        focusedTextColor = theme.textPrimary,
                        unfocusedTextColor = theme.textPrimary,
                        cursorColor = Color(0xFF38BDF8)
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        style = TextStyle(
                            color = MayaColorError,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { onLoginSubmit(ceoId, password) },
                    enabled = ceoId.trim().isNotEmpty() && password.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("ceo_panel_login_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0284C7),
                        disabledContainerColor = Color(0xFF0284C7).copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Login to CEO Panel",
                            style = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}

private fun formatLockout(seconds: Long): String {
    return if (seconds >= 3600) {
        val hours = seconds / 3600
        val mins = (seconds % 3600) / 60
        val secs = seconds % 60
        String.format("%02d:%02d:%02d", hours, mins, secs)
    } else if (seconds >= 60) {
        val mins = seconds / 60
        val secs = seconds % 60
        String.format("%02d:%02d:%02d", mins, secs)
    } else {
        "${seconds}s"
    }
}
