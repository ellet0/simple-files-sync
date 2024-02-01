# Simple Files Sync

A very simple solution to sync files, you will need to create file called `urls.json` in the same directory and run the jar

with the similar content: 


```json
[
  {
    "url": "https://raw.githubusercontent.com/ellet0/the-fantastic-team-mods-fabric/main/BetterF3-9.0.0-Fabric-1.20.4.jar",
    "sha1": "38d1588b91104c7375bf81e455902a5b7b61c479"
  },
  {
    "url": "https://raw.githubusercontent.com/ellet0/the-fantastic-team-mods-fabric/main/minecraft-comes-alive-7.5.12+1.20.4-universal.jar",
    "sha1": "c12a3ee2e09261f67e110fa7a01a1289deb52a56"
  }
]
```

On Windows you can get the SHA1 after download the file using:

```
certutil -hashfile ".\file.jar" SHA1
```

on Linux and Unix I'm not going to cover this because I assume you already know this


I created this project for minecraft server with mods, because I don't need old files staying in `.git`, also I don't want to require the players to download git