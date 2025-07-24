package com.example.tailstale.view.pages

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tailstale.viewmodel.PetViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(petViewModel: PetViewModel) {
    val currentPet by petViewModel.currentPet.collectAsState()
    val requiredVaccines by petViewModel.requiredVaccines.collectAsState()
    val overdueVaccines by petViewModel.overdueVaccines.collectAsState()
    val diseaseRisks by petViewModel.diseaseRisks.collectAsState()
    val diseaseWarnings by petViewModel.diseaseWarnings.collectAsState()
    val vaccinationRecommendations by petViewModel.vaccinationRecommendations.collectAsState()
    val petAgingStats by petViewModel.petAgingStats.collectAsState()

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Pet Info Header
        item {
            currentPet?.let { pet ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
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
                            Column {
                                Text(
                                    text = pet.name,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF007AFF)
                                )
                                Text(
                                    text = "${pet.type} • ${pet.age} months old",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                            Icon(
                                Icons.Default.Pets,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color(0xFFFF9500)
                            )
                        }
                    }
                }
            } ?: run {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No pet selected",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // Current Stats
        item {
            currentPet?.let { pet ->
                StatsCard(
                    title = "Current Stats",
                    icon = Icons.Default.BarChart
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatBar("Health", pet.health, Color(0xFF4CAF50))
                        StatBar("Hunger", 100 - pet.hunger, Color(0xFFFF9800))
                        StatBar("Happiness", pet.happiness, Color(0xFFE91E63))
                        StatBar("Energy", pet.energy, Color(0xFF2196F3))
                        StatBar("Cleanliness", pet.cleanliness, Color(0xFF9C27B0))
                    }
                }
            }
        }

        // Disease History
        item {
            currentPet?.let { pet ->
                if (pet.diseaseHistory.isNotEmpty()) {
                    StatsCard(
                        title = "Disease History",
                        icon = Icons.Default.LocalHospital
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Fix: diseaseHistory is Map<String, Any> where each entry.value contains the disease data
                            pet.diseaseHistory.forEach { (key, value) ->
                                val diseaseMap = value as? Map<String, Any> ?: mapOf(
                                    "diseaseName" to (value.toString()),
                                    "severity" to "Unknown",
                                    "diagnosedDate" to System.currentTimeMillis(),
                                    "treatmentCost" to 0,
                                    "symptoms" to emptyList<String>()
                                )
                                DiseaseHistoryItem(diseaseMap, dateFormat)
                            }
                        }
                    }
                }
            }
        }

        // Disease Warnings
        item {
            if (diseaseWarnings.isNotEmpty()) {
                StatsCard(
                    title = "Health Warnings",
                    icon = Icons.Default.Warning,
                    headerColor = Color(0xFFF44336)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        diseaseWarnings.take(5).forEach { warning ->
                            DiseaseWarningItem(warning)
                        }
                    }
                }
            }
        }

        // Disease Risk Assessment
        item {
            if (diseaseRisks.isNotEmpty()) {
                StatsCard(
                    title = "Disease Risk Assessment",
                    icon = Icons.Default.Analytics
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        diseaseRisks.take(5).forEach { risk ->
                            DiseaseRiskItem(risk)
                        }
                    }
                }
            }
        }

        // Vaccine Records
        item {
            currentPet?.let { pet ->
                if (pet.vaccineHistory.isNotEmpty()) {
                    StatsCard(
                        title = "Vaccination Records",
                        icon = Icons.Default.Vaccines
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Fix: vaccineHistory is Map<String, Any> where each entry.value contains the vaccine data
                            pet.vaccineHistory.forEach { (key, value) ->
                                val vaccineMap = value as? Map<String, Any> ?: mapOf(
                                    "vaccineName" to (value.toString()),
                                    "vaccineId" to key,
                                    "vaccineType" to "Unknown",
                                    "dateAdministered" to System.currentTimeMillis(),
                                    "nextDueDate" to System.currentTimeMillis()
                                )
                                VaccineRecordItem(vaccineMap, dateFormat)
                            }
                        }
                    }
                }
            }
        }

        // Required Vaccines
        item {
            if (requiredVaccines.isNotEmpty()) {
                StatsCard(
                    title = "Required Vaccines",
                    icon = Icons.Default.Schedule,
                    headerColor = Color(0xFF2196F3)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        requiredVaccines.forEach { vaccine ->
                            RequiredVaccineItem(vaccine, petViewModel)
                        }
                    }
                }
            }
        }

        // Overdue Vaccines
        item {
            if (overdueVaccines.isNotEmpty()) {
                StatsCard(
                    title = "Overdue Vaccines",
                    icon = Icons.Default.Error,
                    headerColor = Color(0xFFF44336)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        overdueVaccines.forEach { vaccine ->
                            OverdueVaccineItem(vaccine, petViewModel)
                        }
                    }
                }
            }
        }

        // Vaccination Recommendations
        item {
            if (vaccinationRecommendations.isNotEmpty()) {
                StatsCard(
                    title = "Vaccination Recommendations",
                    icon = Icons.Default.Recommend
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        vaccinationRecommendations.forEach { recommendation ->
                            VaccinationRecommendationItem(recommendation)
                        }
                    }
                }
            }
        }

        // Aging Stats
        item {
            if (petAgingStats.isNotEmpty()) {
                StatsCard(
                    title = "Aging Information",
                    icon = Icons.Default.AccessTime
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        petAgingStats.forEach { (key, value) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = key.replace("_", " ").replaceFirstChar { it.uppercase() },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = value.toString(),
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    headerColor: Color = Color(0xFF007AFF),
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = headerColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = headerColor
                )
            }
            content()
        }
    }
}

@Composable
private fun StatBar(label: String, value: Int, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${value}%",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(value / 100f)
                    .height(12.dp)
                    .background(color, RoundedCornerShape(6.dp))
            )
        }
    }
}

@Composable
private fun DiseaseHistoryItem(disease: Map<String, Any>, dateFormat: SimpleDateFormat) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = disease["diseaseName"]?.toString() ?: "Unknown Disease",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100)
            )
            Text(
                text = "Severity: ${disease["severity"]?.toString() ?: "Unknown"}",
                fontSize = 12.sp,
                color = Color.Gray
            )
            disease["diagnosedDate"]?.let { timestamp ->
                if (timestamp is Long) {
                    Text(
                        text = "Diagnosed: ${dateFormat.format(Date(timestamp))}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            disease["treatmentCost"]?.let { cost ->
                Text(
                    text = "Treatment Cost: $${cost}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun DiseaseWarningItem(warning: com.example.tailstale.service.DiseaseWarning) {
    val warningColor = when (warning.warningLevel.name) {
        "HIGH" -> Color(0xFFF44336)
        "MEDIUM" -> Color(0xFFFF9800)
        "LOW" -> Color(0xFFFFEB3B)
        else -> Color(0xFF4CAF50)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = warningColor.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = warning.disease.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = warningColor
                )
                // Fix: Use AssistChip instead of private Chip
                AssistChip(
                    onClick = { },
                    label = { Text("${warning.riskPercentage}%", fontSize = 10.sp) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = warningColor)
                )
            }
            Text(
                text = warning.recommendedAction,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun DiseaseRiskItem(risk: com.example.tailstale.service.DiseaseRiskAssessment) {
    val riskColor = if (risk.isHighRisk) Color(0xFFF44336) else Color(0xFF4CAF50)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = riskColor.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = risk.disease.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${risk.riskPercentage}% risk",
                    fontSize = 12.sp,
                    color = riskColor,
                    fontWeight = FontWeight.Bold
                )
            }
            if (risk.riskFactors.isNotEmpty()) {
                Text(
                    text = "Risk factors: ${risk.riskFactors.joinToString(", ")}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun VaccineRecordItem(vaccine: Map<String, Any>, dateFormat: SimpleDateFormat) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = vaccine["vaccineName"]?.toString() ?: "Unknown Vaccine",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            vaccine["dateAdministered"]?.let { timestamp ->
                if (timestamp is Long) {
                    Text(
                        text = "Administered: ${dateFormat.format(Date(timestamp))}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            vaccine["nextDueDate"]?.let { timestamp ->
                if (timestamp is Long) {
                    Text(
                        text = "Next due: ${dateFormat.format(Date(timestamp))}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun RequiredVaccineItem(vaccine: com.example.tailstale.model.VaccineModel, petViewModel: PetViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vaccine.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )
                Text(
                    text = vaccine.description,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Cost: $${vaccine.cost}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
            Button(
                onClick = { petViewModel.vaccinatePet(vaccine.id, vaccine.name) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) {
                Text("Vaccinate", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun OverdueVaccineItem(vaccine: com.example.tailstale.model.VaccineModel, petViewModel: PetViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = vaccine.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF44336)
                    )
                }
                Text(
                    text = "OVERDUE - ${vaccine.description}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Cost: $${vaccine.cost}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
            Button(
                onClick = { petViewModel.vaccinatePet(vaccine.id, vaccine.name) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
            ) {
                Text("Urgent", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun VaccinationRecommendationItem(recommendation: com.example.tailstale.service.VaccinationRecommendation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = recommendation.vaccine.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7B1FA2)
            )
            Text(
                text = recommendation.reason,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = "Urgency: ${recommendation.urgency.name}",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}
