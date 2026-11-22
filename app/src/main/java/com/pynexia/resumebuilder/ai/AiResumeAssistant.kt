package com.pynexia.resumebuilder.ai

import android.content.Context
import com.pynexia.resumebuilder.data.model.ResumeData
import com.pynexia.resumebuilder.utils.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class AiResumeAssistant(private val context: Context) {
    private val prefManager = PreferenceManager(context)

    suspend fun generateSummary(
        jobTitle: String,
        industry: String,
        yearsOfExp: String,
        keySkills: List<String>,
        tone: String = "Impactful"
    ): String = withContext(Dispatchers.IO) {
        val prompt = "Write a compelling $tone professional summary for a $jobTitle in $industry with $yearsOfExp of experience and skills: ${keySkills.joinToString(", ")}."
        val remoteResult = callRemoteAiIfConfigured(prompt)
        if (!remoteResult.isNullOrBlank()) {
            return@withContext remoteResult
        }
        // Fallback: Smart Local Generation Engine
        return@withContext generateLocalSummary(jobTitle, industry, yearsOfExp, keySkills, tone)
    }

    suspend fun enhanceBulletPoint(rawInput: String, jobTitle: String = ""): List<String> = withContext(Dispatchers.IO) {
        val prompt = "Rewrite the following resume bullet point using the STAR method with strong action verbs and quantified impact: \"$rawInput\""
        val remoteResult = callRemoteAiIfConfigured(prompt)
        if (!remoteResult.isNullOrBlank()) {
            return@withContext remoteResult.lines().filter { it.isNotBlank() }
        }
        return@withContext generateLocalEnhancedBullets(rawInput, jobTitle)
    }

    suspend fun polishSentence(sentence: String): String = withContext(Dispatchers.IO) {
        val prompt = "Rewrite this resume sentence to be concise, authoritative, and professionally polished: \"$sentence\""
        val remoteResult = callRemoteAiIfConfigured(prompt)
        if (!remoteResult.isNullOrBlank()) {
            return@withContext remoteResult.trim()
        }
        return@withContext polishLocalSentence(sentence)
    }

    fun recommendSkills(jobTitle: String): List<String> {
        val titleLower = jobTitle.lowercase()
        return when {
            titleLower.contains("android") || titleLower.contains("mobile") || titleLower.contains("ios") -> {
                listOf("Kotlin", "Jetpack Compose", "Coroutines & Flow", "Room Database", "Retrofit & OkHttp", "MVVM / MVI", "Clean Architecture", "Dagger Hilt", "JUnit & Espresso", "Material Design 3", "Git & CI/CD", "Performance Optimization")
            }
            titleLower.contains("full stack") || titleLower.contains("backend") || titleLower.contains("software") || titleLower.contains("developer") -> {
                listOf("Java / Kotlin", "Python / Go", "RESTful APIs & GraphQL", "PostgreSQL / MongoDB", "Docker & Kubernetes", "Microservices", "Redis Caching", "AWS / GCP Cloud", "Distributed Systems", "Unit & Integration Testing", "System Design")
            }
            titleLower.contains("data") || titleLower.contains("machine learning") || titleLower.contains("ai") -> {
                listOf("Python", "SQL", "Pandas & NumPy", "TensorFlow / PyTorch", "Scikit-Learn", "Data Pipelines & ETL", "Tableau / PowerBI", "Statistical Modeling", "BigQuery / Snowflake", "Cloud ML Deployments", "A/B Testing")
            }
            titleLower.contains("product") || titleLower.contains("pm") -> {
                listOf("Product Strategy & Vision", "Agile & Scrum Roadmap", "User Journey Mapping", "KPI & OKR Tracking", "Data Analytics & Mixpanel", "A/B Testing", "Cross-Functional Leadership", "Stakeholder Alignment", "Competitive Benchmarking")
            }
            titleLower.contains("market") || titleLower.contains("growth") -> {
                listOf("Growth Marketing", "Funnel Optimization", "SEO & SEM", "Customer Acquisition (CAC)", "Retention & LTV", "HubSpot / Marketo", "Google Analytics 4", "Email Campaign Automation", "Content Strategy", "Conversion Rate Optimization")
            }
            titleLower.contains("design") || titleLower.contains("ux") || titleLower.contains("ui") -> {
                listOf("Figma & Prototyping", "Design Systems", "User Research & Usability Testing", "Wireframing", "Interaction Design", "Responsive Layouts", "Accessibility (WCAG)", "Design Handoff", "Information Architecture")
            }
            else -> {
                listOf("Project Management", "Strategic Planning", "Data-Driven Decision Making", "Cross-Functional Collaboration", "Process Optimization", "Client Relationship Management", "Budgeting & Resource Allocation", "Continuous Improvement")
            }
        }
    }

    fun generateHeadlines(name: String, role: String, specialty: String): List<String> {
        return listOf(
            "$role | Specializing in $specialty & Scalable Architectures",
            "Results-Driven $role | Delivering High-Impact Solutions & Innovation",
            "Senior $role | $specialty Specialist | Performance & Growth Driver",
            "$role | Transforming Complex Business Challenges into Elegant Systems"
        )
    }

    fun detectMissingInfo(resume: ResumeData): List<String> {
        val issues = mutableListOf<String>()
        if (resume.personalInfo.email.isBlank()) issues.add("Missing email address in Contact section.")
        if (resume.personalInfo.phone.isBlank()) issues.add("Missing phone number for recruiter contact.")
        if (resume.personalInfo.linkedInUrl.isBlank()) issues.add("Adding a LinkedIn profile URL boosts ATS trust score.")
        if (resume.summary.isBlank() || resume.summary.length < 50) issues.add("Professional Summary is empty or too short.")
        if (resume.experienceList.isEmpty()) issues.add("Work Experience section is empty.")
        if (resume.educationList.isEmpty()) issues.add("Education section is missing.")
        if (resume.skillsList.size < 5) issues.add("Include at least 6-8 relevant skills for keyword matching.")
        return issues
    }

    private fun generateLocalSummary(
        jobTitle: String,
        industry: String,
        yearsOfExp: String,
        keySkills: List<String>,
        tone: String
    ): String {
        val skillPhrase = if (keySkills.isNotEmpty()) "proficient in ${keySkills.take(4).joinToString(", ")}" else "specializing in scalable architectures"
        return when (tone) {
            "Executive" -> {
                "Strategic, outcome-driven $jobTitle with $yearsOfExp of leadership within $industry. Proven expertise steering cross-functional teams, executing high-stakes transformation initiatives, and maximizing enterprise ROI. Recognized for combining analytical rigor with strategic vision to consistently outpace growth milestones."
            }
            "Technical" -> {
                "High-impact $jobTitle with $yearsOfExp designing and deploying mission-critical systems across $industry. Deep hands-on experience $skillPhrase. Track record of optimizing high-throughput workflows, minimizing latency, and implementing robust CI/CD engineering standards."
            }
            "Fresh Graduate" -> {
                "Ambitious, detail-oriented professional with comprehensive foundational knowledge in $industry and $skillPhrase. Eager to contribute academic excellence, problem-solving abilities, and strong technical curiosity to deliver high-quality outcomes within a collaborative team environment."
            }
            else -> {
                "Results-oriented $jobTitle with $yearsOfExp delivering high-performance solutions in $industry. Skilled in $skillPhrase, with a proven ability to accelerate product velocity, eliminate bottlenecks, and translate complex technical requirements into measurable business impact."
            }
        }
    }

    private fun generateLocalEnhancedBullets(rawInput: String, jobTitle: String): List<String> {
        val cleaned = rawInput.replace(Regex("^[•\\-*\\s]+"), "").trim()
        val baseVerb = if (cleaned.isNotEmpty()) cleaned else "Developed and deployed key functional modules"
        return listOf(
            "Architected and deployed $baseVerb, resulting in a 34% reduction in processing latency and zero critical outages.",
            "Spearheaded $baseVerb across cross-functional teams, driving measurable delivery efficiency improvements of 25%.",
            "Optimized and streamlined $baseVerb, leading to improved user engagement and an estimated $120K annual cost savings."
        )
    }

    private fun polishLocalSentence(sentence: String): String {
        var result = sentence.trim()
        result = result.replace(Regex("(?i)\\bresponsible for\\b"), "Led and executed")
        result = result.replace(Regex("(?i)\\bworked on\\b"), "Engineered and delivered")
        result = result.replace(Regex("(?i)\\bhelped with\\b"), "Collaborated cross-functionally to achieve")
        result = result.replace(Regex("(?i)\\bgood at\\b"), "Proven expertise in")
        result = result.replace(Regex("(?i)\\bmade\\b"), "Architected and implemented")
        return result
    }

    private fun callRemoteAiIfConfigured(prompt: String): String? {
        val apiKey = prefManager.aiApiKey.trim()
        if (apiKey.isBlank()) return null

        return try {
            val endpoint = prefManager.aiEndpointUrl.trim().let {
                if (it.endsWith("/")) "${it}chat/completions" else "$it/chat/completions"
            }
            val url = URL(endpoint)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Authorization", "Bearer $apiKey")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.connectTimeout = 8000
            conn.readTimeout = 12000
            conn.doOutput = true

            val payload = JSONObject().apply {
                put("model", prefManager.aiModelName)
                put("temperature", 0.7)
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", "You are an elite executive resume consultant. Provide direct, highly polished resume content without conversational filler.")
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                })
            }

            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val responseStr = reader.readText()
                val jsonResp = JSONObject(responseStr)
                val choices = jsonResp.getJSONArray("choices")
                if (choices.length() > 0) {
                    val msg = choices.getJSONObject(0).getJSONObject("message")
                    return msg.getString("content")
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }
}
