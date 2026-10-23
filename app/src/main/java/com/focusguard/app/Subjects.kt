package com.focusguard.app

/** Catalogue des matières du Génie Civil, groupées par domaine. */
object Subjects {
    val catalog: List<Pair<String, List<String>>> = listOf(
        "Sciences fondamentales" to listOf(
            "Analyse mathématique",
            "Algèbre linéaire",
            "Probabilités et statistiques",
            "Analyse numérique",
            "Méthodes numériques",
            "Physique générale",
            "Mécanique générale",
            "Chimie générale",
            "Informatique et algorithmique",
            "Programmation (Python / MATLAB)"
        ),
        "Dessin, topographie et SIG" to listOf(
            "Dessin technique",
            "Dessin de bâtiment",
            "DAO / CAO (AutoCAD)",
            "Topographie",
            "Géodésie",
            "Photogrammétrie",
            "Cartographie et SIG",
            "Métrés"
        ),
        "Mécanique des structures" to listOf(
            "Statique",
            "Résistance des matériaux (RDM)",
            "Mécanique des milieux continus",
            "Théorie de l'élasticité",
            "Calcul des structures",
            "Méthode des éléments finis",
            "Dynamique des structures",
            "Stabilité des structures (flambement)",
            "Plasticité"
        ),
        "Matériaux de construction" to listOf(
            "Matériaux de construction",
            "Technologie du béton",
            "Bitumes et enrobés",
            "Chimie des matériaux",
            "Durabilité et pathologie des ouvrages"
        ),
        "Géologie et géotechnique" to listOf(
            "Géologie générale",
            "Géologie de l'ingénieur",
            "Mécanique des sols",
            "Mécanique des roches",
            "Géotechnique",
            "Fondations",
            "Ouvrages de soutènement",
            "Terrassements",
            "Stabilité des pentes",
            "Hydrogéologie"
        ),
        "Béton, acier, bois et ouvrages" to listOf(
            "Béton armé",
            "Béton précontraint",
            "Charpente métallique",
            "Construction mixte acier-béton",
            "Construction en bois",
            "Maçonnerie",
            "Eurocodes",
            "Génie parasismique",
            "Ponts et ouvrages d'art",
            "Tunnels et ouvrages souterrains"
        ),
        "Hydraulique et environnement" to listOf(
            "Mécanique des fluides",
            "Hydraulique générale",
            "Hydraulique à surface libre",
            "Hydrologie",
            "Alimentation en eau potable (AEP)",
            "Assainissement",
            "Ouvrages hydrauliques",
            "Barrages",
            "Irrigation et drainage",
            "Hydraulique urbaine",
            "Environnement et développement durable"
        ),
        "Routes et transports" to listOf(
            "Routes et conception géométrique",
            "Chaussées",
            "Géotechnique routière",
            "Trafic et transport",
            "Voies ferrées",
            "Aéroports",
            "Ports et ouvrages maritimes",
            "Signalisation et sécurité routière",
            "VRD (voirie et réseaux divers)"
        ),
        "Bâtiment et architecture" to listOf(
            "Technologie du bâtiment",
            "Second œuvre",
            "Architecture",
            "Physique du bâtiment (thermique)",
            "Acoustique du bâtiment",
            "Électricité du bâtiment",
            "Plomberie sanitaire",
            "Équipements techniques du bâtiment"
        ),
        "Gestion et organisation" to listOf(
            "Organisation de chantier",
            "Gestion de projet",
            "Planification (MS Project)",
            "Estimation et devis quantitatif",
            "Économie de la construction",
            "Droit de la construction et marchés publics",
            "Management et entrepreneuriat",
            "Hygiène, sécurité et santé (HSE)",
            "Qualité",
            "BIM (Revit)"
        ),
        "Logiciels de calcul" to listOf(
            "Robot Structural Analysis",
            "SAP2000",
            "ETABS",
            "Covadis",
            "Excel pour l'ingénieur"
        ),
        "Langues, communication et projets" to listOf(
            "Anglais technique",
            "Français et expression écrite",
            "Communication",
            "Travaux pratiques (TP)",
            "Projet de fin d'études (PFE)",
            "Stage",
            "Mémoire"
        )
    )
}
