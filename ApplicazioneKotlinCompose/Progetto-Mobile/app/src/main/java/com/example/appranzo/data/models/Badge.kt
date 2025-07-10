package com.example.appranzo.data.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

@Serializable
data class BadgeDto(val nome: String)

data class Badge(
    val nome: String,
    val icona: ImageVector,
    val threshold: Int,
    val descrizione: String
) {
    companion object {
        val roadmapBadge = listOf(
            Badge(
                nome = "Esploratore Nascente",
                icona = Icons.Default.Shield,
                threshold = 100,
                descrizione = "Hai mosso i primi passi nel mondo del gusto. Il viaggio è appena iniziato, ma la tua curiosità è già una leggenda!"
            ),
            Badge(
                nome = "Giramondo Gastronomico",
                icona = Icons.Default.RocketLaunch,
                threshold = 500,
                descrizione = "Nessun locale è troppo lontano, nessuna recensione troppo audace. Stai mappando l'universo del cibo, un boccone alla volta."
            ),
            Badge(
                nome = "Maestro dei Sapori",
                icona = Icons.Default.Whatshot,
                threshold = 1000,
                descrizione = "I tuoi sensi sono affilati come la lama di uno chef. Distingui ogni spezia, ogni sfumatura, trasformando ogni pasto in una sinfonia."
            ),
            Badge(
                nome = "Gourmet Leggendario",
                icona = Icons.Default.Public,
                threshold = 2500,
                descrizione = "Le tue recensioni sono Vangelo, i tuoi consigli valgono oro. I ristoratori sussurrano il tuo nome con un misto di timore e ammirazione."
            ),
            Badge(
                nome = "Titano del Gusto",
                icona = Icons.Default.WorkspacePremium,
                threshold = 3000,
                descrizione = "Sei una forza della natura culinaria. I tuoi giudizi possono decretare il successo o l'oblio di un locale. Un grande potere comporta grandi... forchette."
            ),
            Badge(
                nome = "Divinità della Forchetta",
                icona = Icons.Default.MilitaryTech,
                threshold = 3500,
                descrizione = "Il tuo palato è su un altro piano di esistenza. Non mangi, celebri. Non assaggi, benedici. L'Olimpo della gastronomia ti attende."
            ),
            Badge(
                nome = "Sovrano della Tavola Rotonda",
                icona = Icons.Default.Star,
                threshold = 5000,
                descrizione = "Siedi al vertice del regno culinario. Ogni tavola è la tua corte, ogni chef il tuo vassallo. Il tuo dominio sul gusto è assoluto e indiscutibile."
            )
        )
    }
}

