## Commandes essentielles

### 1️⃣ Ajouter ADB au PATH de Windows (UNE SEULE FOIS)

**Méthode rapide (PowerShell en Admin) :**

1. Lance PowerShell **en tant qu'administrateur**
2. Tape cette commande :

```powershell
[Environment]::SetEnvironmentVariable("Path", $env:Path + ";$env:LOCALAPPDATA\Android\Sdk\platform-tools", "User")
```

1. **Ferme et rouvre** ton terminal
2. Fini ! ADB marchera tout le temps 🎉

**OU méthode manuelle (sans commande) :**

1. Clique droit sur **Ce PC** → **Propriétés**
2. **Paramètres système avancés**
3. **Variables d'environnement**
4. Dans les variables **utilisateur**, double-clique sur **Path**
5. Clique sur **Nouveau**
6. Ajoute : `C:\Users\jordb\AppData\Local\Android\Sdk\platform-tools`
7. **OK** partout, puis **ferme et rouvre** le terminal

### 2️⃣ Voir les logs du seeder
```powershell
adb logcat | Select-String "DatabaseSeeder"
```

### 3️⃣ Effacer toutes les données de l'app (RESET)
```powershell
adb shell pm clear com.example.swipy
```

### 4️⃣ Désinstaller l'app
```powershell
adb uninstall com.example.swipy
```


**Si les commandes ne marchent pas:**
1. Sur ton téléphone/émulateur : **Paramètres** → **Apps** → **Swipy**
2. Clique sur **Stockage**
3. **Effacer les données**
4. Relance l'app ▶️

## Notes

- Mot de passe de tous les comptes : `password123`
- Les données persistent entre les builds
- Pour reset : désinstalle l'app ou utilise `adb shell pm clear`
- Les photos viennent de [pravatar.cc](https://pravatar.cc/)
- La base de données s'appelle `swipy-db`

