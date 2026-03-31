package com.topespinf.collabtask.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Diversity3
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.topespinf.collabtask.R
import com.topespinf.collabtask.ui.theme.Primary
import com.topespinf.collabtask.ui.theme.ScreenBackground
import com.topespinf.collabtask.ui.theme.Secondary

data class FeatureItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun HomeScreen(
    onGetStartedClick: () -> Unit
) {
    val features = listOf(
        FeatureItem("Gerenciamento de Tarefas", "Crie, atribua e acompanhe tarefas com facilidade.", Icons.Outlined.TaskAlt, Primary),
        FeatureItem("Colaboracao em Equipe", "Trabalhe em conjunto com atualizacoes em tempo real.", Icons.Outlined.Diversity3, Secondary),
        FeatureItem("Acompanhamento", "Monitore o status e o progresso de cada entrega.", Icons.Outlined.QueryStats, Color(0xFF22C55E)),
        FeatureItem("Prioridades", "Defina niveis de prioridade para organizar o backlog.", Icons.Outlined.Flag, Color(0xFFF59E0B)),
        FeatureItem("Painel Pessoal", "Visualize tarefas atribuidas e pendencias do dia.", Icons.Outlined.Person, Primary),
        FeatureItem("Seguro e Confiavel", "Dados protegidos com autenticacao segura.", Icons.Outlined.Lock, Secondary)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HeroSection(onGetStartedClick = onGetStartedClick)
        }
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.home_section_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.home_section_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
        item {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.height(500.dp),
                userScrollEnabled = false
            ) {
                items(features) { item ->
                    FeatureCard(item)
                }
            }
        }
        item {
            CtaSection()
        }
    }
}

@Composable
private fun HeroSection(
    onGetStartedClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Primary, Secondary)
                )
            )
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(R.string.headline_home),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = stringResource(R.string.home_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.92f)
            )
            Button(onClick = onGetStartedClick) {
                Text(text = stringResource(R.string.start_now))
            }
        }
    }
}

@Composable
private fun FeatureCard(feature: FeatureItem) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(feature.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(feature.icon, contentDescription = null, tint = feature.color)
            }
            Text(feature.title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
            Text(
                feature.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun CtaSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.horizontalGradient(listOf(Primary, Secondary)))
            .padding(22.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Pronto para transformar a produtividade da sua equipe?",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Junte-se a milhares de equipes que usam o CollabTask.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row {
                Text("Cadastre-se Agora", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

