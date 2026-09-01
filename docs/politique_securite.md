# Politique de Sécurité Informatique — iAgen

**Version** : 2.1 | **Date** : Janvier 2025 | **Classification** : Interne

---

## 1. Politique des mots de passe

### Règles de création

Tous les mots de passe utilisés dans le cadre professionnel chez iAgen doivent respecter les critères suivants :

- **Longueur minimale** : 14 caractères
- **Composition obligatoire** : au moins 1 majuscule, 1 minuscule, 1 chiffre, 1 caractère spécial (!, @, #, $, %, etc.)
- **Interdits** : prénom, nom, date de naissance, "iAgen", "password", suites simples (123456, azerty)
- **Unicité** : un mot de passe ne peut pas être réutilisé avant 12 changements

### Renouvellement

- Les mots de passe de compte utilisateur doivent être renouvelés tous les **90 jours**
- Les mots de passe de comptes à privilèges (admin, DevOps) doivent être renouvelés tous les **30 jours**

### Gestionnaire de mots de passe

L'utilisation du gestionnaire de mots de passe d'entreprise **Bitwarden** est obligatoire pour tous les salariés. Les mots de passe ne doivent jamais être écrits sur papier ou stockés en clair.

---

## 2. Politique VPN

La connexion au réseau interne d'iAgen depuis l'extérieur (télétravail, déplacements) **est obligatoirement** effectuée via le VPN d'entreprise.

- **Solution VPN** : Cisco AnyConnect (configuration fournie par la DSI)
- **Certificat** : un certificat personnel est fourni à chaque employé
- **Obligation** : le VPN doit être activé dès qu'on accède à une ressource interne
- **Logs** : toutes les connexions VPN sont journalisées pendant **12 mois**

**Interdictions** :
- Utiliser un VPN personnel ou un outil tiers non approuvé
- Partager ses identifiants VPN avec un tiers, même un collègue

---

## 3. Gestion des incidents de sécurité

### Classification des incidents

| Niveau | Description | Délai de signalement |
|---|---|---|
| P1 - Critique | Accès non autorisé, ransomware, fuite de données | **Immédiat (< 1h)** |
| P2 - Majeur | Tentative d'intrusion, phishing réussi | **< 4 heures** |
| P3 - Modéré | Anomalie détectée, comportement suspect | **< 24 heures** |
| P4 - Mineur | Oubli de verrouillage, email suspect | **< 72 heures** |

### Procédure de signalement

1. Contacter immédiatement la DSI via **securite@iagen.fr** ou le numéro d'urgence **+33 1 82 00 00 01**
2. Ne pas éteindre l'équipement concerné (préserver les preuves)
3. Documenter les actions effectuées avec horodatage
4. Le RSSI (Responsable Sécurité) prend en charge l'incident sous 30 minutes (P1/P2)

---

## 4. Sécurité des postes de travail

- **Chiffrement** : tous les disques durs doivent être chiffrés (BitLocker obligatoire sous Windows)
- **Verrouillage automatique** : délai maximum 5 minutes d'inactivité
- **Antivirus** : solution CrowdStrike déployée et maintenue à jour par la DSI
- **Mises à jour** : les correctifs de sécurité sont déployés automatiquement sous **72h** après publication

---

## 5. Politique d'utilisation acceptable

Les ressources informatiques d'iAgen sont à usage professionnel. Un usage personnel modéré est toléré sous réserve que :
- Il ne nuise pas à la productivité
- Il ne compromette pas la sécurité du réseau
- Il ne contrevienne pas à la loi

**Interdictions absolues** :
- Installer des logiciels non approuvés par la DSI
- Connecter des périphériques de stockage personnels (clés USB, disques externes)
- Accéder à des sites malveillants ou de phishing
- Partager des données confidentielles sur des services cloud personnels
