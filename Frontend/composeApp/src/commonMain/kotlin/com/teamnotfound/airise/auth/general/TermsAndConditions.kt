package com.teamnotfound.airise.auth.general

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamnotfound.airise.util.BgBlack
import com.teamnotfound.airise.util.Orange
import com.teamnotfound.airise.util.Silver
import com.teamnotfound.airise.util.White

/**
 * NOTE: Template T&C for product description only; have counsel review before release.
 */
@Composable
fun TermsOfUseScreen(
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
                title = "Terms & Conditions",
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
                    Title("AiRise Terms of Use")

                    SectionTitle("1. Acceptance of Terms")
                    Body("By creating an account or using AiRise (the “Service”), you agree to these Terms and our Privacy Policy.")

                    SectionTitle("2. Eligibility")
                    Body("You must be at least 13 years old (or older if required by local law). If you’re under the age of majority, you must have a parent/guardian’s consent.")

                    SectionTitle("3. Accounts & Security")
                    Bullet("Provide accurate registration information and keep it updated.")
                    Bullet("You’re responsible for safeguarding your account and for all activity under it.")
                    Bullet("Notify us immediately of unauthorized use or security incidents.")

                    SectionTitle("4. Acceptable Use")
                    Bullet("Don’t misuse the Service (spam, abuse, reverse engineering, or unlawful content).")
                    Bullet("Don’t upload content that is illegal, infringing, or harmful.")
                    Bullet("Follow community guidelines and all applicable laws.")

                    SectionTitle("5. AI Features & External Providers")
                    Body("To deliver coaching and recommendations, we may send prompts and relevant context to trusted AI providers (e.g., Google Gemini API). We minimize personal data when possible and restrict providers’ use to the requested services. AI outputs may be imperfect—use discretion and verify advice.")

                    SectionTitle("6. Health & Safety Notice (Not Medical Advice)")
                    Body("AiRise provides general fitness information and is not a medical service. Consult a qualified professional before starting any exercise program, especially if you have injuries, conditions, or concerns. Stop activity if you feel pain, dizziness, or discomfort.")

                    SectionTitle("7. User Content & License")
                    Bullet("You retain ownership of content you submit (e.g., notes, custom plans).")
                    Bullet("You grant us a worldwide, non-exclusive license to host, process, and display that content solely to operate and improve the Service.")
                    Bullet("You represent you have rights to the content you submit.")

                    SectionTitle("8. Intellectual Property")
                    Body("The Service and all related materials are owned by AiRise or its licensors and are protected by IP laws. Except as allowed by these Terms, you may not copy, modify, or distribute our content.")

                    SectionTitle("9. Termination")
                    Body("We may suspend or terminate accounts for violations of these Terms or risk to users/services. You may stop using the Service at any time. Certain sections survive termination (e.g., IP, disclaimers, limitation of liability).")

                    SectionTitle("10. Disclaimers")
                    Bullet("THE SERVICE IS PROVIDED “AS IS” AND “AS AVAILABLE.”")
                    Bullet("WE DISCLAIM WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT TO THE MAXIMUM EXTENT PERMITTED BY LAW.")

                    SectionTitle("11. Limitation of Liability")
                    Body("To the maximum extent permitted by law, AiRise and its affiliates won’t be liable for indirect, incidental, special, consequential, or punitive damages, or any loss of data, profits, or revenues, arising from or related to your use of the Service.")

                    SectionTitle("12. Governing Law; Dispute Resolution")
                    Body("These Terms are governed by the laws of your primary operating jurisdiction (to be specified). Disputes will be resolved in the courts or arbitration forum specified by AiRise (to be specified).")

                    SectionTitle("13. Changes to Terms")
                    Body("We may update these Terms from time to time. Updates will be posted in-app with a revised “Last updated” date. Material changes will be communicated more prominently.")

                    SectionTitle("14. Contact")
                    Body("Questions? Email support@airise.app")
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// --- Small text helpers matching your Privacy screen style ---

@Composable private fun Title(text: String) {
    Text(text, color = White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(8.dp))
}

@Composable private fun SectionTitle(text: String) {
    Spacer(Modifier.height(14.dp))
    Text(text, color = White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(6.dp))
}

@Composable private fun Body(text: String) {
    Text(text, color = Silver, fontSize = 14.sp, lineHeight = 20.sp)
}

@Composable private fun Bullet(text: String) {
    Row(Modifier.fillMaxWidth()) {
        Text("•", color = Orange, fontSize = 14.sp, modifier = Modifier.padding(end = 6.dp))
        Text(text, color = Silver, fontSize = 14.sp, lineHeight = 20.sp, modifier = Modifier.weight(1f))
    }
}
