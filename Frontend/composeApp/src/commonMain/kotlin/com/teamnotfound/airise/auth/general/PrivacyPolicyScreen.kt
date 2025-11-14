package com.teamnotfound.airise.auth.general

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import com.teamnotfound.airise.util.BgBlack
import com.teamnotfound.airise.util.DeepBlue
import com.teamnotfound.airise.util.Orange
import com.teamnotfound.airise.util.Silver
import com.teamnotfound.airise.util.White

@Composable
fun PrivacyPolicyScreen(
    onBackClick: () -> Unit
) {
    val scroll = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBlack)
    ) {
        Column(Modifier.fillMaxSize()) {
            AuthHeader(
                title = "Privacy Policy",
                subtitle = "Last updated: October 31, 2025",
                onBackClick = onBackClick
            )

            Spacer(Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scroll),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AuthCard {
                    // Title
                    Text(
                        "AiRise Privacy Policy",
                        color = White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Thank you for using AiRise. This Privacy Policy explains what data we collect, how we use it to power your fitness experience, and the choices you have.",
                        color = Silver,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    SectionTitle("1. What We Collect")
                    Bullet("Account & Contact Info: email address, display name, and authentication identifiers.")
                    Bullet("Profile & Goals (optional): age range, height/weight ranges, goals, training preferences.")
                    Bullet("App & Workout Activity: plan selections, workout completions, sets/reps/time, streaks, and app interactions.")
                    Bullet("AI Prompts & Outputs: questions you ask the coach and the model’s responses.")
                    Bullet("Device & Usage Data: device type, app version, crash logs, and diagnostics.")

                    SectionTitle("2. How We Use Your Data")
                    Bullet("Personalize training plans, coaching tips, and content.")
                    Bullet("Power AI features including chat, recommendations, and form guidance.")
                    Bullet("Improve app performance, safety, and reliability.")
                    Bullet("Communicate updates, security alerts, and support responses.")
                    Bullet("Comply with legal obligations and enforce our terms.")

                    SectionTitle("3. AI Processing & Third Parties")
                    Text(
                        "To provide AI features, we may send relevant data (like your prompts, workout context, and preferences) to trusted AI providers for processing, such as the Google Gemini API. We take steps to minimize personally identifiable information in these requests when possible.",
                        color = Silver, fontSize = 14.sp, lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Bullet("We restrict providers’ use of your data to performing our requested services.")
                    Bullet("We do not sell personal data.")
                    Bullet("Aggregated/ de-identified analytics may be used for product insights.")

                    SectionTitle("4. Data Sharing")
                    Bullet("Service Providers: hosting, analytics, crash reporting, and AI processing vendors.")
                    Bullet("Legal & Safety: when required by law or to protect users and our services.")
                    Bullet("Business Transfers: as part of a merger, acquisition, or asset sale (you’ll be notified of any material changes).")

                    SectionTitle("5. Your Controls & Rights")
                    Bullet("Access & Update: edit profile info inside the app.")
                    Bullet("Export: request a copy of your data via support.")
                    Bullet("Delete: request deletion of your account and associated personal data, subject to legal retention requirements.")
                    Bullet("Opt-Out: you can limit certain analytics and marketing communications.")
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "We will honor applicable rights under laws like GDPR/CCPA where relevant.",
                        color = Silver, fontSize = 14.sp, lineHeight = 20.sp
                    )

                    SectionTitle("6. Data Retention")
                    Text(
                        "We keep personal data only as long as needed for the purposes in this policy: usually for the life of your account, or a reasonable period thereafter to comply with law, resolve disputes, and maintain security. AI prompts and outputs may be retained to improve your coaching history unless you request deletion.",
                        color = Silver, fontSize = 14.sp, lineHeight = 20.sp
                    )

                    SectionTitle("7. Children’s Privacy")
                    Text(
                        "AiRise is not intended for children under 13 (or older, if required by local law). We do not knowingly collect personal data from children.",
                        color = Silver, fontSize = 14.sp, lineHeight = 20.sp
                    )

                    SectionTitle("8. Security")
                    Text(
                        "We use administrative, technical, and organizational measures to protect your data. No method of transmission or storage is 100% secure; please keep your credentials safe.",
                        color = Silver, fontSize = 14.sp, lineHeight = 20.sp
                    )

                    SectionTitle("9. Changes to This Policy")
                    Text(
                        "We may update this policy to reflect changes to our practices. We will post updates in-app and revise the “Last updated” date above. Material changes will be communicated more prominently.",
                        color = Silver, fontSize = 14.sp, lineHeight = 20.sp
                    )

                    SectionTitle("10. Contact Us")
                    Text(
                        "Questions, feedback, or privacy requests? Email support@airise.app or write to:",
                        color = Silver, fontSize = 14.sp, lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "AiRise — Attn: Privacy Team, Sacramento, CA, USA",
                        color = Silver, fontSize = 14.sp, lineHeight = 20.sp
                    )

                    // Prototype / Non-commercial notice
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Prototype notice: This app is provided for testing and demonstration purposes only and is not intended for commercial use.",
                        color = Silver, fontSize = 12.sp, lineHeight = 18.sp
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun AuthErrorBanner(
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DeepBlue.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, DeepBlue),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Orange)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = message,
                color = White,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

fun friendlyAuthError(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    val s = raw.lowercase()
    return when {
        "user" in s && "not" in s && "found" in s -> "We couldn’t find an account with that email."
        "wrong password" in s || "invalid password" in s -> "That password doesn’t look right."
        "invalid email" in s || "badly formatted" in s -> "Please enter a valid email address."
        "too many requests" in s || "blocked" in s -> "Too many attempts. Please try again in a moment."
        "network" in s || "timeout" in s || "unreachable" in s -> "Network problem. Check your connection and try again."
        "credential" in s || "token" in s -> "There was a sign-in issue. Please try again."
        else -> "Couldn’t sign you in. Check your email and password and try again."
    }
}

@Composable private fun SectionTitle(text: String) {
    Spacer(Modifier.height(14.dp))
    Text(text, color = White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(6.dp))
}

@Composable private fun Bullet(text: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text("•", color = Orange, fontSize = 14.sp, modifier = Modifier.padding(end = 6.dp))
        Text(text, color = Silver, fontSize = 14.sp, lineHeight = 20.sp, modifier = Modifier.weight(1f))
    }
}
