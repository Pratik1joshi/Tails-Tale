package com.example.tailstale.view.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tailstale.service.DiseaseWarning
import com.example.tailstale.service.UrgencyLevel
import com.example.tailstale.service.VaccinationRecommendation
import com.example.tailstale.service.WarningLevel
import com.example.tailstale.viewmodel.PetViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthScreen() {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val petViewModel: PetViewModel = viewModel(
        factory = com.example.tailstale.viewmodel.PetViewModelFactory(
            com.example.tailstale.repo.PetRepositoryImpl(),
            com.example.tailstale.repo.UserRepositoryImpl()
        )
    )

    // Observe health-related data
    val currentPet by petViewModel.currentPet.collectAsState()
    val diseaseWarnings by petViewModel.diseaseWarnings.collectAsState()
    val vaccinationRecommendations by petViewModel.vaccinationRecommendations.collectAsState()
    val requiredVaccines by petViewModel.requiredVaccines.collectAsState()
    val overdueVaccines by petViewModel.overdueVaccines.collectAsState()
    val loading by petViewModel.loading.collectAsState()
    val error by petViewModel.error.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    // Load pet data when screen loads
    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { userId ->
            petViewModel.loadUserPets(userId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color(0xFFE91E63)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Pet Health & Vaccination",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                currentPet?.let { pet ->
                    Text(
                        "${pet.name} • ${pet.age} months old",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Loading state
        if (loading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // Error state
        error?.let { errorMessage ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (errorMessage.contains("success", ignoreCase = true))
                        Color(0xFF4CAF50) else Color(0xFFFF5722)
                )
            ) {
                Text(
                    errorMessage,
                    color = Color.White,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Main content
        currentPet?.let { pet ->
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Disease Warnings Section
                if (diseaseWarnings.isNotEmpty()) {
                    item {
                        Text(
                            "⚠️ Disease Warnings",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF5722)
                        )
                    }

                    items(diseaseWarnings) { warning ->
                        DiseaseWarningCard(
                            warning = warning,
                            onVaccinateClick = { disease ->
                                // Find the recommended vaccine for this disease
                                val recommendedVaccine = disease.requiredVaccineForPrevention
                                    ?: disease.preventableByVaccines.firstOrNull()

                                recommendedVaccine?.let { vaccineName ->
                                    coroutineScope.launch {
                                        petViewModel.vaccinatePet(
                                            vaccineId = "vaccine_${System.currentTimeMillis()}",
                                            vaccineName = vaccineName
                                        )
                                    }
                                }
                            }
                        )
                    }
                }

                // Vaccination Recommendations Section
                if (vaccinationRecommendations.isNotEmpty()) {
                    item {
                        Text(
                            "💉 Vaccination Recommendations",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2196F3)
                        )
                    }

                    items(vaccinationRecommendations) { recommendation ->
                        VaccinationRecommendationCard(
                            recommendation = recommendation,
                            onVaccinateClick = { vaccine ->
                                coroutineScope.launch {
                                    petViewModel.vaccinatePet(
                                        vaccineId = vaccine.id,
                                        vaccineName = vaccine.name
                                    )
                                }
                            }
                        )
                    }
                }

                // Required Vaccines Section
                if (requiredVaccines.isNotEmpty()) {
                    item {
                        Text(
                            "🏥 Required Vaccines",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    }

                    items(requiredVaccines) { vaccine ->
                        VaccineCard(
                            vaccine = vaccine,
                            isOverdue = vaccine in overdueVaccines,
                            onVaccinateClick = {
                                coroutineScope.launch {
                                    petViewModel.vaccinatePet(
                                        vaccineId = vaccine.id,
                                        vaccineName = vaccine.name
                                    )
                                }
                            }
                        )
                    }
                }

                // Current Protection Status
                item {
                    Text(
                        "🛡️ Current Protection Status",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9C27B0)
                    )
                }

                item {
                    ProtectionStatusCard(pet = pet)
                }

                // Health Tips
                item {
                    Text(
                        "📋 Health Tips",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF607D8B)
                    )
                }

                item {
                    HealthTipsCard(pet = pet)
                }
            }
        } ?: run {
            // No pet selected state
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Pets,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No pet selected",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Text(
                        "Please create or select a pet to view health information",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun DiseaseWarningCard(
    warning: DiseaseWarning,
    onVaccinateClick: (com.example.tailstale.model.DiseaseModel) -> Unit
) {
    val backgroundColor = when (warning.warningLevel) {
        WarningLevel.HIGH -> Color(0xFFFF5722)
        WarningLevel.MEDIUM -> Color(0xFFFF9800)
        WarningLevel.LOW -> Color(0xFFFFC107)
        WarningLevel.INFO -> Color(0xFF2196F3)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = backgroundColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            warning.disease.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    Text(
                        "Risk: ${warning.riskPercentage}% • ${warning.warningLevel.name}",
                        fontSize = 12.sp,
                        color = backgroundColor,
                        fontWeight = FontWeight.Medium
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = backgroundColor,
                    modifier = Modifier.clickable { onVaccinateClick(warning.disease) }
                ) {
                    Text(
                        "Vaccinate",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                warning.disease.description,
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Recommended: ${warning.recommendedAction}",
                fontSize = 12.sp,
                color = backgroundColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun VaccinationRecommendationCard(
    recommendation: VaccinationRecommendation,
    onVaccinateClick: (com.example.tailstale.model.VaccineModel) -> Unit
) {
    val urgencyColor = when (recommendation.urgency) {
        UrgencyLevel.IMMEDIATE -> Color(0xFFFF1744)
        UrgencyLevel.HIGH -> Color(0xFFFF5722)
        UrgencyLevel.MEDIUM -> Color(0xFFFF9800)
        UrgencyLevel.LOW -> Color(0xFF4CAF50)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        recommendation.vaccine.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Text(
                        "Urgency: ${recommendation.urgency.name} • Cost: $${recommendation.vaccine.cost}",
                        fontSize = 12.sp,
                        color = urgencyColor,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = { onVaccinateClick(recommendation.vaccine) },
                    colors = ButtonDefaults.buttonColors(containerColor = urgencyColor)
                ) {
                    Text("Vaccinate", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                recommendation.vaccine.description,
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                recommendation.reason,
                fontSize = 12.sp,
                color = urgencyColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun VaccineCard(
    vaccine: com.example.tailstale.model.VaccineModel,
    isOverdue: Boolean,
    onVaccinateClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue) Color(0xFFFF5722).copy(alpha = 0.1f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isOverdue) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = Color(0xFFFF5722),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            vaccine.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    Text(
                        "Cost: $${vaccine.cost}" + if (isOverdue) " • OVERDUE" else "",
                        fontSize = 12.sp,
                        color = if (isOverdue) Color(0xFFFF5722) else Color.Gray,
                        fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Button(
                    onClick = onVaccinateClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isOverdue) Color(0xFFFF5722) else Color(0xFF4CAF50)
                    )
                ) {
                    Text("Vaccinate", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                vaccine.description,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ProtectionStatusCard(pet: com.example.tailstale.model.PetModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Vaccination History",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (pet.vaccineHistory.isEmpty()) {
                Text(
                    "No vaccinations recorded yet",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                )
            } else {
                pet.vaccineHistory.forEach { (key, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            key,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
fun HealthTipsCard(pet: com.example.tailstale.model.PetModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Age-Specific Health Tips",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            val tips = when (pet.growthStage) {
                com.example.tailstale.model.GrowthStage.BABY -> listOf(
                    "• Complete DHPP vaccination series (3 doses)",
                    "• Keep indoors until fully vaccinated",
                    "• Monitor for parvovirus symptoms",
                    "• Schedule first vet checkup"
                )
                com.example.tailstale.model.GrowthStage.YOUNG -> listOf(
                    "• Get rabies vaccination at 14-16 weeks",
                    "• Consider lifestyle vaccines (Bordetella, Lyme)",
                    "• Start heartworm prevention",
                    "• Monitor for kennel cough in social settings"
                )
                com.example.tailstale.model.GrowthStage.ADULT -> listOf(
                    "• Keep up with annual boosters",
                    "• Regular dental care",
                    "• Watch for hip dysplasia signs",
                    "• Maintain healthy weight"
                )
                com.example.tailstale.model.GrowthStage.SENIOR -> listOf(
                    "• Increase vet checkup frequency",
                    "• Monitor for arthritis symptoms",
                    "• Consider joint supplements",
                    "• Watch for age-related diseases"
                )
            }

            tips.forEach { tip ->
                Text(
                    tip,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}
