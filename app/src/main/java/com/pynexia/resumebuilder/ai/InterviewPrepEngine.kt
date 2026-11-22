package com.pynexia.resumebuilder.ai

import com.pynexia.resumebuilder.data.model.ResumeData

data class InterviewQuestion(
    val category: String, // Behavioral, Technical, System Design, Leadership
    val question: String,
    val sampleAnswerFramework: String,
    val tips: String
)

class InterviewPrepEngine {

    fun generateQuestions(resume: ResumeData): List<InterviewQuestion> {
        val questions = mutableListOf<InterviewQuestion>()
        val role = resume.targetJobTitle.ifBlank { "Software Engineer" }
        val topSkills = resume.skillsList.take(3).map { it.name }.joinToString(", ")

        // 1. Behavioral STAR
        questions.add(
            InterviewQuestion(
                category = "Behavioral (STAR Method)",
                question = "Describe a situation where you encountered a critical technical obstacle or project delay. How did you resolve it?",
                sampleAnswerFramework = "Situation: Set the scene in your role at ${resume.experienceList.firstOrNull()?.company ?: "your recent company"}.\nTask: What was the specific milestone at risk?\nAction: Detail the technical decisions, code reviews, and mitigation strategies you executed.\nResult: Conclude with metrics (e.g. delivered on time, avoided downtime).",
                tips = "Focus on your individual contribution while acknowledging collaborative teamwork."
            )
        )

        // 2. Technical Specialization
        questions.add(
            InterviewQuestion(
                category = "Technical Architecture",
                question = "How do you approach architecture and modularization for scale when working with $topSkills?",
                sampleAnswerFramework = "Explain separation of concerns, clean boundaries, dependency inversion, and caching strategies. Reference real implementations from your projects.",
                tips = "Mention latency trade-offs, thread safety, and unit testability."
            )
        )

        // 3. Leadership & Cross-functional
        questions.add(
            InterviewQuestion(
                category = "Leadership & Influence",
                question = "How do you align differing engineering perspectives between product managers, designers, and developers?",
                sampleAnswerFramework = "Discuss data-driven decision making, MVP scoping, user feedback loops, and empathetic communication.",
                tips = "Demonstrate that you prioritize user and business outcomes over personal ego."
            )
        )

        // 4. Measurable Accomplishment
        val topExp = resume.experienceList.firstOrNull()
        val expDesc = topExp?.measurableResults?.ifBlank { "performance optimizations" } ?: "your core responsibilities"
        questions.add(
            InterviewQuestion(
                category = "Experience Deep-Dive",
                question = "In your resume, you highlighted: \"$expDesc\". Walk us through how you achieved this outcome step-by-step.",
                sampleAnswerFramework = "Break down baseline measurement -> hypothesis -> technical execution -> post-launch telemetry and metrics verification.",
                tips = "Be ready to explain the numbers and tooling used to measure success."
            )
        )

        // 5. System Resilience & Quality
        questions.add(
            InterviewQuestion(
                category = "Quality & Reliability",
                question = "How do you ensure 99.9%+ reliability, crash-free rates, and zero regressions in continuous deployment?",
                sampleAnswerFramework = "Discuss automated CI/CD pipelines, unit/integration testing pyramids, staged rollouts, and proactive crash telemetry.",
                tips = "Highlight preventative architecture over reactive firefighting."
            )
        )

        return questions
    }
}
