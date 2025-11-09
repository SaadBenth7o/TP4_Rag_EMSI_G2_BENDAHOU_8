# TP4 - Système RAG (Retrieval-Augmented Generation)
## 📋 Contexte Académique

**Formation :** Master Ingénierie Logicielle et Intelligence Artificielle  
**Établissement :**  Université Côte d'Azur  
**Module :** Intelligence Artificielle et Systèmes Distribués  
**Année :** 2025-2026  
**Encadrant :** M. Richard Grin
---
##  Description du Projet

Ce projet implémente plusieurs variantes d'un système **RAG (Retrieval-Augmented Generation)** en Java utilisant la bibliothèque **LangChain4j** et le modèle de langage **Google Gemini**. Le RAG permet d'améliorer les réponses d'un modèle de langage en lui fournissant des informations contextuelles pertinentes extraites d'un corpus de documents.

##  Objectifs

Ce projet démontre différentes approches de RAG :
1. **RAG basique** : Récupération d'informations depuis un document PDF
2. **RAG conditionnel** : Utilisation conditionnelle du RAG selon le type de question
3. **RAG hybride** : Combinaison de documents locaux et de recherche web
4. **RAG avec routage** : Routage intelligent entre plusieurs sources de documents

##  Architecture

### Composants Principaux

- **Assistant** : Interface définissant le contrat de conversation avec le modèle
- **RagNaif** : Implémentation basique du RAG
- **TestPasDeRag** : RAG avec routage conditionnel (avec/sans RAG)
- **TestRagWeb** : RAG combinant documents PDF et recherche web
- **TestRoutage** : RAG avec routage entre plusieurs documents

### Technologies Utilisées

- **LangChain4j** (v1.7.1) : Framework pour applications LLM
- **Google Gemini AI** : Modèle de langage pour la génération de réponses
- **Apache Tika** : Parser de documents (PDF)
- **AllMiniLmL6V2** : Modèle d'embedding pour la recherche sémantique
- **Tavily** : Moteur de recherche web
- **Java 21** : Langage de programmation

## 📁 Structure du Projet

```
tp4/
├── pom.xml                          # Configuration Maven
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── Assistant.java       # Interface de l'assistant
│   │   │   ├── RagNaif.java         # RAG basique
│   │   │   ├── TestPasDeRag.java    # RAG conditionnel
│   │   │   ├── TestRagWeb.java      # RAG avec recherche web
│   │   │   └── TestRoutage.java     # RAG avec routage multi-documents
│   │   └── resources/
│   │       ├── LLM_Course_RAG.pdf   # Document sur le RAG
│   │       └── ArtDeco.pdf          # Document sur l'Art Déco
│   └── test/
└── README.md
```

## 🚀 Installation et Configuration

### Prérequis

- **Java 21** ou supérieur
- **Maven 3.6+**
- **Clé API Google Gemini** : Obtenez votre clé sur [Google AI Studio](https://makersuite.google.com/app/apikey)
- **Clé API Tavily** (optionnelle, pour TestRagWeb) : Obtenez votre clé sur [Tavily](https://tavily.com/)

### Configuration des Variables d'Environnement

Avant d'exécuter le projet, configurez les variables d'environnement suivantes :

#### Windows (PowerShell)
```powershell
$env:GEMINI_KEY="votre_cle_gemini"
$env:c="votre_cle_tavily"  # Uniquement pour TestRagWeb
```

#### Linux/Mac
```bash
export GEMINI_KEY="votre_cle_gemini"
export c="votre_cle_tavily"  # Uniquement pour TestRagWeb
```

### Compilation

```bash
mvn clean compile
```

### Exécution

Chaque classe peut être exécutée indépendamment :

```bash
# RAG basique
mvn exec:java -Dexec.mainClass="RagNaif"

# RAG conditionnel
mvn exec:java -Dexec.mainClass="TestPasDeRag"

# RAG avec recherche web
mvn exec:java -Dexec.mainClass="TestRagWeb"

# RAG avec routage
mvn exec:java -Dexec.mainClass="TestRoutage"
```

## 📖 Détails des Implémentations

### 1. RagNaif - RAG Basique

**Fonctionnalité** : Implémentation simple du RAG qui charge un document PDF, crée des embeddings et répond aux questions en utilisant le contenu du document.

**Processus** :
1. Chargement et parsing du PDF `LLM_Course_RAG.pdf`
2. Découpage du document en segments (300 caractères, overlap de 50)
3. Génération d'embeddings pour chaque segment
4. Stockage dans un `InMemoryEmbeddingStore`
5. Récupération des segments pertinents lors des questions
6. Génération de réponses par Gemini en utilisant le contexte récupéré

**Utilisation** : Idéal pour comprendre les bases du RAG.

### 2. TestPasDeRag - RAG Conditionnel

**Fonctionnalité** : Utilise un `QueryRouter` personnalisé qui décide si la question nécessite l'utilisation du RAG ou non.

**Logique de routage** :
- Analyse la question pour déterminer si elle porte sur l'IA
- Si la réponse est "non", n'utilise pas le RAG (réponse directe du modèle)
- Si la réponse est "oui" ou "peut-être", utilise le RAG avec le document

**Avantage** : Économise des appels API et améliore les performances pour les questions générales.

### 3. TestRagWeb - RAG Hybride

**Fonctionnalité** : Combine la récupération depuis un document PDF local avec la recherche web en temps réel.

**Sources de données** :
- **PDF local** : `LLM_Course_RAG.pdf` (connaissances statiques)
- **Recherche web** : Tavily (informations à jour)

**Routage** : Utilise `DefaultQueryRouter` qui interroge les deux sources et combine les résultats.

**Utilisation** : Parfait pour des questions nécessitant à la fois des connaissances documentaires et des informations récentes.

### 4. TestRoutage - RAG Multi-Documents

**Fonctionnalité** : Routage intelligent entre plusieurs documents PDF selon la pertinence de la question.

**Documents disponibles** :
- `LLM_Course_RAG.pdf` : Cours sur le RAG et les LLM
- `ArtDeco.pdf` : Document sur l'architecture Art Déco

**Routage** : Utilise `LanguageModelQueryRouter` qui analyse la question et route vers le document le plus pertinent en fonction des descriptions fournies.

**Avantage** : Permet de gérer plusieurs domaines de connaissances dans un seul système.

## 🔧 Paramètres Configurables

### Modèle de Langage
- **Modèle** : `gemini-2.5-flash` ou `gemini-2.0-flash-exp`
- **Température** : 0.3 (réponses plus déterministes)

### Embeddings
- **Modèle** : `AllMiniLmL6V2EmbeddingModel`
- **Taille des segments** : 300 caractères
- **Overlap** : 50 caractères

### Récupération
- **Max résultats** : 3-4 segments
- **Score minimum** : 0.5 (similarité cosinus)

### Mémoire
- **Taille de la fenêtre** : 10 messages (contexte de conversation)

## 💡 Exemples d'Utilisation

### Avec RagNaif
```
Posez votre question : Qu'est-ce que le RAG?
Assistant : Le RAG (Retrieval-Augmented Generation) est une technique...
```

### Avec TestPasDeRag
```
Posez votre question : Quel temps fait-il aujourd'hui?
Assistant : [Réponse directe sans RAG]

Posez votre question : Comment fonctionne le RAG?
Assistant : [Réponse avec RAG utilisant le document]
```

### Avec TestRagWeb
```
Posez votre question : Quelles sont les dernières avancées en RAG?
Assistant : [Combine informations du PDF et résultats web récents]
```

### Avec TestRoutage
```
Posez votre question : Qu'est-ce que le RAG?
Assistant : [Route vers LLM_Course_RAG.pdf]

Posez votre question : Qu'est-ce que l'Art Déco?
Assistant : [Route vers ArtDeco.pdf]
```

## 🛠️ Dépendances Maven

Les principales dépendances sont définies dans `pom.xml` :
- `langchain4j` : Framework principal
- `langchain4j-google-ai-gemini` : Intégration Gemini
- `langchain4j-easy-rag` : Utilitaires RAG
- `langchain4j-document-parser-apache-tika` : Parser de documents
- `langchain4j-embeddings-all-minilm-l6-v2` : Modèle d'embedding
- `langchain4j-web-search-engine-tavily` : Recherche web

## 📝 Notes Importantes

1. **Clés API** : Assurez-vous que vos clés API sont correctement configurées dans les variables d'environnement
2. **Documents** : Les fichiers PDF doivent être placés dans `src/main/resources/`
3. **Mémoire** : Le système conserve les 10 derniers messages pour maintenir le contexte de conversation
4. **Fin de conversation** : Tapez "fin" pour quitter l'application

## 🔍 Concepts Clés

### RAG (Retrieval-Augmented Generation)
Technique qui améliore les réponses d'un LLM en :
1. Récupérant des documents pertinents depuis une base de connaissances
2. Injectant ces documents comme contexte dans le prompt
3. Générant une réponse basée sur le contexte récupéré

### Embeddings
Représentations vectorielles du texte permettant de mesurer la similarité sémantique entre différents textes.

### Query Routing
Mécanisme permettant de diriger une requête vers la source de données la plus appropriée.

## 📚 Ressources

- [Documentation LangChain4j](https://github.com/langchain4j/langchain4j)
- [Google Gemini API](https://ai.google.dev/)
- [Tavily Search API](https://tavily.com/)

## 👤 Auteur

BENDAHOU SAAD

