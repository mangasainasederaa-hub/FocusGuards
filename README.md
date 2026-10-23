# FocusGuard

Application Android (Kotlin) de concentration pour étudiants en Génie Civil.

## Fonctionnement
1. Tu crées des **tâches** (titre, matière, jours, horaires, message perso).
2. Tu choisis tes **applications à bloquer** (réseaux sociaux, jeux…).
3. Pendant une tâche, si tu ouvres une de ces applis, FocusGuard affiche un **écran de blocage** avec ton message.

Les matières du Génie Civil sont déjà dans la liste (recherche + choix d'un clic). Tu peux aussi **ajouter tes propres matières** (appui long sur une matière perso pour la supprimer).

## Obtenir l'APK (sans Android Studio)
1. Crée un compte gratuit sur github.com et un nouveau dépôt.
2. Envoie tout le contenu de ce dossier dans le dépôt (bouton « Add file » > « Upload files »).
3. Onglet **Actions** > « Construire l'APK » > **Run workflow**.
4. Quand c'est terminé (3 à 6 minutes), ouvre l'exécution et télécharge **FocusGuard-apk** (zip contenant `app-debug.apk`).
5. Installe l'APK sur le téléphone (autorise « Installer des applis inconnues »).

## Avec Android Studio
Ouvre le dossier, attends la synchronisation, puis **Build > Build APK(s)**.

## Après l'installation
Ouvre FocusGuard > **Activer la protection** > trouve **FocusGuard** dans les services d'accessibilité > active-le.
FocusGuard n'utilise ce service que pour connaître le nom de l'appli ouverte ; aucune donnée n'est lue ni envoyée.
