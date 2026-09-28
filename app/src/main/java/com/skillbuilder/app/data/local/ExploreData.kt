package com.skillbuilder.app.data.local

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.BusinessCenter
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.ui.graphics.vector.ImageVector
import com.skillbuilder.app.domain.model.MentorVideo

data class ExploreTopicItem(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val categoryFilter: String
)

data class ExploreCourseItem(
    val id: String,
    val title: String,
    val organization: String,
    val type: String = "Course",
    val rating: Float,
    val reviewCount: String,
    val thumbnailUrl: String,
    val orgBadgeText: String? = null,
    val orgBadgeColor: Long = 0xFFFFFFFF,
    val orgTextColor: Long = 0xFF2B2B2B,
    val mentorVideo: MentorVideo
)

data class DegreeProgramItem(
    val id: String,
    val title: String,
    val university: String,
    val degreeType: String = "Degree",
    val thumbnailUrl: String,
    val badgeText: String,
    val badgeBgColor: Long = 0xFFFFFFFF,
    val badgeTextColor: Long = 0xFF2B2B2B,
    val mentorVideo: MentorVideo
)

data class CertificateItem(
    val id: String,
    val title: String,
    val provider: String,
    val type: String = "Professional Certificate",
    val rating: Float,
    val reviewCount: String,
    val thumbnailUrl: String,
    val badgeText: String,
    val badgeBgColor: Long = 0xFFFFFFFF,
    val badgeTextColor: Long = 0xFF2B2B2B,
    val mentorVideo: MentorVideo
)

object ExploreData {

    val topics = listOf(
        ExploreTopicItem("t_arts", "Arts and Humanities", Icons.Rounded.Palette, "Design & Art"),
        ExploreTopicItem("t_biz", "Business", Icons.Rounded.BusinessCenter, "Business"),
        ExploreTopicItem("t_cs", "Computer Science", Icons.Rounded.Terminal, "Tech & Coding"),
        ExploreTopicItem("t_data", "Data Science", Icons.Rounded.Analytics, "Tech & Coding"),
        ExploreTopicItem("t_lang", "Languages", Icons.Rounded.Translate, "Languages"),
        ExploreTopicItem("t_fit", "Health & Fitness", Icons.Rounded.FitnessCenter, "Fitness & Yoga")
    )

    val mobileFocusedCourses = listOf(
        ExploreCourseItem(
            id = "c_english_workplace",
            title = "English for Common Interactions in the Workplace",
            organization = "Pontificia Universidad Católica de Chile",
            type = "Course",
            rating = 4.7f,
            reviewCount = "4k",
            thumbnailUrl = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=600",
            orgBadgeText = "UC",
            orgBadgeColor = 0xFF163E75,
            orgTextColor = 0xFFFFFFFF,
            mentorVideo = MentorVideo(
                id = "c_english_workplace",
                title = "English for Common Interactions in the Workplace",
                courseTitle = "Business English Communication",
                duration = "14:20",
                views = 4200,
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                uploadDate = "2 days ago",
                thumbnailUrl = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=600",
                description = "Learn authentic communication skills for meetings, interviews, presentations, and daily workplace interactions with international colleagues.",
                category = "Languages",
                level = "Beginner",
                mentorName = "Pontificia Universidad Católica de Chile",
                price = "Free with SkillBuilder"
            )
        ),
        ExploreCourseItem(
            id = "c_workday_basics",
            title = "Workday Basics Series",
            organization = "Workday",
            type = "Course",
            rating = 4.8f,
            reviewCount = "391",
            thumbnailUrl = "https://images.unsplash.com/photo-1573497019940-1c28c88b4f3e?w=600",
            orgBadgeText = "W",
            orgBadgeColor = 0xFF2464B8,
            orgTextColor = 0xFFFFFFFF,
            mentorVideo = MentorVideo(
                id = "c_workday_basics",
                title = "Workday Basics Series",
                courseTitle = "Enterprise Cloud Applications",
                duration = "18:45",
                views = 3910,
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                uploadDate = "1 week ago",
                thumbnailUrl = "https://images.unsplash.com/photo-1573497019940-1c28c88b4f3e?w=600",
                description = "Master enterprise HCM, financial management, navigation, employee self-service, and analytics in Workday.",
                category = "Tech & Coding",
                level = "All Levels",
                mentorName = "Workday Education",
                price = "Free with SkillBuilder"
            )
        ),
        ExploreCourseItem(
            id = "c_writing_sciences",
            title = "Writing in the Sciences",
            organization = "Stanford Online",
            type = "Course",
            rating = 4.9f,
            reviewCount = "9.8k",
            thumbnailUrl = "https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=600",
            orgBadgeText = "S",
            orgBadgeColor = 0xFF1A4B8C,
            orgTextColor = 0xFFFFFFFF,
            mentorVideo = MentorVideo(
                id = "c_writing_sciences",
                title = "Writing in the Sciences",
                courseTitle = "Scientific Communication & Publishing",
                duration = "22:15",
                views = 9800,
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                uploadDate = "3 weeks ago",
                thumbnailUrl = "https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=600",
                description = "Practical scientific writing: manuscript preparation, peer review, effective grant writing, and communicating findings clearly.",
                category = "Languages",
                level = "Intermediate",
                mentorName = "Stanford Online",
                price = "Free with SkillBuilder"
            )
        ),
        ExploreCourseItem(
            id = "c_android_compose",
            title = "Android with Jetpack Compose",
            organization = "Google Developer Experts",
            type = "Course",
            rating = 4.9f,
            reviewCount = "5.4k",
            thumbnailUrl = "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=600",
            orgBadgeText = "G",
            orgBadgeColor = 0xFF10B981,
            orgTextColor = 0xFFFFFFFF,
            mentorVideo = MentorVideo(
                id = "c_android_compose",
                title = "Android with Jetpack Compose",
                courseTitle = "Modern Android Development",
                duration = "25:30",
                views = 5400,
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                uploadDate = "2 days ago",
                thumbnailUrl = "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=600",
                description = "Build declarative, reactive UI components in Kotlin with StateFlow, Navigation, and modern Android architecture.",
                category = "Tech & Coding",
                level = "Advanced",
                mentorName = "Aman Rathore (GDE)",
                price = "Free with SkillBuilder"
            )
        ),
        ExploreCourseItem(
            id = "c_acoustic_guitar",
            title = "Acoustic Guitar Basics",
            organization = "SkillBuilder Music Academy",
            type = "Course",
            rating = 4.8f,
            reviewCount = "2.4k",
            thumbnailUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600",
            orgBadgeText = "SB",
            orgBadgeColor = 0xFF5995E5,
            orgTextColor = 0xFFFFFFFF,
            mentorVideo = MentorVideo(
                id = "c_acoustic_guitar",
                title = "Acoustic Guitar Basics",
                courseTitle = "Fingerstyle & Harmony",
                duration = "16:10",
                views = 2400,
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                uploadDate = "5 days ago",
                thumbnailUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600",
                description = "Open chords, fingerpicking patterns, rhythm timing, and song accompaniment for aspiring guitarists.",
                category = "Music",
                level = "Beginner",
                mentorName = "Aman Rathore",
                price = "Free with SkillBuilder"
            )
        )
    )

    val degreePrograms = listOf(
        DegreeProgramItem(
            id = "deg_imba_illinois",
            title = "Master of Business Administration (iMBA)",
            university = "University of Illinois",
            thumbnailUrl = "https://images.unsplash.com/photo-1523240795612-9a054b0db644?w=600",
            badgeText = "ILLINOIS",
            badgeBgColor = 0xFFEA580C,
            badgeTextColor = 0xFFFFFFFF,
            mentorVideo = MentorVideo(
                id = "deg_imba_illinois",
                title = "iMBA Program Overview & Strategic Management",
                courseTitle = "iMBA - University of Illinois",
                duration = "20:00",
                views = 12500,
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                uploadDate = "1 month ago",
                thumbnailUrl = "https://images.unsplash.com/photo-1523240795612-9a054b0db644?w=600",
                description = "Top-ranked global online MBA offering strategic leadership, corporate finance, digital marketing, and business analytics.",
                category = "Business",
                level = "Graduate Degree",
                mentorName = "Gies College of Business",
                price = "Accredited Degree"
            )
        ),
        DegreeProgramItem(
            id = "deg_stat_isi",
            title = "Postgraduate Diploma in Applied Statistics",
            university = "Indian Statistical Institute",
            thumbnailUrl = "https://images.unsplash.com/photo-1541339907198-e08756dedf3f?w=600",
            badgeText = "ISI",
            badgeBgColor = 0xFF0D9488,
            badgeTextColor = 0xFFFFFFFF,
            mentorVideo = MentorVideo(
                id = "deg_stat_isi",
                title = "Statistical Inference & Probability Modeling",
                courseTitle = "Applied Statistics Diploma",
                duration = "24:10",
                views = 8900,
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                uploadDate = "2 weeks ago",
                thumbnailUrl = "https://images.unsplash.com/photo-1541339907198-e08756dedf3f?w=600",
                description = "Rigorous statistical foundations, stochastic processes, Bayesian data analysis, and predictive mathematical modeling.",
                category = "Tech & Coding",
                level = "Postgraduate Diploma",
                mentorName = "ISI Faculty Board",
                price = "Accredited Diploma"
            )
        ),
        DegreeProgramItem(
            id = "deg_emba_iitr",
            title = "Executive MBA",
            university = "IIT Roorkee",
            thumbnailUrl = "https://images.unsplash.com/photo-1562774053-701939374585?w=600",
            badgeText = "IITR",
            badgeBgColor = 0xFF0F2B50,
            badgeTextColor = 0xFFFFFFFF,
            mentorVideo = MentorVideo(
                id = "deg_emba_iitr",
                title = "Technology Leadership & Operations Strategy",
                courseTitle = "Executive MBA - IIT Roorkee",
                duration = "28:40",
                views = 15200,
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                uploadDate = "3 weeks ago",
                thumbnailUrl = "https://images.unsplash.com/photo-1562774053-701939374585?w=600",
                description = "Transformational executive program focusing on technological innovation, business operations, supply chain, and global enterprise strategy.",
                category = "Tech & Coding",
                level = "Executive Degree",
                mentorName = "DOMS IIT Roorkee",
                price = "Accredited Degree"
            )
        ),
        DegreeProgramItem(
            id = "deg_mcs_asu",
            title = "Master of Computer Science (MCS)",
            university = "Arizona State University",
            thumbnailUrl = "https://images.unsplash.com/photo-1498243691581-b145c3f54a5a?w=600",
            badgeText = "ASU",
            badgeBgColor = 0xFF163E75,
            badgeTextColor = 0xFFFFC627,
            mentorVideo = MentorVideo(
                id = "deg_mcs_asu",
                title = "Advanced Distributed Systems & Cloud Architecture",
                courseTitle = "MCS - Arizona State University",
                duration = "31:00",
                views = 18400,
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                uploadDate = "1 month ago",
                thumbnailUrl = "https://images.unsplash.com/photo-1498243691581-b145c3f54a5a?w=600",
                description = "Deep dive into artificial intelligence, cybersecurity, algorithms, cloud computing, and large-scale distributed software architectures.",
                category = "Tech & Coding",
                level = "Master's Degree",
                mentorName = "Ira A. Fulton Schools of Engineering",
                price = "Accredited Degree"
            )
        )
    )

    val industryCertifications = listOf(
        CertificateItem(
            id = "cert_google_data",
            title = "Google Data Analytics Professional Certificate",
            provider = "Google",
            rating = 4.8f,
            reviewCount = "134k",
            thumbnailUrl = "https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=600",
            badgeText = "G",
            badgeBgColor = 0xFF2464B8,
            badgeTextColor = 0xFFFFFFFF,
            mentorVideo = MentorVideo(
                id = "cert_google_data",
                title = "Data Cleaning, SQL Queries & Tableau Dashboards",
                courseTitle = "Google Data Analytics Certificate",
                duration = "19:30",
                views = 134000,
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                uploadDate = "1 month ago",
                thumbnailUrl = "https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=600",
                description = "Gain in-demand skills in data cleaning, SQL, R programming, Tableau, and data-driven decision making with Google certified mentors.",
                category = "Tech & Coding",
                level = "Professional Certificate",
                mentorName = "Google Career Certificates",
                price = "Free with SkillBuilder"
            )
        ),
        CertificateItem(
            id = "cert_aws_architect",
            title = "AWS Certified Solutions Architect Associate",
            provider = "Amazon Web Services",
            rating = 4.9f,
            reviewCount = "88k",
            thumbnailUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=600",
            badgeText = "AWS",
            badgeBgColor = 0xFFFF9900,
            badgeTextColor = 0xFF232F3E,
            mentorVideo = MentorVideo(
                id = "cert_aws_architect",
                title = "High Availability, VPC & Serverless Microservices",
                courseTitle = "AWS Solutions Architect Preparation",
                duration = "26:45",
                views = 88000,
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                uploadDate = "2 weeks ago",
                thumbnailUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=600",
                description = "Design resilient, high-performing, cost-optimized cloud architectures using EC2, S3, RDS, Lambda, and IAM on AWS.",
                category = "Tech & Coding",
                level = "Professional Certificate",
                mentorName = "AWS Training & Certification",
                price = "Free with SkillBuilder"
            )
        ),
        CertificateItem(
            id = "cert_meta_frontend",
            title = "Meta Front-End Developer Certificate",
            provider = "Meta",
            rating = 4.8f,
            reviewCount = "56k",
            thumbnailUrl = "https://images.unsplash.com/photo-1633356122544-f134324a6cee?w=600",
            badgeText = "META",
            badgeBgColor = 0xFF3D7ED4,
            badgeTextColor = 0xFFFFFFFF,
            mentorVideo = MentorVideo(
                id = "cert_meta_frontend",
                title = "React.js State Architecture & Modern UI Frameworks",
                courseTitle = "Meta Front-End Developer Program",
                duration = "21:15",
                views = 56000,
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                uploadDate = "3 weeks ago",
                thumbnailUrl = "https://images.unsplash.com/photo-1633356122544-f134324a6cee?w=600",
                description = "Launch your career as a front-end developer. Learn HTML, CSS, JavaScript, React, UI design principles, and version control.",
                category = "Tech & Coding",
                level = "Professional Certificate",
                mentorName = "Meta Staff Engineers",
                price = "Free with SkillBuilder"
            )
        )
    )
}
