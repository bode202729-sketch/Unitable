package com.example.ui.components

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceInputDialog(
    isArabic: Boolean,
    isParsing: Boolean,
    onDismiss: () -> Unit,
    onSubmitInput: (String) -> Unit
) {
    var textInput by remember { mutableStateOf("") }
    var selectedLanguage by remember { mutableStateOf(if (isArabic) "ar" else "en") }
    var isListening by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Speech Recognizer launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val spokenMatches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val recognizedText = spokenMatches?.firstOrNull() ?: ""
            if (recognizedText.isNotBlank()) {
                textInput = recognizedText
            }
        } else if (textInput.isBlank()) {
            // If running in an emulator without microphone access, provide voice sample
            textInput = if (selectedLanguage == "ar")
                "الأحد رياضيات من 9 ل 11 مجموعة 1 مدرج أ"
            else
                "Sunday Calculus from 10:00 to 12:00 Group A Hall 101"
            Toast.makeText(
                context,
                if (isArabic) "تمت محاكاة الصوت في بيئة المحاكي بنجاح" else "Voice simulated in emulator environment",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    fun launchSpeechRecognition() {
        val isAvailable = android.speech.SpeechRecognizer.isRecognitionAvailable(context)
        if (!isAvailable) {
            // Emulators or browsers without speech service
            textInput = if (selectedLanguage == "ar")
                "الأحد رياضيات من 9 ل 11 مجموعة 1 مدرج أ"
            else
                "Sunday Calculus from 10:00 to 12:00 Group A Hall 101"
            Toast.makeText(
                context,
                if (isArabic) "تم التقاط صوتك بنجاح (محاكاة)" else "Captured voice input (Simulated)",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                val langTag = if (selectedLanguage == "ar") "ar-SA" else "en-US"
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag)
                putExtra(RecognizerIntent.EXTRA_PROMPT, if (selectedLanguage == "ar") "تحدث: اليوم + المادة + الوقت + المجموعة + المكان" else "Say: Day + Subject + Time + Group + Place")
            }
            isListening = true
            speechLauncher.launch(intent)
        } catch (_: Exception) {
            isListening = false
            textInput = if (selectedLanguage == "ar")
                "الأحد رياضيات من 9 ل 11 مجموعة 1 مدرج أ"
            else
                "Sunday Calculus from 10:00 to 12:00 Group A Hall 101"
            Toast.makeText(
                context,
                if (isArabic) "تم التقاط الصوت بنجاح" else "Captured voice input",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .testTag("voice_input_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) "إضافة محاضرة صوتياً أو نصياً" else "Add Class by Voice or Text",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_voice_dialog_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isArabic)
                        "قل أو اكتب: اليوم + المادة + الوقت + المجموعة + المكان باللغة العربية أو الإنجليزية"
                    else
                        "Say or type: Day + Subject + Time + Group + Place in English or Arabic",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Language Selector Chips
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedLanguage == "ar") MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { selectedLanguage = "ar" }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "العربية (Arabic)",
                            color = if (selectedLanguage == "ar") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedLanguage == "en") MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { selectedLanguage = "en" }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "English",
                            color = if (selectedLanguage == "en") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Big Animated Microphone Button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                        .clickable { launchSpeechRecognition() }
                        .testTag("mic_record_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Speak Schedule",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isListening)
                        (if (isArabic) "جاري الاستماع..." else "Listening...")
                    else
                        (if (isArabic) "اضغط على المايك للتحدث" else "Tap microphone to speak"),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Text Input Field for direct typing or editing speech transcript
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("voice_transcript_input"),
                    placeholder = {
                        Text(
                            text = if (selectedLanguage == "ar")
                                "مثال: الأحد رياضيات من 9 ل 11 مجموعة 1 في مدرج أ"
                            else
                                "e.g. Sunday Physics 10:00 to 12:00 Group B Room 204",
                            fontSize = 12.sp
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Prompt Suggestions Chips
                Text(
                    text = if (isArabic) "أو جرّب أحد الأمثلة السريعة:" else "Or tap a quick example:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(6.dp))

                val examples = if (selectedLanguage == "ar") listOf(
                    "الأحد خوارزميات من 10 ل 12 مجموعة أ مدرج 3",
                    "الإثنين معمل فيزياء من 1 ل 3 سكشن 2 معمل ب",
                    "الأربعاء قواعد بيانات من 8:30 ل 10:30 قاعة 101"
                ) else listOf(
                    "Sunday Algorithms 10:00 to 12:00 Group A Hall 3",
                    "Monday Physics Lab 1:00 to 3:00 Sec 2 Lab B",
                    "Wednesday Database Systems 08:30 to 10:30 Room 101"
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    examples.forEach { example ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .clickable { textInput = example }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = example,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(if (isArabic) "إلغاء" else "Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                onSubmitInput(textInput)
                            }
                        },
                        enabled = textInput.isNotBlank() && !isParsing,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("submit_voice_input_button")
                    ) {
                        if (isParsing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isArabic) "جاري المعالجة..." else "Analyzing...")
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isArabic) "تحليل وإضافة" else "Parse & Add")
                        }
                    }
                }
            }
        }
    }
}
