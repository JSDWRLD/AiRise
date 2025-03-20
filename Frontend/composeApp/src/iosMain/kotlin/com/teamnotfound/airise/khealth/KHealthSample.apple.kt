package com.teamnotfound.airise.khealth

import com.khealth.KHealth

private val kHealth = KHealth()
fun sampleCheckAllPerms() = sampleCheckAllPerms(kHealth)
fun sampleRequestAllPerms() = sampleRequestAllPerms(kHealth)
fun sampleWriteData() = sampleWriteData(kHealth)
fun sampleReadData() = sampleReadData(kHealth)