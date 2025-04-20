# RSMod Extra Pipeline

This project compiles two upstream RSMod modules (`engine/map` and `engine/routefinder`) into a single "extra" JAR, publishes it to a local Maven repository, and uses GitHub Actions to commit new artifacts when they change.

## How it works

1. **Checkout** this pipeline repo and the `rsmod/rsmod` upstream into `upstream/`.
2. Gradle `sourceSet("extra")` pulls Kotlin sources from:
    - `upstream/engine/map/src/main/kotlin`
    - `upstream/engine/routefinder/src/main/kotlin`
3. `./gradlew extraJar publishExtraPublicationToLocalRepo` builds the JAR and drops it under `local-maven/com/rsmod/rsmod-extra/1.0.0/`.
4. The GitHub Action computes the JAR CRC, compares it to the last run, and if different:
    - Copies the JAR into `artifacts/<run-number>/`
    - Updates `last_crc.txt`
    - Commits both back to `main`