# OC11-Code-Depot

## Workflow Git

**Résumé**

- `main` est la branche de production *stable*. 
- `dev-main` est la branche de préproduction et l'origine de toutes les branches de développement (`feature/*`, `bugfix/*`, ...).
- Les branches de développement partent de `dev-main`, sont testées puis fusionnées dans `dev-main`.
- Après validation sur `dev-main`, un merge vers `main` peut être réalisé.
- Les branches `main` et `dev-main` restent ouvertes ; les branches de développement sont supprimées après leur fusion.

---

### 1) Branches principales

- **`main`** — Production stable. Branche protégée : pas de push direct, PR requises, checks obligatoires.
- **`dev-main`** — Préproduction. Base pour toutes les branches de développement. Branche protégée et testée après chaque merge.

### 2) Création d’une branche de développement

- Créer la branche depuis `dev-main` :
  - `git checkout -b feature/ma-fonctionnalite dev-main`
  - `git checkout -b bugfix/ID-corrige dev-main`
- Conventions de nommage recommandées : `feature/<description>`, `bugfix/<issue>-<courte-description>`.

### 3) Développement et tests locaux

- Effectuer des commits atomiques et clairs.
- Lancer les tests unitaires et d’intégration localement avant d'ouvrir une PR.
- Mettre à jour la documentation ou les tests si nécessaire.

### 4) Pull Request vers `dev-main` (revue & CI)

- Ouvrir une PR vers `dev-main` lorsqu'une fonctionnalité est prête.
- Conditions minimales pour merger :
  - CI verte (tests, linter, build).
  - Revue par au moins une personne (ou plus selon le repo).
  - Respect des règles de sécurité et des politiques de commit.
- Après approbation et CI verte : merger la PR dans `dev-main` et supprimer la branche de développement.

### 5) Préproduction (`dev-main`) — Validation intégrée

- À chaque merge dans `dev-main`, exécuter les pipelines complets (tests, scans, build, déploiement sur staging si applicable).
- Procéder aux tests d'intégration et validations manuelles nécessaires.
- Si des problèmes sont détectés, corriger via de nouvelles branches issues de `dev-main`.

### 6) Passage en production (`dev-main` → `main`)

- Lorsque `dev-main` est stable et validée : ouvrir une PR de `dev-main` vers `main`.
- Exiger CI verte et approbation pour fusionner dans `main`.
- Après merge, tagguer la release (ex. `v1.2.3`) si souhaité.

### 7) Règles et pratiques recommandées

- **Ne pas supprimer** `main` ni `dev-main` : ces branches restent actives.
- **Supprimer** automatiquement les branches `feature/*` et `bugfix/*` après fusion.
- Protéger `main` et `dev-main` : interdire push direct, exiger PR, définir checks obligatoires et reviewers.
- Stratégie de merge recommandée : `Squash and merge` pour un historique propre ou `Merge commit` si vous souhaitez garder l’historique complet des merges.
- Automatiser au maximum (CI, tests, scans de sécurité, déploiement sur staging).
