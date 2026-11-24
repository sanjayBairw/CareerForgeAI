package com.pynexia.resumebuilder.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import com.pynexia.resumebuilder.data.model.AdditionalItem
import com.pynexia.resumebuilder.data.model.CertificationItem
import com.pynexia.resumebuilder.data.model.CoverLetterEntity
import com.pynexia.resumebuilder.data.model.EducationItem
import com.pynexia.resumebuilder.data.model.ExperienceItem
import com.pynexia.resumebuilder.data.model.JobApplicationEntity
import com.pynexia.resumebuilder.data.model.PersonalInfo
import com.pynexia.resumebuilder.data.model.ProjectItem
import com.pynexia.resumebuilder.data.model.ResumeData
import com.pynexia.resumebuilder.data.model.ResumeEntity
import com.pynexia.resumebuilder.data.model.ResumeVersionEntity
import com.pynexia.resumebuilder.data.model.SkillItem
import com.pynexia.resumebuilder.data.model.UserProfileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ResumeEntity::class,
        ResumeVersionEntity::class,
        CoverLetterEntity::class,
        JobApplicationEntity::class,
        UserProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun resumeDao(): ResumeDao
    abstract fun resumeVersionDao(): ResumeVersionDao
    abstract fun coverLetterDao(): CoverLetterDao
    abstract fun jobApplicationDao(): JobApplicationDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "careerforge_database"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val gson = Gson()

            // 1. User Profile
            val profile = UserProfileEntity(
                id = 1,
                fullName = "",
                email = "",
                phone = "",
                headline = "",
                isLoggedIn = false,
                authProvider = "LOCAL",
                lastSyncTime = System.currentTimeMillis()
            )
            database.userProfileDao().insertOrUpdate(profile)

            // 2. Sample Resume 1: Senior Software Engineer
            val resume1 = ResumeData(
                id = 0,
                title = "Senior Full-Stack Architect",
                targetJobTitle = "Senior Staff Software Engineer",
                targetIndustry = "Technology / Enterprise Cloud",
                templateId = "modern_pro",
                colorTheme = "#2563EB",
                fontFamily = "roboto",
                fontSizeScale = 1.0f,
                layoutColumns = 2,
                lastAtsScore = 92,
                personalInfo = PersonalInfo(
                    fullName = "Candidate Name",
                    headline = "Lead Mobile & Distributed Systems Architect",
                    email = "candidate@example.com",
                    phone = "+1 (555) 019-2834",
                    location = "San Francisco, CA (Remote)",
                    linkedInUrl = "https://linkedin.com/in/candidate",
                    githubUrl = "https://github.com/candidate",
                    portfolioUrl = "https://candidate.dev"
                ),
                summary = "Results-driven Lead Systems Architect with 8+ years architecting high-scale distributed applications and mission-critical Android solutions serving 15M+ active users. Proven track record boosting cloud throughput by 42% and slashing p99 latency by 35% through reactive architectures and modern caching paradigms.",
                educationList = mutableListOf(
                    EducationItem(
                        institution = "Stanford University",
                        degree = "Master of Science",
                        fieldOfStudy = "Computer Science (Distributed Systems)",
                        startDate = "2016",
                        endDate = "2018",
                        grade = "GPA: 3.92 / 4.0",
                        achievements = "Dean's Honor List, Distributed Computing Fellowship"
                    ),
                    EducationItem(
                        institution = "University of California, Berkeley",
                        degree = "Bachelor of Science",
                        fieldOfStudy = "Electrical Engineering & Computer Science",
                        startDate = "2012",
                        endDate = "2016",
                        grade = "Magna Cum Laude (GPA: 3.88)",
                        achievements = "ACM Collegiate Programming Finalist"
                    )
                ),
                experienceList = mutableListOf(
                    ExperienceItem(
                        company = "Apex Cloud Technologies",
                        jobTitle = "Principal Mobile & Cloud Architect",
                        employmentType = "Full-time",
                        location = "San Francisco, CA",
                        startDate = "Jan 2022",
                        endDate = "Present",
                        isCurrent = true,
                        responsibilities = "Lead architecture for cross-platform enterprise SDKs and high-resilience Android microservices deployed across 12 regions.",
                        measurableResults = "Engineered real-time sync engine reducing network battery drain by 28% and driving $2.4M ARR in enterprise upgrades."
                    ),
                    ExperienceItem(
                        company = "HyperScale Media Inc.",
                        jobTitle = "Senior Android Engineer",
                        employmentType = "Full-time",
                        location = "Sunnyvale, CA",
                        startDate = "Mar 2018",
                        endDate = "Dec 2021",
                        isCurrent = false,
                        responsibilities = "Orchestrated the migration of legacy Java monolith to modular Kotlin MVI architecture with Jetpack Room & Coroutines.",
                        measurableResults = "Achieved 99.98% crash-free sessions across 10M installs and improved cold app startup time by 48%."
                    )
                ),
                skillsList = mutableListOf(
                    SkillItem(name = "Kotlin & Java", category = "Technical", level = "Expert", rating = 5),
                    SkillItem(name = "Android SDK & Jetpack", category = "Technical", level = "Expert", rating = 5),
                    SkillItem(name = "System Architecture", category = "Technical", level = "Expert", rating = 5),
                    SkillItem(name = "Room / SQLite / SQL", category = "Technical", level = "Advanced", rating = 4),
                    SkillItem(name = "Coroutines & Reactive Flows", category = "Technical", level = "Expert", rating = 5),
                    SkillItem(name = "CI/CD & Docker", category = "Tools", level = "Advanced", rating = 4),
                    SkillItem(name = "Cross-functional Leadership", category = "Soft", level = "Expert", rating = 5),
                    SkillItem(name = "Agile / Scrum Mentorship", category = "Soft", level = "Expert", rating = 5)
                ),
                projectsList = mutableListOf(
                    ProjectItem(
                        title = "OmniSync Realtime Offline Engine",
                        role = "Creator & Lead Contributor",
                        techStack = "Kotlin, Room, WebSockets, Protobuf",
                        startDate = "2023",
                        endDate = "2024",
                        description = "High-performance two-way sync engine providing sub-15ms conflict resolution for offline-first mobile applications.",
                        liveUrl = "https://omnisync.dev",
                        repoUrl = "https://github.com/alex-dev/omnisync"
                    )
                ),
                certificationsList = mutableListOf(
                    CertificationItem(
                        name = "Google Certified Associate Android Developer",
                        issuingOrganization = "Google Developers",
                        issueDate = "2021",
                        credentialId = "GC-AAD-98421"
                    ),
                    CertificationItem(
                        name = "AWS Certified Solutions Architect – Professional",
                        issuingOrganization = "Amazon Web Services",
                        issueDate = "2023",
                        credentialId = "AWS-SAP-7712"
                    )
                ),
                additionalList = mutableListOf(
                    AdditionalItem(
                        category = "Award",
                        title = "Global Innovation Award",
                        subtitle = "Apex Cloud Technologies",
                        date = "2023",
                        description = "Recognized for designing zero-downtime offline migration pipeline."
                    )
                ),
                qrCodeContent = "https://linkedin.com/in/candidate"
            )

            // 3. Sample Resume 2: Product & Growth Lead
            val resume2 = ResumeData(
                id = 0,
                title = "Senior Product Growth Strategist",
                targetJobTitle = "Director of Product Growth",
                targetIndustry = "SaaS / Fintech",
                templateId = "executive",
                colorTheme = "#059669",
                fontFamily = "serif",
                fontSizeScale = 1.0f,
                layoutColumns = 1,
                lastAtsScore = 87,
                personalInfo = PersonalInfo(
                    fullName = "Sophia Martinez",
                    headline = "Director of Product & Growth Marketing",
                    email = "sophia.martinez@execgrowth.com",
                    phone = "+1 (212) 555-0199",
                    location = "New York, NY",
                    linkedInUrl = "https://linkedin.com/in/sophiamartinez-growth",
                    portfolioUrl = "https://sophiagrowth.io"
                ),
                summary = "Dynamic Growth Marketing Executive with 7+ years directing product-led growth strategies, user acquisition funnels, and retention flywheels for high-velocity B2B SaaS companies. Scaled annualized ARR from $4M to $28M while reducing customer acquisition costs (CAC) by 34%.",
                educationList = mutableListOf(
                    EducationItem(
                        institution = "Columbia Business School",
                        degree = "Master of Business Administration (MBA)",
                        fieldOfStudy = "Strategic Management & Analytics",
                        startDate = "2017",
                        endDate = "2019",
                        grade = "GPA: 3.85"
                    )
                ),
                experienceList = mutableListOf(
                    ExperienceItem(
                        company = "Veloce Financial SaaS",
                        jobTitle = "Head of Product Growth",
                        employmentType = "Full-time",
                        location = "New York, NY",
                        startDate = "2021",
                        endDate = "Present",
                        isCurrent = true,
                        responsibilities = "Lead a 14-person cross-functional team of growth product managers, data scientists, and performance engineers.",
                        measurableResults = "Increased onboarding activation rate by 52% and drove 140% Net Dollar Retention (NDR)."
                    )
                ),
                skillsList = mutableListOf(
                    SkillItem(name = "Product-Led Growth (PLG)", category = "Technical", level = "Expert", rating = 5),
                    SkillItem(name = "Funnel Analytics & Mixpanel", category = "Tools", level = "Expert", rating = 5),
                    SkillItem(name = "A/B Testing & Multivariate", category = "Technical", level = "Expert", rating = 5),
                    SkillItem(name = "Executive Presentation", category = "Soft", level = "Expert", rating = 5)
                ),
                qrCodeContent = "https://linkedin.com/in/sophiamartinez-growth"
            )

            val resumeId1 = database.resumeDao().insert(resume1.toEntity(gson))
            val resumeId2 = database.resumeDao().insert(resume2.toEntity(gson))

            // 4. Sample Cover Letter
            val coverLetter = CoverLetterEntity(
                id = 0,
                resumeId = resumeId1,
                title = "Apex Solutions - Staff Architect Cover Letter",
                recipientName = "Eleanor Vance, VP of Engineering",
                companyName = "Apex Solutions Global",
                jobTitle = "Staff Android Systems Architect",
                date = "November 2026",
                salutation = "Dear Ms. Vance and the Apex Engineering Team,",
                bodyText = "I am writing to express my strong interest in the Staff Android Systems Architect position at Apex Solutions Global. Having followed Apex's groundbreaking initiatives in edge computing and distributed mobile synchronization, I was thrilled to see this opening for someone with deep expertise in scalable mobile architectures.\n\nOver the past eight years, I have led teams delivering high-volume Android platforms serving tens of millions of daily active users. At Apex Cloud Technologies, I spearheaded our reactive caching engine, which reduced p99 latency by 35% while cutting battery consumption across our entire mobile footprint. My approach couples rigorous code craft with business acumen, ensuring robust architectures that accelerate product velocity.\n\nI would welcome the opportunity to discuss how my technical leadership and background in high-concurrency systems can help Apex exceed its 2027 technical objectives. Thank you for your time and consideration.",
                signOff = "Sincerely,",
                senderName = "Candidate Name",
                senderContact = "candidate@example.com | +1 (555) 019-2834",
                templateId = "modern"
            )
            database.coverLetterDao().insert(coverLetter)

            // 5. Sample Job Application
            val application = JobApplicationEntity(
                id = 0,
                companyName = "Apex Solutions Global",
                jobTitle = "Staff Android Systems Architect",
                location = "San Francisco, CA / Remote",
                salaryRange = "$195,000 - $230,000",
                jobType = "Full-time",
                status = "INTERVIEW",
                appliedDate = System.currentTimeMillis() - (5 * 24 * 60 * 60 * 1000L),
                interviewDate = System.currentTimeMillis() + (2 * 24 * 60 * 60 * 1000L),
                jobDescription = "Seeking a Staff Architect with deep Kotlin, Room, Coroutines, and distributed mobile system experience.",
                notes = "Technical interview scheduled with VP of Engineering Eleanor Vance. Prepare architecture diagrams for offline sync.",
                contactPerson = "Eleanor Vance",
                contactEmail = "e.vance@apexsolutions.io",
                linkedResumeId = resumeId1
            )
            val application2 = JobApplicationEntity(
                id = 0,
                companyName = "Starlight Media & Streaming",
                jobTitle = "Lead Mobile Software Engineer",
                location = "San Jose, CA",
                salaryRange = "$180,000 - $210,000",
                jobType = "Full-time",
                status = "APPLIED",
                appliedDate = System.currentTimeMillis() - (8 * 24 * 60 * 60 * 1000L),
                interviewDate = null,
                jobDescription = "High-scale video streaming application engineering.",
                notes = "Referral from senior engineer on the video playback team.",
                contactPerson = "Marcus Sterling",
                contactEmail = "msterling@starlight.io",
                linkedResumeId = resumeId1
            )
            database.jobApplicationDao().insert(application)
            database.jobApplicationDao().insert(application2)
        }
    }
}
