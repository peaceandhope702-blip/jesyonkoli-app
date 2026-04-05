# 📘 📦 DOCUMENTATION PROJET — JESYONKOLI

---

## 🧾 1. Présentation du projet

**Nom :** JesyonKoli
**Type :** Application Android (Java)

**Objectif :**
JesyonKoli est une application conçue pour faciliter la gestion des encomendas dans un condominium.

Elle permet :

* à la **portaria** d’enregistrer les colis
* aux **moradores** de consulter leurs encomendas
* de gérer la **signature lors du retrait**
* de conserver un **historique complet**

---

## 🏗️ 2. Architecture du projet

### 📦 Package principal

com.ricardo.jesyonkoli

---

### 🔧 core

#### constants / util

* **UpdateChecker** : Vérifie si une nouvelle version de l’application est disponible

---

### 💾 data

#### adapter

* EncomendaAdapter → affichage liste encomendas
* MoradorEncomendaAdapter → côté morador
* PortariaEncomendaAdapter → côté portaria
* UnitAdapter → gestion des unités
* UsuarioAdapter → affichage utilisateurs

#### model

* Encomenda → modèle colis
* UnitModel → modèle unité
* UsuarioModel → modèle utilisateur

#### repo

* Gestion des données (Firestore et logique backend)

---

### 🖥️ ui

#### admin

* AdminPanelActivity → panneau admin
* UnitsActivity → gestion unités
* UsuariosActivity → gestion utilisateurs

#### auth

* LoginActivity → connexion
* RegisterActivity → inscription
* RoleGate → redirection selon rôle

#### morador

* MoradorHomeActivity → écran principal
* MoradorDetalheEncomendaActivity → détails colis

#### portaria

* PortariaDashboardActivity → dashboard
* NovaEncomendaActivity → ajouter colis
* PendentesActivity → colis en attente
* HistoricoActivity → historique
* DetalheEncomendaActivity → détails
* AssinaturaActivity → signature

#### view

* SignatureView → signature digitale personnalisée

---

## ⚙️ 3. Layouts principaux

* activity_admin_panel.xml
* activity_units.xml
* activity_usuarios.xml
* activity_login.xml
* activity_splash.xml
* activity_portaria_dashboard.xml
* activity_pendentes.xml
* activity_historico.xml
* activity_nova_encomenda.xml
* activity_detalhe_encomenda.xml
* activity_assinatura.xml
* activity_morador_home.xml
* activity_morador_detalhe_encomenda.xml

---

## ⚙️ 4. Fonctionnalités principales

### 👤 Authentification

* Connexion utilisateur
* Inscription
* Redirection selon rôle

### 🏢 Gestion des unités

* Ajouter unité
* Activer / désactiver
* Liste des unités

### 📦 Gestion des encomendas

* Ajouter colis
* Voir pendentes
* Voir historique
* Détails complets

### ✍️ Signature digitale

* Signature du morador
* Enregistrement de preuve

### 👥 Gestion utilisateurs

* Consultation utilisateurs
* Gestion des rôles

### 🔄 Mise à jour application

* Vérification version via Firebase

---

## 🔁 5. Flux de l’application

SplashActivity
↓
LoginActivity
↓
RoleGate
↓
Admin / Portaria / Morador

---

## 🧰 6. Technologies utilisées

* Java
* Android Studio
* Firebase Authentication
* Firebase Firestore
* RecyclerView
* ViewBinding
* Material Design

---

## 🎯 7. Rôles utilisateurs

| Rôle     | Description                 |
| -------- | --------------------------- |
| Admin    | Gère utilisateurs et unités |
| Portaria | Gère les encomendas         |
| Morador  | Consulte et signe           |

---

## 🚀 8. Points forts du projet

* Architecture claire et organisée
* Séparation des responsabilités
* Interface simple et efficace
* Utilisation de Firebase (temps réel)
* Signature digitale intégrée

---

## 📌 Conclusion

JesyonKoli est une application complète de gestion de colis adaptée aux besoins d’un condominium moderne.
Elle améliore l’organisation, la traçabilité et l’expérience des utilisateurs grâce à une solution digitale simple et efficace.

---
